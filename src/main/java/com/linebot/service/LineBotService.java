package com.linebot.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.linebot.bean.OrderInfo;
import com.linebot.bean.PostBack;
import com.linebot.bean.Product;
import com.linebot.enums.CarouselAction;
import com.linebot.enums.PostBackAction;
import com.linebot.enums.UserAction;
import com.linebot.utils.JsonUtils;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MemberJoinedEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@Service
@LineMessageHandler
public class LineBotService extends AbstractService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LineBotService.class);

  private static final String LOG_RQ = "Request Data: {}";

  private static final String DEFAULT_URL = "https://www.liontravel.com/category/zh-tw/index";

  private static final String DEFAULT_IMG_URL =
      "https://activity.liontravel.com/Images/Activity_Loading.jpg";

  @Autowired
  private ActiveService activeService;

  @Autowired
  private OrderService orderService;

  @EventMapping
  public void handleDefaultMessageEvent(Event event) {
    LOGGER.info(LOG_RQ, event);
  }

  @EventMapping
  public StickerMessage handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
    LOGGER.info(LOG_RQ, event);
    String packageId = event.getMessage().getPackageId();
    String stickerId = event.getMessage().getStickerId();
    if ("2".equals(packageId) && "32".equals(stickerId)) {
      activeService.resetData();
      return new StickerMessage("1", "109");
    }

    return new StickerMessage(packageId, stickerId);
  }

  @EventMapping
  public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    LOGGER.info(LOG_RQ, event);
    Source source = event.getSource();
    try {
      if (source.getClass().equals(GroupSource.class)) {
        handleGroupMessage(event, "group");
      } else if (source.getClass().equals(RoomSource.class)) {
        handleGroupMessage(event, "room");
      } else {
        handleUserMessage(event);
      }
    } catch (Exception e) {
      LOGGER.info(LOG_RQ, event);
      defaultMessage(event);
    }
  }

  private void handleGroupMessage(MessageEvent<TextMessageContent> event, String type) {
    String replyToken = event.getReplyToken();
    String groupId = event.getSource().getSenderId();
    String text = event.getMessage().getText();
    lineMessagingClient.replyMessage(new ReplyMessage(replyToken,
        new TextMessage("這是" + type + ", groupId=" + groupId + ", echo=" + text)));
  }

  private void handleUserMessage(MessageEvent<TextMessageContent> event)
      throws InterruptedException, ExecutionException {
    String text = event.getMessage().getText();
    String replyToken = event.getReplyToken();
    String userId = event.getSource().getUserId();

    switch (UserAction.fromMessage(text.toUpperCase())) {
      case MY_ORDER:
        List<OrderInfo> orderInfos = orderService.retrieveOrders(userId);
        if (orderInfos.isEmpty()) {
          lineMessagingClient.replyMessage(
              new ReplyMessage(replyToken, new TextMessage(UserAction.MY_ORDER.getSysReply())));
        } else {
          // 一次最多5筆TextMessage
          if (orderInfos.size() > 5) {
            orderInfos = orderInfos.subList(0, 5);
          }
          Message message = new TemplateMessage(UserAction.MY_ORDER.getSysReply(),
              getOrderCarouselTemplate(orderInfos));
          lineMessagingClient.pushMessage(new PushMessage(userId, message));
        }
        break;
      case NEAR_STORE:
        // lineMessagingClient.pushMessage(new PushMessage(userId, activeService.getProductList()
        // .stream().map(TextMessage::new).collect(Collectors.toList())));
        break;
      case NEW_ACTIVE:
        Message message =
            new TemplateMessage(UserAction.NEW_ACTIVE.getSysReply(), getProductCarouselTemplate());
        lineMessagingClient.pushMessage(new PushMessage(userId, message));
        break;
      case BUY_PRODUCT:
        int item = Integer.parseInt(text.replace(UserAction.BUY_PRODUCT.getMessage(), ""));
        sellItem(String.valueOf(item), replyToken, userId);
        break;
      default:
        defaultMessage(event);
        break;
    }
  }

  @EventMapping
  public void handlePostBackEvent(PostbackEvent event)
      throws InterruptedException, ExecutionException {
    LOGGER.info(LOG_RQ, event);
    try {
      PostBack postBackMsg =
          JsonUtils.fromJson(event.getPostbackContent().getData(), PostBack.class);
      switch (postBackMsg.getType()) {
        case BUY:
          sellItem(postBackMsg.getMessage(), event.getReplyToken(), event.getSource().getUserId());
          break;
        case ORDER_DETAIL:
          orderDetail(postBackMsg.getMessage(), event.getReplyToken(),
              event.getSource().getUserId());
          break;
        default:
          defaultMessage(event);
          break;
      }
    } catch (Exception e) {
      defaultMessage(event);
    }
  }

  @EventMapping
  public void handleJoinEvent(JoinEvent event) {
    Source source = event.getSource();
    String userId = source.getUserId();

    String replyToken = event.getReplyToken();
    try {

      CompletableFuture<UserProfileResponse> userProfileFuture;
      UserProfileResponse userProfile;

      if (replyToken != null) {
        TextMessage message =
            new TextMessage("大家好，我是line bot，現在時間=" + LocalDateTime.now().toString());
        lineMessagingClient.replyMessage(new ReplyMessage(replyToken, message));
      } else {
        userProfileFuture = lineMessagingClient.getGroupMemberProfile(source.getSenderId(), userId);
        if (userProfileFuture == null) {
          userProfileFuture =
              lineMessagingClient.getRoomMemberProfile(source.getSenderId(), userId);
        }

        if (userProfileFuture != null) {
          userProfile = userProfileFuture.get();

          TextMessage message =
              new TextMessage("source=" + JsonUtils.objToString(source) + ", username="
                  + userProfile.getDisplayName() + ", user=" + JsonUtils.objToString(userProfile));
          lineMessagingClient.replyMessage(new ReplyMessage(replyToken, message));
        }
      }
    } catch (Exception e) {
      LOGGER.info(LOG_RQ, event);
    }
  }

  @EventMapping
  public void handleMemberJoinedEvent(MemberJoinedEvent event) {
    List<Source> members = event.getJoined().getMembers();

    String replyToken = event.getReplyToken();
    try {
      if (replyToken != null) {
        List<String> names = new ArrayList<>();
        for (Source member : members) {
          UserProfileResponse userProfile = lineMessagingClient
              .getGroupMemberProfile(member.getSenderId(), member.getUserId()).get();
          names.add(userProfile.getDisplayName());
        }
        if (!names.isEmpty()) {
          TextMessage message =
              new TextMessage(Arrays.toString(names.toArray()) + "好，我是line bot，有什麼我可以服務的嗎?");
          lineMessagingClient.replyMessage(new ReplyMessage(replyToken, message));
        }
      }
    } catch (Exception e) {
      LOGGER.info(LOG_RQ, event);
    }
  }

  private void sellItem(String itemId, String replyToken, String userId)
      throws InterruptedException, ExecutionException {
    try {
      // 扣量
      Product product = activeService.sell(itemId);

      // 推送訊息
      lineMessagingClient.replyMessage(new ReplyMessage(replyToken,
          new TextMessage(product.getName() + UserAction.BUY_PRODUCT.getSysReply()))).get();

      // 成立訂單
      orderService.createOrder(userId, 1, product);
    } catch (Exception e) {
      Product product = activeService.getProduct(itemId);
      String errMessage = "很抱歉，您選購的商品不存在唷";
      if (product != null && "Sold Out".equalsIgnoreCase(e.getMessage())) {
        errMessage = UserAction.BUY_PRODUCT.getDefReply().replace("{NAME}", product.getName());
      }

      lineMessagingClient.replyMessage(new ReplyMessage(replyToken, new TextMessage(errMessage)))
          .get();
    }
  }

  private void orderDetail(String ordrNo, String replyToken, String userID) {
    List<OrderInfo> orderInfos = orderService.retrieveOrders(userID);
    OrderInfo order = orderInfos.stream().filter(ordr -> ordrNo.equals(ordr.getOrderNo()))
        .findFirst().orElse(null);
    try {
      if (orderInfos.isEmpty()) {
        lineMessagingClient.replyMessage(
            new ReplyMessage(replyToken, new TextMessage(UserAction.ORDER_DETAIL.getDefReply())));
      } else {
        Product product = activeService.getProducts(false).stream()
            .filter(prod -> prod.getId().equals(order.getItemId())).findFirst()
            .orElse(new Product());
        String orerDetail = UserAction.ORDER_DETAIL.getSysReply()
            .replace("{NO}", order.getOrderNo()).replace("{PRODUCT}", product.getName())
            .replace("{DESC}", product.getDesc().concat("\n").concat(product.getActiveUrl()))
            .replace("{AMOUNT}", String.valueOf(order.getPaxNumber()))
            .replace("{PRICE}", String.valueOf(order.getPrice()))
            .replace("{DATE}", order.getOrderNo());
        lineMessagingClient.replyMessage(new ReplyMessage(replyToken, new TextMessage(orerDetail)));
      }
    } catch (Exception e) {
      LOGGER.error("Order Detail:{}", ordrNo, e);
      lineMessagingClient.replyMessage(
          new ReplyMessage(replyToken, new TextMessage(UserAction.ORDER_DETAIL.getDefReply())));
    }
  }

  private CarouselTemplate getProductCarouselTemplate() {
    // 一次最多10筆CarouselColumn
    List<CarouselColumn> columns = activeService.getProducts(true).stream()
        .map(this::toCarouselColumn).collect(Collectors.toList());
    return new CarouselTemplate(columns);
  }

  private CarouselTemplate getOrderCarouselTemplate(List<OrderInfo> orderInfos) {
    // 一次最多10筆CarouselColumn
    List<Product> products = activeService.getProducts(false);
    List<CarouselColumn> columns = orderInfos.stream()
        .map(order -> toCarouselColumn(order, products)).collect(Collectors.toList());
    return new CarouselTemplate(columns);
  }

  private URI getURI(String url, String def) {
    try {
      if (url.isBlank() || url.isEmpty()) {
        return new URI(def);
      }
      return new URI(url);
    } catch (URISyntaxException ex) {
      LOGGER.error("URISyntaxException:{}/{}", url, def);
      return null;
    }
  }

  private CarouselColumn toCarouselColumn(Product product) {
    // Carousel最多3個Actions
    Action action1 = new URIAction(CarouselAction.PRODUCT.getAction1(),
        getURI(product.getActiveUrl(), DEFAULT_URL), null);
    Action action2 = new PostbackAction(CarouselAction.PRODUCT.getAction2(),
        setPostBack(PostBackAction.BUY, product.getId()), null);
    return new CarouselColumn(getURI(product.getImageUrl(), DEFAULT_IMG_URL), product.getName(),
        activeService.getActiveDesc(product), Arrays.asList(action1, action2));
  }

  private CarouselColumn toCarouselColumn(OrderInfo order, List<Product> products) {
    // Carousel最多3個Actions
    Product product = products.stream().filter(prod -> prod.getId().equals(order.getItemId()))
        .findFirst().orElse(new Product());
    String payUrl = "https://member.liontravel.com/order/myorderlist";
    Action action1 = new PostbackAction(CarouselAction.ORDER.getAction1(),
        setPostBack(PostBackAction.ORDER_DETAIL, order.getOrderNo()), null);
    Action action2 = new URIAction(CarouselAction.ORDER.getAction2(), getURI(payUrl, payUrl), null);
    return new CarouselColumn(getURI(product.getImageUrl(), DEFAULT_IMG_URL), order.getOrderNo(),
        product.getName(), Arrays.asList(action1, action2));
  }

  private String setPostBack(PostBackAction action, String msg) {
    PostBack orderMsg = new PostBack();
    orderMsg.setType(action);
    orderMsg.setMessage(msg);
    return JsonUtils.objToString(orderMsg);
  }

  private void defaultMessage(Event event) {
    try {
      lineMessagingClient.multicast(getErrorMulticast(event.getSource().getUserId()));
    } catch (Exception e) {
      LOGGER.info(LOG_RQ, event);
    }
  }

  private Multicast getErrorMulticast(String userId) {
    Set<String> user = new HashSet<>();
    user.add(userId);

    List<Message> message = new ArrayList<>();
    // https://devdocs.line.me/files/sticker_list.pdf
    message.add(new StickerMessage("2", "38"));
    message.add(new TextMessage(UserAction.UNKNOW.getDefReply()));

    return new Multicast(user, message);
  }
}
