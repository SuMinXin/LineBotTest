package com.linebot.appengine.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.Event;
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
public class EchoService extends ActiveService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EchoService.class);

	private static final String LOG_RQ = "Request Data: {}";

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		LOGGER.info(LOG_RQ, event);
	}

	@EventMapping
	public StickerMessage handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
		LOGGER.info(LOG_RQ, event);
		return new StickerMessage(event.getMessage().getPackageId(), event.getMessage().getStickerId());
	}

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		String text = event.getMessage().getText();
		String userId = event.getSource().getUserId();
		try {
			switch (text.toUpperCase()) {
			case "HI":
				lineMessagingClient.pushMessage(new PushMessage(userId,
						getPromotionList().stream().map(TextMessage::new).collect(Collectors.toList())));
				break;
			case "YO":
				Message message = new TemplateMessage("優惠活動來囉~~", getCarouselTemplate());
				lineMessagingClient.pushMessage(new PushMessage(userId, message));
				break;
			default:
				if (text.toUpperCase().contains("+")) {
					sellItem(text.replace("+", ""), event.getReplyToken());
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
	public void handlePostBackEvent(PostbackEvent event) {
		List<String> datas = Arrays.asList(event.getPostbackContent().getData().split("&"));
		Map<String, String> params = datas.stream().map(data -> data.split("="))
				.collect(Collectors.toMap(a -> a[0], a -> a[1]));
		try {
			sellItem(params.get("item"), event.getReplyToken());
		} catch (Exception e) {
			defaultMessage(event.getSource().getUserId(), String.join("\n", datas));
		}
	}

	private void sellItem(String itemID, String replyToken) throws InterruptedException, ExecutionException {
		String actName = "";
		try {
			actName = sell(itemID);
			lineMessagingClient.replyMessage(new ReplyMessage(replyToken, new TextMessage(actName + " 購買成功!"))).get();
		} catch (Exception e) {
			if (actName.isBlank() || actName.isEmpty()) {
				lineMessagingClient.replyMessage(new ReplyMessage(replyToken, new TextMessage("很抱歉，您選購的商品不存在唷"))).get();
			} else {
				lineMessagingClient.replyMessage(
						new ReplyMessage(replyToken, new TextMessage("很抱歉，您選購的商品" + actName + " 已銷售完畢囉!\n期待您的下次選購^_^")))
						.get();
			}
		}

	}

	private CarouselTemplate getCarouselTemplate() {
		String defaultImageUrl = "https://activity.liontravel.com/Images/Activity_Loading.jpg";
		String defaultLionUrl = "https://www.liontravel.com/category/zh-tw/index";
		AtomicInteger count = new AtomicInteger(1);
		List<CarouselColumn> columns = getPromotions().stream().map(c -> {
			// Carousel最多3個Action
			Action action1 = null;
			action1 = new URIAction("詳細內容", getURI(c.getActiveUrl(), defaultLionUrl), null);
			Action action2 = new PostbackAction("立刻購買", "action=buy&item=" + count.getAndIncrement(), null);
			return new CarouselColumn(getURI(c.getImageUrl(), defaultImageUrl), c.getName(), getActiveDesc(c),
					Arrays.asList(action1, action2));

		}).collect(Collectors.toList());
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

	private void defaultMessage(String userID, String oriMessage) {
		try {
			Set<String> user = new HashSet<>();
			user.add(userID);
			String exMessage = "這個帳號沒有辦法您剛才的訊息內容做出回覆。\n送出 【HI】 的話可以看到目前最新的優惠活動唷~ \n期待您下次的訊息內容！";
			List<Message> message = new ArrayList<>();
			// https://devdocs.line.me/files/sticker_list.pdf
			message.add(new StickerMessage("2", "38"));
			message.add(new TextMessage(exMessage));

			lineMessagingClient.multicast(new Multicast(user, message));
		} catch (Exception e) {
			LOGGER.info(LOG_RQ, oriMessage);
		}
	}

	private void defaultMessage(MessageEvent<TextMessageContent> event) {
		try {
			Set<String> user = new HashSet<>();
			user.add(event.getSource().getUserId());

			String exMessage = "這個帳號沒有辦法您剛才的訊息內容做出回覆。\n送出 【HI】 的話可以看到目前最新的優惠活動唷~ \n期待您下次的訊息內容！";
			List<Message> message = new ArrayList<>();
			// https://devdocs.line.me/files/sticker_list.pdf
			message.add(new StickerMessage("2", "38"));
			message.add(new TextMessage(exMessage));

			lineMessagingClient.multicast(new Multicast(user, message));
		} catch (Exception e) {
			LOGGER.info(LOG_RQ, event);
		}
	}

}