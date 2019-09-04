package com.linebot.appengine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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
  public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    LOGGER.info("Request Data: {}", event);
    return new TextMessage(event.getMessage().getText());
  }

  @EventMapping
  public StickerMessage handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
    LOGGER.info("Request Data: {}", event);
    return new StickerMessage(event.getMessage().getPackageId(), event.getMessage().getStickerId());
  }

  @EventMapping
  public void handleDefaultMessageEvent(Event event) {
    LOGGER.info("Request Data: {}", event);
  }

}
