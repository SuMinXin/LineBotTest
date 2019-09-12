package com.linebot.service;

public class ActiveException extends RuntimeException {

  private static final long serialVersionUID = 5368428352305070806L;

  public ActiveException(String message) {
    super(message);
  }

}
