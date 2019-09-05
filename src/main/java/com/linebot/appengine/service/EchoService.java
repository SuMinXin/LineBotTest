package com.linebot.appengine.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@Service
@LineMessageHandler
public class EchoService extends AbstractService {

  private static final Logger LOGGER = LoggerFactory.getLogger(EchoService.class);

  @EventMapping
  public StickerMessage handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
    LOGGER.info("Request Data: {}", event);
    return new StickerMessage(event.getMessage().getPackageId(), event.getMessage().getStickerId());
  }

  @EventMapping
  public void handleTextMessageEvent(MessageEvent<TextMessageContent> event)
      throws InterruptedException, ExecutionException, URISyntaxException {
    String text = event.getMessage().getText();
    String userId = event.getSource().getUserId();
    switch (text.toUpperCase()) {
      case "HI":
        lineMessagingClient.pushMessage(new PushMessage(userId,
            getPromotions().stream().map(TextMessage::new).collect(Collectors.toList())));
        break;
      case "YO":
        Message message = new TemplateMessage("優惠活動來囉~~", getCarouselTemplate());
        lineMessagingClient.pushMessage(new PushMessage(userId, message));
        break;
      default:
        lineMessagingClient
            .replyMessage(new ReplyMessage(event.getReplyToken(), new TextMessage(text))).get();
        break;
    }
  }

  @EventMapping
  public void handlePostBackEvent(PostbackEvent event)
      throws InterruptedException, ExecutionException {
    List<String> datas = Arrays.asList(event.getPostbackContent().getData().split("&"));
    Map<String, String> params =
        datas.stream().map(data -> data.split("=")).collect(Collectors.toMap(a -> a[0], a -> a[1]));
    lineMessagingClient.replyMessage(new ReplyMessage(event.getReplyToken(),
        new TextMessage("商品" + params.get("item") + " 購買成功!"))).get();
  }

  private List<String> getPromotions() {
    List<String> promotions = new ArrayList<>();
    promotions.add("優惠活動1:\n華納威秀電影團體券：230元/張\n剩餘 15 組");
    promotions.add("優惠活動2:\n朱銘美術館參觀票：好康價150元/張\n剩餘 8 組");
    promotions.add("優惠活動3:\n六福村中午12點以後入園：480元/張\n剩餘 19 組");
    promotions.add("優惠活動4:\n陶板屋-和風創作料理套餐券：585元/張\n剩餘 6 組");
    return promotions;
  }

  private CarouselTemplate getCarouselTemplate() throws URISyntaxException {
    String imageUrl = "https://activity.liontravel.com/Images/Activity_Loading.jpg";
    URI imageUri = new URI(imageUrl);

    String lionUrl = "https://www.liontravel.com/category/zh-tw/index";
    URI uri = new URI(lionUrl);

    AtomicInteger count = new AtomicInteger(1);
    List<CarouselColumn> columns = getPromotions().stream().map(c -> {
      // Carousel最多3個Action
      Action action1 = new URIAction("詳細內容", uri, null);
      Action action2 =
          new PostbackAction("立刻購買", "action=buy&item=" + count.getAndIncrement(), null);
      return new CarouselColumn(imageUri, c.split("\\\n")[0], c.split("\\\n", 2)[1],
          Arrays.asList(action1, action2));
    }).collect(Collectors.toList());

    return new CarouselTemplate(columns);
  }

}
