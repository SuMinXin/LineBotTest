package com.linebot.service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.google.common.annotations.VisibleForTesting;
import com.linebot.bean.LionOrderRQ;
import com.linebot.bean.LionOrderRS;
import com.linebot.bean.OrderInfo;
import com.linebot.bean.Product;
import com.linebot.client.LionApiClient;
import com.linebot.client.RedisClient;
import com.linebot.utils.JsonUtils;
import com.linebot.utils.OrderInfoConverter;

@Component
public class OrderService {

  @Autowired
  private LionApiClient lionApiClient;
  @Autowired
  private RedisClient redisClient;

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

  /**
   * 顧客下單
   * 
   * @param itemId 商品ID
   * @param paxNum 旅客人數(暫未使用多人)
   */
  @Async("threadPoolTaskExecutor")
  public void createOrder(String userId, Integer paxNum, Product product) {
    // 轉換訂購物件
    OrderInfo orderInfo = OrderInfoConverter.convert(userId, paxNum, product);

    // 取得訂單號
    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MMdd-HHmmss");
    String orderNo = df.format(orderInfo.getOrderDate());

    // 沒權限，拿不到單號
    // String year = String.valueOf(orderInfo.getOrderDate().getYear());
    // Integer ordr = 0;
    // LionOrderRS lionOrder = getOrder(year);
    // if (lionOrder != null) {
    // ordr = lionOrder.getIsno_seq();
    // }

    orderInfo.setOrderNo(orderNo);

    // 存入Redis
    redisClient.saveHashToken(getHashKey(product.getId()), userId.concat(":").concat(orderNo),
        JsonUtils.objToString(orderInfo));
  }

  /**
   * 活動結算
   * 
   * @param itemId 活動代碼
   */
  @Async("threadPoolTaskExecutor")
  public void transferOrders(String itemId) {
    List<String> paxDetail = redisClient.retrieveHashToken(getHashKey(itemId));
    if (!CollectionUtils.isEmpty(paxDetail)) {
      for (String order : paxDetail) {
        OrderInfo ordr = JsonUtils.fromJson(order, OrderInfo.class);
        redisClient.saveHashToken("Orders:".concat(ordr.getUserId()), ordr.getOrderNo(),
            JsonUtils.objToString(ordr));
      }
    }
  }

  @VisibleForTesting
  public List<OrderInfo> retrieveOrders(String userId) {
    String key = "Orders:".concat(userId);
    List<String> orderList = redisClient.retrieveHashToken(key);
    if (CollectionUtils.isEmpty(orderList)) {
      return Collections.emptyList();
    }
    return orderList.stream().map(order -> JsonUtils.fromJson(order, OrderInfo.class))
        .sorted(Comparator.comparing(OrderInfo::getOrderDate))
        .collect(Collectors.toList());
  }

  // 取單號
  @SuppressWarnings("unused")
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
    result.append(":");
    result.append(itemID);
    return result.toString();
  }

}
