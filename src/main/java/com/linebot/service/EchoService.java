
package com.linebot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@Service
@LineMessageHandler
public class EchoService {

  private static final Logger LOGGER = LoggerFactory.getLogger(EchoService.class);

  @EventMapping
  public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    LOGGER.info("event: {}", event);

    final String originalMessageText = event.getMessage().getText();
    LOGGER.info("message {} : {}", event.getReplyToken(), originalMessageText);

    return new TextMessage(originalMessageText);
  }

}
