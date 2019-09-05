package com.linebot.appengine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestLineBotController {

	@GetMapping("/")
	public String welcome() {
		return "Hi, 歡迎來到LineBot測試!!";
	}

}
