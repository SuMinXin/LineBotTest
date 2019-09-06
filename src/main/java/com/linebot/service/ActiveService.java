package com.linebot.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.linebot.bean.Product;

@Service
public class ActiveService {

  private Lock lock = new ReentrantLock();

  private GoogleSheetService sheetService = GoogleSheetService.getInstance();

  private static final String SHEET_ID = "1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s";

  private static final String SHEET_NAME = "Action!";

  private static Map<String, Product> productsMap = new HashMap<>();

  private static List<Integer> exprireTime = new ArrayList<>(Arrays.asList(0, 10, 20, 30, 40, 50));

  // HI
  public List<String> getProductList() {
    return getProducts().stream().map(action -> {
      StringBuilder active = new StringBuilder();
      active.append("活動名稱：");
      active.append(action.getName());
      active.append('\n');
      active.append(getActiveDesc(action));
      return active.toString();
    }).collect(Collectors.toList());
  }

  // YO
  public List<Product> getProducts() {
    readData();
    return new ArrayList<>(productsMap.values());
  }

  // Sell
  public String sell(String id) throws Exception {
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
        throw new Exception("Sold Out");
      }
    } finally {
      lock.unlock();
    }
    return response;
  }

  public void resetData() {
    productsMap.clear();
    readData();
  }

  private void readData() {
    if (productsMap.isEmpty() || isExpire()) {
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
    if (LocalDateTime.now().getMinute() > exprireTime.get(0)) {
      exprireTime.add(exprireTime.get(0));
      exprireTime.remove(0);
      return true;
    } else {
      return false;
    }
  }

  private Product toProduct(List<Object> object) {
    Product product = new Product();
    product.setId(object.get(0).toString());
    product.setName(object.get(1).toString());
    product.setDesc(object.get(2).toString());
    product.setPrice(Integer.valueOf(object.get(3).toString()));
    product.setUnit(object.get(4).toString());
    product.setAmount(Integer.valueOf(object.get(5).toString()));
    product.setImageUrl(object.get(6).toString());
    product.setActiveUrl(object.get(7).toString());
    return product;
  }

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

  private void updateProduct(String id) {
    Product product = productsMap.get(id);
    ValueRange data = toValueRange(product);
    sheetService.updateGoogleSheet(SHEET_ID, Arrays.asList(data));
  }

  public void updateProducts() {
    List<ValueRange> data =
        productsMap.values().stream().map(this::toValueRange).collect(Collectors.toList());
    sheetService.updateGoogleSheet(SHEET_ID, data);
  }

  private ValueRange toValueRange(Product product) {
    return new ValueRange().setRange(SHEET_NAME + "F" + (Integer.valueOf(product.getId()) + 1))
        .setValues(Arrays.asList(Arrays.asList(product.getAmount())));
  }

}
