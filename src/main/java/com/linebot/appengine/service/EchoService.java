package com.linebot.appengine.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@Service
@LineMessageHandler
public class EchoService extends AbstractService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EchoService.class);
	private static final String LOG_RQ = "Request Data: {}";

	@EventMapping
	public StickerMessage handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
		LOGGER.info(LOG_RQ, event);
		return new StickerMessage(event.getMessage().getPackageId(), event.getMessage().getStickerId());
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		LOGGER.info(LOG_RQ, event);
	}

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		try {
			if ("hi".equalsIgnoreCase(event.getMessage().getText())) {
				// push 活動內容
				List<Message> message = new ArrayList<>();
				message.add(new TextMessage("優惠活動1:\n華納威秀電影團體券：230元/張\n剩餘 15 組"));
				message.add(new TextMessage("優惠活動2:\n朱銘美術館參觀票：好康價150元/張\n剩餘 8 組"));
				message.add(new TextMessage("優惠活動3:\n六福村中午12點以後入園：480元/張\n剩餘 19 組"));
				message.add(new TextMessage("優惠活動4:\n陶板屋-和風創作料理套餐券：585元/張\n剩餘 6 組"));
				message.add(new StickerMessage("2", "144"));
				Set<String> user = new HashSet<>();
				user.add(event.getSource().getUserId());
				client.multicast(new Multicast(user, message));
			} else if (event.getMessage().getText().contains("+")) {
				//
			} else {
				defaultMessage(event);
			}
		} catch (Exception e) {
			LOGGER.info(LOG_RQ, event);
			defaultMessage(event);
		}
	}

	private void defaultMessage(MessageEvent<TextMessageContent> event) {
		try {
			Set<String> user = new HashSet<>();
			user.add(event.getSource().getUserId());
			List<Message> message = new ArrayList<>();
			// https://devdocs.line.me/files/sticker_list.pdf
			message.add(new StickerMessage("2", "38"));
			String exMessage = "這個帳號沒有辦法您剛才的訊息內容做出回覆。\n送出 【HI】 的話可以看到目前最新的優惠活動唷~ \n期待您下次的訊息內容！";
			message.add(new TextMessage(exMessage));
			client.multicast(new Multicast(user, message));
		} catch (Exception e) {
			LOGGER.info(LOG_RQ, event);
		}
	}
}
