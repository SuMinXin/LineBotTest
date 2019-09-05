package com.linebot.appengine.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.MessageEvent;
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

  private List<String> getPromotions() {
    List<String> promotions = new ArrayList<>();
    promotions.add("優惠活動1:\n華納威秀電影團體券：230元/張\n剩餘 15 組");
    promotions.add("優惠活動2:\n朱銘美術館參觀票：好康價150元/張\n剩餘 8 組");
    promotions.add("優惠活動3:\n六福村中午12點以後入園：480元/張\n剩餘 19 組");
    promotions.add("優惠活動4:\n陶板屋-和風創作料理套餐券：585元/張\n剩餘 6 組");
    return promotions;
  }

  private CarouselTemplate getCarouselTemplate() throws URISyntaxException {
    String lionUrl = "https://www.liontravel.com/category/zh-tw/index";
    URI uri = new URI(lionUrl);
    // Action action = new URIAction("label here", uri, null);
    // List<Action> actions = Arrays.asList(action);

    List<CarouselColumn> columns = getPromotions().stream().map(c -> {
      Action action = new URIAction(c.split("\\\n")[2], uri, null);
      return new CarouselColumn(null, c.split("\\\n")[0], c.split("\\\n")[1],
          Arrays.asList(action));
    }).collect(Collectors.toList());

    return new CarouselTemplate(columns);
  }

}
