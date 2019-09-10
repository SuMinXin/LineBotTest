package com.linebot.bean;

import com.linebot.enums.PostBackAction;

public class PostBack {

  private PostBackAction type;
  private String message;

  public PostBackAction getType() {
    return type;
  }

  public void setType(PostBackAction type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
