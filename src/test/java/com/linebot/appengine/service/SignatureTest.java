package com.linebot.appengine.service;

import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;

public class SignatureTest {

  @Test
  public void createSignatureTest() throws Exception {
    String channelSecret = "b98ec764f6b609527ae34446d4e6a1cf";
    // Request body string
    String httpRequestBody =
        "{\"events\":[{\"type\":\"message\",\"replyToken\":\"af7738d161c4486a84cdceb0a43a7fd1\",\"source\":{\"userId\":\"U6a408ec15c63c73fb36c17ea1258939f\",\"type\":\"user\"},\"timestamp\":1567481188463,\"message\":{\"type\":\"sticker\",\"id\":\"10521461786656\",\"packageId\":\"2\",\"stickerId\":\"32\"}}],\"destination\":\"Ua73de9ca23aa0664b9f6b99f4c55796d\"}";
    SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(key);
    byte[] source = httpRequestBody.getBytes("UTF-8");
    String signature = Base64.getEncoder().encodeToString(mac.doFinal(source));
    System.out.println(signature);
  }

}
