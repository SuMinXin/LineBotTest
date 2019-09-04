package com.linebot.appengine.service;

import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;

public class EchoServiceTest {
	@Test
	public void createTest() throws Exception {
		String channelSecret = "110d29531c085354329394e2b6eefee5";
		// Request body string
		String httpRequestBody = "{\"events\":[{\"type\":\"message\",\"replyToken\":\"ef9d86c9cbd84a7389e682fb120c8511\",\"source\":{\"userId\":\"Ua73de9ca23aa0664b9f6b99f4c55796d\",\"type\":\"user\"},\"timestamp\":1567481188463,\"message\":{\"type\":\"text\",\"id\":\"10503215608937\",\"text\":\"111\"}}],\"destination\":\"Ua73de9ca23aa0664b9f6b99f4c55796d\"}";
		SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);
		byte[] source = httpRequestBody.getBytes("UTF-8");
		String signature = Base64.getEncoder().encodeToString(mac.doFinal(source));
		System.out.println(signature);
	}

}
