package com.linebot.bean;

public enum UserAction {

  MY_ORDER("我的訂單","目前沒有您的訂單唷，快輸入 【最新優惠】選購喜歡的商品吧~"),
  NEAR_STORE("附近門市",""),
  NEW_ACTIVE("最新優惠","優惠活動來囉~~"),
  BUY_PRODUCT("+",""),
  UNKNOW("","這個帳號沒有辦法對您剛才的訊息內容做出回覆。\n試試看送出 【最新優惠】 ，可以看到目前最新的優惠活動唷~ \n期待您下次的訊息內容！");
  

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
    return UNKNOW;
  }

  public String getSysReply() {
    return sysReply;
  }
  
  public String getMessage() {
    return message;
  }
}
