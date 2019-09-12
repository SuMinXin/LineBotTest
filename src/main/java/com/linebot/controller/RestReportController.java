package com.linebot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.linebot.service.UserActiveService;

@RestController
public class RestReportController {

  @Autowired
  private UserActiveService userActiveService;

  @GetMapping(value = {"/report", "/report/{type}", "/product/{product}"})
  public String report(@PathVariable(required = false) String type,
      @PathVariable(required = false) String product) {
    type = (type == null ? "" : type);
    switch (type) {
      case "":
        return userActiveService.allAction();
      case "product":
        return userActiveService.product(product);
      default:
        return "Can't Find Report Type";
    }
  }

}
