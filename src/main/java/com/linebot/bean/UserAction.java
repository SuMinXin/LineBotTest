package com.linebot.bean;

public enum UserAction {

  MY_ORDER("我的訂單",""),
  NEAR_STORE("附近門市",""),
  NEW_ACTIVE("最新優惠","優惠活動來囉~~"),
  BUY_PRODUCT("+","");
  

  private final String message;
  private final String sysReply;


  private UserAction(String message, String sysReply) {
    this.message = message;
    this.sysReply = sysReply;
  }

  public static UserAction fromMessage(String message) {
    for (UserAction act : UserAction.values()) {
      if (act.message.equals(message)) {
        return act;
      }else if(message.contains(BUY_PRODUCT.message)){
        return BUY_PRODUCT;
      }
    }
    return null;
  }

  public String getSysReply() {
    return sysReply;
  }
  
  public String getMessage() {
    return message;
  }
}
