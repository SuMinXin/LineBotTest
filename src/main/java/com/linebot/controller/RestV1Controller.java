package com.linebot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestV1Controller {

  @GetMapping("/")
  public String hello() {
    return "Hi, 歡迎來到LineBot測試!!";
  }

  @PostMapping("/webhook")
  public String webhook() {
    return "webhook";
  }

}
