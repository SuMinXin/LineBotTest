package com.linebot.appengine.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.linebot.service.OrderService;
import com.linebot.utils.JsonUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class OrderServiceTest extends SignatureTest {

  @Autowired
  private OrderService orderService;

  @Test
  public void retrieveOrders() throws Exception {
    System.out.println(
        JsonUtils.objToString(orderService.retrieveOrders("U6a408ec15c63c73fb36c17ea1258939f")));
  }

}
