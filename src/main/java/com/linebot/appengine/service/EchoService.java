package com.linebot.appengine.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@Service
@LineMessageHandler
public class EchoService extends UtilService {

	@PostConstruct
	private void buildLineMessagingService() {
		lineMessageClient = LineMessagingClient.builder(() -> channelToken).build();
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
			actions.forEach(act -> {
				lineMessageClient.pushMessage(new PushMessage(event.getSource().getUserId(),
						Collections.singletonList(new TextMessage(act))));
			});
		} else {
			lineMessageClient.replyMessage(new ReplyMessage(event.getReplyToken(),
					Collections.singletonList(new TextMessage(event.getSource().getUserId())))).get();
		}

	}

}
