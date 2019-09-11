package com.linebot.enums;

public enum UserAction {

  MY_ORDER("我的訂單", "以下是您的訂單資訊\n(只會顯示最新的5筆唷!)", "目前沒有您的訂單唷，快輸入 【最新優惠】選購喜歡的商品吧~"),

  NEAR_STORE("附近門市", "", ""),

  NEW_ACTIVE("最新優惠", "優惠活動來囉~~", "很抱歉，目前沒有可供購買的優惠活動 。\n敬請期待下次活動內容~"),

  BUY_PRODUCT("+", " 購買成功!", "很抱歉，您選購的【{NAME}】 已銷售完畢!\\n期待您下次選購^_^"),

  UNKNOW("", "", "這個帳號沒有辦法對您剛才的訊息內容做出回覆。\n試試看送出 【最新優惠】 ，可以看到目前最新的優惠活動唷~ \n期待您下次的訊息內容！"),

  ORDER_DETAIL("訂單明細",
      "訂單資訊:{NO}\n產品名稱:{PRODUCT}\n產品說明:{DESC}\n購買數量:{AMOUNT}\n單價:{PRICE}\n訂購時間:{DATE}",
      "目前無法查詢您的訂單，\n請稍後再試試。"),
  
  COUPON("COUPON訂單", "", ""),;

  private final String message;
  private final String sysReply;
  private final String defReply;


  private UserAction(String message, String sysReply, String defReply) {
    this.message = message;
    this.sysReply = sysReply;
    this.defReply = defReply;
  }

  public static UserAction fromMessage(String message) {
    for (UserAction act : UserAction.values()) {
      if (act.message.equals(message)) {
        return act;
      } else if (message.contains(BUY_PRODUCT.message)) {
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

  public String getDefReply() {
    return defReply;
  }
}
