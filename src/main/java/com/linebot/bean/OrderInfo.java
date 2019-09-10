package com.linebot.bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderInfo {

  private LocalDateTime orderDate;
  private String orderNo;
  private Integer paxNumber;
  private BigDecimal price;
  private String itemId;
  private String userId;

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

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

}
