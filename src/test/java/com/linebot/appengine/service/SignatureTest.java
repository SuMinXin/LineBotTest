package com.linebot.appengine.service;

import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;

public class SignatureTest {

  @Test
  public void createSignatureTest() throws Exception {
    String channelSecret = "cad4cc2f471f47e7033193d13e919f72";
    // Request body string
    String httpRequestBody =
        "{\"events\":[{\"type\":\"message\",\"replyToken\":\"4b805241f1954270a3353630734d51d3\",\"source\":{\"userId\":\"U6a408ec15c63c73fb36c17ea1258939f\",\"type\":\"user\"},\"timestamp\":1567481188463,\"message\":{\"type\":\"text\",\"id\":\"10503215608937\",\"text\":\"hi\"}}],\"destination\":\"Ua73de9ca23aa0664b9f6b99f4c55796d\"}";
    SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(key);
    byte[] source = httpRequestBody.getBytes("UTF-8");
    String signature = Base64.getEncoder().encodeToString(mac.doFinal(source));
    System.out.println(signature);
  }

}
