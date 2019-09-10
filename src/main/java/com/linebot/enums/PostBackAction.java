package com.linebot.enums;

public enum PostBackAction {
  BUY("BUY"),
  ORDER_DETAIL("ORDER");

  private final String type;

  private PostBackAction(String type) {
    this.type = type;
  }
}
