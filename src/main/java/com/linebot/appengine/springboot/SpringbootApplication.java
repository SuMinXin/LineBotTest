package com.linebot.appengine.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class SpringbootApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringbootApplication.class, args);
  }

  @GetMapping("/")
  public String hello() {
    return "Hi, 歡迎來到LineBot測試!!";
  }

  @PostMapping("/webhook")
  public String webhook() {
    return "webhook";
  }

}

