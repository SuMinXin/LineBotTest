package com.linebot.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.linebot.bean.Product;

@Service
public class SheetProductService {

  // https://docs.google.com/spreadsheets/d/1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s/edit#gid=0
  @Value("${sheet-id.product}")
  private String productSheetId;

  private GoogleSheetService sheetService = GoogleSheetService.getInstance();
  private String range = SHEET_NAME + "A2:H";
  private static final String SHEET_NAME = "Action!";
  private static Map<String, Product> productMap = new ConcurrentHashMap<>();

  public void resetProductMap() {
    productMap.clear();
    setProductMap();
  }

  public Product getProduct(String productId) {
    return productMap.get(productId);
  }

  public Map<String, Product> getProductMap(boolean isExpired) {
    if (isExpired || productMap.isEmpty()) {
      setProductMap();
    }
    return productMap;
  }

  protected void setProductMap() {
    List<List<Object>> response = sheetService.readGoogleSheet(productSheetId, range);
    if (!response.isEmpty()) {
      productMap.putAll(response.stream().map(this::toProduct)
          .collect(Collectors.toMap(Product::getId, Function.identity())));
    }
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

  public void updateProduct(String id, int quantity) {
    Product product = productMap.get(id);
    product.setAmount(quantity);

    ValueRange data = toValueRange(product);
    sheetService.updateGoogleSheet(productSheetId, Arrays.asList(data));
  }

  private ValueRange toValueRange(Product product) {
    return new ValueRange().setRange(SHEET_NAME + "F" + (Integer.valueOf(product.getId()) + 1))
        .setValues(Arrays.asList(Arrays.asList(product.getAmount())));
  }

}
