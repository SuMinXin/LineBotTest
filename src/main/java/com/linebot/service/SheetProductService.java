package com.linebot.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.linebot.bean.Product;

@Service
public class SheetProductService {

  @Value("${sheet-id.product}")
  private String productSheetId;// = "1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s";

  private GoogleSheetService sheetService = GoogleSheetService.getInstance();
  private String range = SHEET_NAME + "A2:H";
  private static final String SHEET_NAME = "Action!";
  protected static Map<String, Product> productsMap = new HashMap<>();

  @Async("threadPoolTaskExecutor")
  protected void resetProduct() {
    // https://docs.google.com/spreadsheets/d/1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s/edit#gid=0
    List<List<Object>> response = GoogleSheetService.getInstance().readGoogleSheet(productSheetId, range);
    if (!response.isEmpty()) {
      List<Product> acts = response.stream().map(this::toProduct).collect(Collectors.toList());
      for (Product act : acts) {
        if (!productsMap.containsKey(act.getId())) {
          productsMap.put(act.getId(), act);
        }
      }
    }
  }

  protected Map<String, Product> getProduct() {
    // https://docs.google.com/spreadsheets/d/1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s/edit#gid=0
    List<List<Object>> response = GoogleSheetService.getInstance().readGoogleSheet(productSheetId, range);
    if (!response.isEmpty()) {
      List<Product> acts = response.stream().map(this::toProduct).collect(Collectors.toList());
      for (Product act : acts) {
        if (!productsMap.containsKey(act.getId())) {
          productsMap.put(act.getId(), act);
        }
      }
    }
    return productsMap;
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

  protected void updateProduct(String id) {
    Product product = productsMap.get(id);
    ValueRange data = toValueRange(product);
    sheetService.updateGoogleSheet(productSheetId, Arrays.asList(data));
  }

  private ValueRange toValueRange(Product product) {
    return new ValueRange().setRange(SHEET_NAME + "F" + (Integer.valueOf(product.getId()) + 1))
        .setValues(Arrays.asList(Arrays.asList(product.getAmount())));
  }

}
