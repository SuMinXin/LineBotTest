package com.linebot.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.linebot.bean.Product;

@Service
public class ActiveService {

  @Autowired
  private OrderService orderService;

  private Lock lock = new ReentrantLock();

  private GoogleSheetService sheetService = GoogleSheetService.getInstance();

  private static final String SHEET_ID = "1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s";

  private static final String SHEET_NAME = "Action!";

  private static Map<String, Product> productsMap = new HashMap<>();

  private static Integer exprireTime = 10;

  private static long prevResetTime = 0;

  // ---------------------------- 依Message 回覆 ----------------------------
  // HI
  public List<String> getProductList() {
    return getProducts(true).stream().map(action -> {
      StringBuilder active = new StringBuilder();
      active.append("活動名稱：");
      active.append(action.getName());
      active.append('\n');
      active.append(getActiveDesc(action));
      return active.toString();
    }).collect(Collectors.toList());
  }

  // YO
  public List<Product> getProducts(boolean excludeZero) {
    readData();
    if (excludeZero) {
      return productsMap.values().stream().filter(act -> act.getAmount() > 0)
          .collect(Collectors.toList()); // 只回傳可賣商品
    } else {
      return new ArrayList<>(productsMap.values());
    }
  }

  public void resetData() {
    productsMap.clear();
    readData();
  }

  // ---------------------------- 購買商品 ----------------------------
  public String sell(String id) throws Exception {
    readData();
    // 數量 -1
    lock.lock();
    String response = null;
    try {
      Product action = productsMap.get(id);
      if (action == null) {
        throw new Exception("Active Not Exist");
      }
      response = action.getName();
      if (action.getAmount() > 0) {
        action.setAmount(action.getAmount() - 1);
        updateProduct(id);
      } else {
        orderService.activeFinished(id); // 結算
        throw new Exception("Sold Out");
      }
    } finally {
      lock.unlock();
    }
    return response;
  }

  public void addOrder(String itemID, String userID) {
    orderService.addPax(itemID, userID, 1, productsMap.get(itemID).getPrice());
  }

  // ---------------------------- Product Function ----------------------------
  private void readData() {
    if (productsMap.isEmpty() || isExpire()) {
      prevResetTime = System.currentTimeMillis();
      // https://docs.google.com/spreadsheets/d/1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s/edit#gid=0
      String range = SHEET_NAME + "A2:H";
      List<List<Object>> response =
          GoogleSheetService.getInstance().readGoogleSheet(SHEET_ID, range);
      if (!response.isEmpty()) {
        List<Product> acts = response.stream().map(this::toProduct).collect(Collectors.toList());
        for (Product act : acts) {
          if (!productsMap.containsKey(act.getId())) {
            productsMap.put(act.getId(), act);
          }
        }
      }
    }
  }

  private boolean isExpire() {
    long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - prevResetTime);
    if (prevResetTime <= 0 || minutes > exprireTime) {
      return true;
    }
    return false;
  }

  private Product toProduct(List<Object> object) {
    Product product = new Product();
    product.setId(object.get(0).toString());
    product.setName(object.get(1).toString());
    product.setDesc(object.get(2).toString());
    product.setPrice(BigDecimal.valueOf(Integer.valueOf(object.get(3).toString())));
    product.setUnit(object.get(4).toString());
    product.setAmount(Integer.valueOf(object.get(5).toString()));
    product.setImageUrl(object.get(6).toString());
    product.setActiveUrl(object.get(7).toString());
    return product;
  }

  private void updateProduct(String id) {
    Product product = productsMap.get(id);
    ValueRange data = toValueRange(product);
    sheetService.updateGoogleSheet(SHEET_ID, Arrays.asList(data));
  }

  // ---------------------------- Format ----------------------------
  public String getActiveDesc(Product product) {
    StringBuilder description = new StringBuilder();
    description.append(product.getDesc());
    description.append('\n');
    description.append(product.getPrice());
    description.append("/");
    description.append(product.getUnit());
    description.append('\n');
    description.append("剩餘數量：").append(product.getAmount());
    return description.toString();
  }

  private ValueRange toValueRange(Product product) {
    return new ValueRange().setRange(SHEET_NAME + "F" + (Integer.valueOf(product.getId()) + 1))
        .setValues(Arrays.asList(Arrays.asList(product.getAmount())));
  }

}
