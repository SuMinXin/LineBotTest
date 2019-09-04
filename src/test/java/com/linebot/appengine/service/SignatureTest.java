package com.linebot.appengine.service;

import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;

public class SignatureTest {

	@Test
	public void createTest() throws Exception {
		String channelSecret = "334c522f9936567ce2b22f18016be253";
		// Request body string
		String httpRequestBody = "{\"events\":[{\"type\":\"message\",\"replyToken\":\"c9993dced4994005b6aad85ec96432ae\",\"source\":{\"userId\":\"Udbb24468127d89d1b43f54fb60517669\",\"type\":\"user\"},\"timestamp\":1567481188463,\"message\":{\"type\":\"text\",\"id\":\"10503215608937\",\"text\":\"hi\"}}],\"destination\":\"Uc46d6cda07653357422640d533994cb9\"}";
		SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);
		byte[] source = httpRequestBody.getBytes("UTF-8");
		String signature = Base64.getEncoder().encodeToString(mac.doFinal(source));
		System.out.println(signature);
	}

}
