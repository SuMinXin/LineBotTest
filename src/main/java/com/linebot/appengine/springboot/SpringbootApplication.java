package com.linebot.appengine.springboot;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@RestController
@SpringBootApplication
@LineMessageHandler
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

	@EventMapping
	public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
		System.out.println("event: " + event);
		return new TextMessage(event.getMessage().getText());
	}

	@EventMapping
	public void handleDefaultMessageEvent(Event event) {
		System.out.println("event: " + event);
	}

	// 測試用
	@GetMapping("/newsignature")
	public String newsignature() {
		try {
			String channelSecret = "334c522f9936567ce2b22f18016be253";
			String httpRequestBody = "{\"events\":[{\"type\":\"message\",\"replyToken\":\"c9993dced4994005b6aad85ec96432ae\",\"source\":{\"userId\":\"Udbb24468127d89d1b43f54fb60517669\",\"type\":\"user\"},\"timestamp\":1567481188463,\"message\":{\"type\":\"text\",\"id\":\"10503215608937\",\"text\":\"111\"}}],\"destination\":\"Uc46d6cda07653357422640d533994cb9\"}";
			SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
			Mac mac;
			mac = Mac.getInstance("HmacSHA256");
			mac.init(key);
			byte[] source = httpRequestBody.getBytes("UTF-8");
			String signature = Base64.getEncoder().encodeToString(mac.doFinal(source));
			return signature;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

}
