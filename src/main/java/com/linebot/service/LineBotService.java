package com.linebot.service;

import java.net.URI;
import java.net.URISyntaxException;
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
import com.linebot.bean.Product;
import com.linebot.utils.JsonUtils;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.JoinEvent;
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
    String text = event.getMessage().getText();
    String userId = event.getSource().getUserId();
    try {
      switch (text.toUpperCase()) {
        case "我的訂單":
          // lineMessagingClient.pushMessage(new PushMessage(userId, activeService.getProductList()
          // .stream().map(TextMessage::new).collect(Collectors.toList())));
          break;
        case "附近門市":
          // lineMessagingClient.pushMessage(new PushMessage(userId, activeService.getProductList()
          // .stream().map(TextMessage::new).collect(Collectors.toList())));
          break;
        case "最新優惠":
          Message message = new TemplateMessage("優惠活動來囉~~", getCarouselTemplate());
          lineMessagingClient.pushMessage(new PushMessage(userId, message));
          break;
        default:
          if (text.contains("+")) {
            int item = Integer.parseInt(text.replace("+", ""));
            sellItem(String.valueOf(item), event.getReplyToken(), event.getSource().getUserId());
          } else {
            defaultMessage(event);
          }
          break;
      }
    } catch (Exception e) {
      LOGGER.info(LOG_RQ, event);
      defaultMessage(event);
    }
  }

  @EventMapping
  public void handlePostBackEvent(PostbackEvent event)
      throws InterruptedException, ExecutionException {
    LOGGER.info(LOG_RQ, event);

    try {
      sellItem(event.getPostbackContent().getData(), event.getReplyToken(),
          event.getSource().getUserId());
    } catch (Exception e) {
      defaultMessage(event);
    }
  }

  @EventMapping
  public void handleJoinEvent(JoinEvent event) {
    Source source = event.getSource();
    // String replyToken = event.getReplyToken();

    String userId = source.getUserId();

    try {
      TextMessage message;
      CompletableFuture<UserProfileResponse> userProfileFuture;
      UserProfileResponse userProfile;
      if (source instanceof RoomSource) {
        message = new TextMessage("這是RoomSource");
        userProfileFuture = lineMessagingClient.getRoomMemberProfile(source.getSenderId(), userId);
        userProfile = userProfileFuture.get();
      } else if (source instanceof GroupSource) {
        message = new TextMessage("這是GroupSource");
        userProfileFuture = lineMessagingClient.getGroupMemberProfile(source.getSenderId(), userId);
        userProfile = userProfileFuture.get();
      } else {
        message = new TextMessage("這是UserSource");
        userProfileFuture = lineMessagingClient.getProfile(userId);
        userProfile = userProfileFuture.get();
      }
      LOGGER.info("UserProfile={}", userProfile.getDisplayName());
      LOGGER.info("UserProfileJson={}", JsonUtils.objToString(userProfile));
      lineMessagingClient.pushMessage(new PushMessage(userId, message));
    } catch (Exception e) {
      LOGGER.info(LOG_RQ, event);
    }
  }

  private void sellItem(String itemID, String replyToken, String userID)
      throws InterruptedException, ExecutionException {
    String actName = "";
    try {
      actName = activeService.sell(itemID);
      lineMessagingClient
          .replyMessage(new ReplyMessage(replyToken, new TextMessage(actName + " 購買成功!"))).get();
      activeService.addOrder(itemID, userID);
    } catch (Exception e) {
      if ("Active Not Exist".equalsIgnoreCase(e.getMessage())) {
        lineMessagingClient
            .replyMessage(new ReplyMessage(replyToken, new TextMessage("很抱歉，您選購的商品不存在唷"))).get();
      } else {
        lineMessagingClient.replyMessage(new ReplyMessage(replyToken,
            new TextMessage("很抱歉，您選購的商品" + actName + " 已銷售完畢囉!\n期待您的下次選購^_^"))).get();
      }
    }
  }

  private CarouselTemplate getCarouselTemplate() {
    List<CarouselColumn> columns = activeService.getProducts(true).stream()
        .map(this::toCarouselColumn).collect(Collectors.toList());
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
    Action action1 = new URIAction("詳細內容", getURI(product.getActiveUrl(), DEFAULT_URL), null);
    Action action2 = new PostbackAction("立即購買", product.getId(), null);
    return new CarouselColumn(getURI(product.getImageUrl(), DEFAULT_IMG_URL), product.getName(),
        activeService.getActiveDesc(product), Arrays.asList(action1, action2));
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

    String exMessage = "這個帳號沒有辦法對您剛才的訊息內容做出回覆。\n試試看送出 【最新優惠】 ，可以看到目前最新的優惠活動唷~ \n期待您下次的訊息內容！";
    List<Message> message = new ArrayList<>();
    // https://devdocs.line.me/files/sticker_list.pdf
    message.add(new StickerMessage("2", "38"));
    message.add(new TextMessage(exMessage));

    return new Multicast(user, message);
  }

}
