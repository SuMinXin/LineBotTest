package com.linebot.bean;

import java.math.BigDecimal;

public class Product {

  private String id;
  private String name;
  private String desc;
  private BigDecimal price;
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

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
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
