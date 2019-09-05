package com.linebot.appengine.service;

import java.io.IOException;
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

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.linebot.googledoc.GoogleDocService;
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
public class EchoService extends AbstractService {

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
				defaultMessage(event);
				break;
			}
		} catch (Exception e) {
			LOGGER.info(LOG_RQ, event);
			defaultMessage(event);
		}
	}

	@EventMapping
	public void handlePostBackEvent(PostbackEvent event) throws InterruptedException, ExecutionException {
		List<String> datas = Arrays.asList(event.getPostbackContent().getData().split("&"));
		Map<String, String> params = datas.stream().map(data -> data.split("="))
				.collect(Collectors.toMap(a -> a[0], a -> a[1]));
		lineMessagingClient
				.replyMessage(
						new ReplyMessage(event.getReplyToken(), new TextMessage("商品" + params.get("item") + " 購買成功!")))
				.get();
	}

	private List<String> getPromotionList() {
		List<String> promotions = new ArrayList<>();
		try {
			List<List<Object>> response = readGoogleDoc();
			if (!response.isEmpty()) {
				response.forEach(action -> {
					StringBuilder active = new StringBuilder();
					Promotion act = setPromotion(action);
					active.append("活動名稱：");
					active.append(act.getName());
					active.append('\n');
					active.append(getActiveDesc(act));
					promotions.add(active.toString());
				});
			}

		} catch (IOException e) {
			LOGGER.error("Read Google Doc Fail:{}", e);
		}
		return promotions;
	}

	private List<Promotion> getPromotions() {
		List<Promotion> promotions = new ArrayList<>();
		try {
			List<List<Object>> response = readGoogleDoc();
			if (!response.isEmpty()) {
				response.forEach(action -> {
					promotions.add(setPromotion(action));
				});
			}
		} catch (IOException e) {
			LOGGER.error("Read Google Doc Fail:{}", e);
		}
		return promotions;
	}

	private Promotion setPromotion(List<Object> action) {
		Promotion temp = new Promotion();
		temp.setId(action.get(0).toString());
		temp.setName(action.get(1).toString());
		temp.setDesc(action.get(2).toString());
		temp.setPrice(Integer.valueOf(action.get(3).toString()));
		temp.setUnit(action.get(4).toString());
		temp.setAmount(Integer.valueOf(action.get(5).toString()));
		temp.setImageUrl(action.get(6).toString());
		temp.setActiveUrl(action.get(7).toString());
		return temp;
	}

	private CarouselTemplate getCarouselTemplate() {
		String defaultImageUrl = "https://activity.liontravel.com/Images/Activity_Loading.jpg";
		String defaultLionUrl = "https://www.liontravel.com/category/zh-tw/index";
		List<CarouselColumn> columns = new ArrayList<>();
		AtomicInteger count = new AtomicInteger(1);
		columns = getPromotions().stream().map(c -> {
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

	private String getActiveDesc(Promotion promotion) {
		StringBuilder result = new StringBuilder();
		result.append(promotion.getDesc());
		result.append('\n');
		result.append(promotion.getPrice());
		result.append("/");
		result.append(promotion.getUnit());
		result.append('\n');
		result.append("剩餘數量：");
		result.append(promotion.getAmount());
		return result.toString();
	}

	private List<List<Object>> readGoogleDoc() throws IOException {
		// Build a new authorized API client service.
		Sheets service = GoogleDocService.getSheetsService();
		// https://docs.google.com/spreadsheets/d/1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s/edit#gid=0
		String spreadsheetId = "1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s";
		String range = "Action!A2:H";
		ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
		List<List<Object>> values = response.getValues();
		if (values == null || values.isEmpty()) {
			LOGGER.info("No data found.");
		} else {
			return response.getValues();
		}
		return new ArrayList<>();
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

class Promotion {

	private String id;
	private String name;
	private String desc;
	private Integer price;
	private String unit;
	private Integer amount;
	private String imageUrl;
	private String activeUrl;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getActiveUrl() {
		return activeUrl;
	}

	public void setActiveUrl(String activeUrl) {
		this.activeUrl = activeUrl;
	}
}
