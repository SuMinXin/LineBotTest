package com.linebot.appengine.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
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
  public void handleDefaultMessageEvent(Event event) {
    LOGGER.info("Request Data: {}", event);
  }

  @EventMapping
  public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
    if ("hi".equalsIgnoreCase(event.getMessage().getText())) {
      // push 活動內容
      List<String> actions = new ArrayList<>();
      actions.add("優惠活動1:\n華納威秀電影團體券：230元/張\n剩餘 15 組");
      actions.add("優惠活動2:\n朱銘美術館參觀票：好康價150元/張\n剩餘 8 組");
      actions.add("優惠活動3:\n六福村中午12點以後入園：480元/張\n剩餘 19 組");
      actions.add("優惠活動4:\n陶板屋-和風創作料理套餐券：585元/張\n剩餘 6 組");
      actions.forEach(act -> client.pushMessage(new PushMessage(event.getSource().getUserId(),
          Collections.singletonList(new TextMessage(act)))));
    } else {
      client.replyMessage(new ReplyMessage(event.getReplyToken(),
          Collections.singletonList(new TextMessage(event.getSource().getUserId())))).get();
    }

  }
}
