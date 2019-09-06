package com.linebot.appengine.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.linebot.googledoc.GoogleDocService;

@Service
public class ActiveService extends AbstractService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveService.class);

	private Lock lock = new ReentrantLock();

	private static List<Promotion> actives = new ArrayList<>();

	// HI
	public List<String> getPromotionList() {
		List<String> promotions = new ArrayList<>();
		setPormotions();
		actives.forEach(action -> {
			StringBuilder active = new StringBuilder();
			active.append("活動代碼：");
			active.append(action.getId());
			active.append('\n');
			active.append("活動名稱：");
			active.append(action.getName());
			active.append('\n');
			active.append(getActiveDesc(action));
			promotions.add(active.toString());
		});
		return promotions;
	}

	// YO
	public List<Promotion> getPromotions() {
		setPormotions();
		return actives;
	}

	// Sell
	public String sell(String id) throws Exception {
		// 數量 -1
		lock.lock();
		String response = null;
		try {
			Promotion action = actives.stream().filter(o -> id.equals(o.getId())).findFirst().orElse(null);
			if (action == null) {
				throw new Exception("Active Not Exist");
			}
			response = action.getName();
			if (action.getAmount() > 0) {
				action.setAmount(action.getAmount() - 1);
			} else {
				throw new Exception("Sold Out");
			}
		} finally {
			lock.unlock();
		}
		return response;
	}

	private void setPormotions() {
		if (CollectionUtils.isEmpty(actives)) {
			try {
				List<List<Object>> response = readGoogleDoc();
				if (!response.isEmpty()) {
					response.forEach(action -> {
						actives.add(setPromotion(action));
					});
				}
			} catch (IOException e) {
				LOGGER.error("Read Google Doc Fail:{}", e);
			}
		}
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

	public String getActiveDesc(Promotion promotion) {
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
}
