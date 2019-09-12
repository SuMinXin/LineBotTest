package com.linebot.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.linebot.utils.JsonUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SheetProductServiceTest {

  @Autowired
  private SheetProductService sheetProductService;

  @Test
  public void getProducts() {
    System.out.println(JsonUtils.objToString(sheetProductService.getProductMap(true)));
  }

}
