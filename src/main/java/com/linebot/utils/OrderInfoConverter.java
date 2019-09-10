package com.linebot.utils;

import java.time.LocalDateTime;
import com.linebot.bean.OrderInfo;
import com.linebot.bean.Product;

public final class OrderInfoConverter {

  private OrderInfoConverter() {}

  public static OrderInfo convert(String userId, int paxNum, Product product) {
    LocalDateTime now = LocalDateTime.now();

    OrderInfo orderInfo = new OrderInfo();
    orderInfo.setItemId(product.getId());
    orderInfo.setOrderDate(now);
    orderInfo.setPaxNumber(paxNum);
    orderInfo.setPrice(product.getPrice());
    orderInfo.setUserId(userId);

    return orderInfo;
  }

}
