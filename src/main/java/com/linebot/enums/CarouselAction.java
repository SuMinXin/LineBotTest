package com.linebot.enums;

public enum CarouselAction {

  PRODUCT("詳細內容","立即購買",""),
  ORDER("訂單明細","付款連結","");
  

  private final String action1;
  private final String action2;
  private final String action3;


  private CarouselAction(String action1, String action2, String action3) {
    this.action1 = action1;
    this.action2 = action2;
    this.action3 = action3;
  }


  public String getAction1() {
    return action1;
  }
  
  public String getAction2() {
    return action2;
  }
  
  public String getAction3() {
    return action3;
  }
}
