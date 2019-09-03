package com.linebot.appengine.service;

import java.util.Collections;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@Service
@LineMessageHandler
public class EchoService {

	@Value("${line.bot.channelToken}")
	private String channelToken;

	private LineMessagingClient lineMessageClient;

	@PostConstruct
	private void buildLineMessagingService() {
		lineMessageClient = LineMessagingClient.builder(() -> channelToken).build();
	}

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
		// LOGGER.info("event: {}", event);
		// BotApiResponse apiResposnse =
		lineMessageClient.replyMessage(new ReplyMessage(event.getReplyToken(),
				Collections.singletonList(new TextMessage(event.getSource().getUserId())))).get();
		// sLOGGER.info("Sent messages: {}", apiResponse);
	}

}
