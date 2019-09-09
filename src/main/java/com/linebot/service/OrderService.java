package com.linebot.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.linebot.bean.LionOrderRQ;
import com.linebot.bean.LionOrderRS;
import com.linebot.client.LionApiClient;
import com.linebot.client.RedisClient;
import com.linebot.utils.JsonUtils;

@Component
public class OrderService {

	@Autowired
	private LionApiClient lionApiClient;
	@Autowired
	private RedisClient redisClient;

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

	/**
	 * 
	 * @param itemID 商品ID
	 * @param paxNum 旅客人數(暫未使用多人)
	 */
	@Async("threadPoolTaskExecutor")
	public void addPax(String itemID, String userID, Integer paxNum, BigDecimal price) {
		LocalDateTime now = LocalDateTime.now();
		String year = String.valueOf(now.getYear());
		Integer ordr = 0;
		LionOrderRS lionOrder = getOrder(year);
		if (lionOrder != null) {
			ordr = lionOrder.getIsno_seq();
		}
		// setRedisValue
		RedisValue value = new RedisValue();
		value.setItemID(itemID);
		value.setOrderDate(now);
		value.setPaxNumber(paxNum);
		value.setPrice(price);
		value.setUserID(userID);
		value.setOrderNo(year.concat("-").concat(String.valueOf(ordr)));
		redisClient.saveHashToken(getHashKey(itemID), userID.concat(":").concat(String.valueOf(ordr)),
				JsonUtils.objToString(value));
	}

	@Async("threadPoolTaskExecutor")
	public void activeFinished(String itemID) {
		List<String> paxDetail = redisClient.retrieveHashToken(getHashKey(itemID));
		if (!CollectionUtils.isEmpty(paxDetail)) {
			/*
			 * Map<String, Set<String>> map2 =
			 * paxDetail.stream().collect(Collectors.groupingBy(RedisValue::getUserID,
			 * Collectors.mapping(pax -> pax.getOrderNo(), Collectors.toSet())));
			 */
			Map<String, List<RedisValue>> map = new HashMap<>();
			for (String order : paxDetail) {
				RedisValue ordr = JsonUtils.fromJson(order, RedisValue.class);

				if (map.containsKey(ordr.getUserID())) {
					map.get(ordr.getUserID()).add(ordr);
				} else {
					map.put(ordr.getUserID(), Arrays.asList(ordr));
				}
			}
		}

	}

	// 取單號
	private LionOrderRS getOrder(String year) {
		LionOrderRQ req = new LionOrderRQ();
		req.setIsno_year(year);
		req.setIsno_type("ORDR");
		String resp = lionApiClient.post("isnom00", req);
		try {
			return JsonUtils.fromJson(resp, LionOrderRS.class);
		} catch (Exception e) {
			LOGGER.error("Get New Order Fail!");
			return null;
		}
	}

	private String getHashKey(String itemID) {
		StringBuilder result = new StringBuilder();
		result.append("Promotion");
		result.append(itemID);
		result.append(":");
		result.append("");
		return result.toString();
	}

	class RedisValue {
		private LocalDateTime orderDate;
		private String orderNo;
		private Integer paxNumber;
		private BigDecimal price;
		private String itemID;
		private String userID;

		public LocalDateTime getOrderDate() {
			return orderDate;
		}

		public void setOrderDate(LocalDateTime orderDate) {
			this.orderDate = orderDate;
		}

		public String getOrderNo() {
			return orderNo;
		}

		public void setOrderNo(String orderNo) {
			this.orderNo = orderNo;
		}

		public Integer getPaxNumber() {
			return paxNumber;
		}

		public void setPaxNumber(Integer paxNumber) {
			this.paxNumber = paxNumber;
		}

		public BigDecimal getPrice() {
			return price;
		}

		public void setPrice(BigDecimal price) {
			this.price = price;
		}

		public String getItemID() {
			return itemID;
		}

		public void setItemID(String itemID) {
			this.itemID = itemID;
		}

		public String getUserID() {
			return userID;
		}

		public void setUserID(String userID) {
			this.userID = userID;
		}
	}
}
