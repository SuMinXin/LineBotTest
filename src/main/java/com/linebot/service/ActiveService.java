package com.linebot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.linebot.bean.Product;

@Service
public class ActiveService extends SheetProductService {

  @Autowired
  private OrderService orderService;

  private Lock lock = new ReentrantLock();

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

  // ---------------------------- 購買商品 ----------------------------
  public Product sell(String id) throws Exception {
    // 數量 -1
    lock.lock();
    Product product = null;
    try {
      product = productsMap.get(id);
      if (product == null) {
        throw new Exception("Active Not Exist");
      }
      if (product.getAmount() > 0) {
        product.setAmount(product.getAmount() - 1);
        updateProduct(id);
      } else {
        // 結算
        orderService.transferOrders(id);
        throw new Exception("Sold Out");
      }
    } finally {
      lock.unlock();
    }
    return product;
  }

  public Product getProduct(String itemId) {
    return productsMap.get(itemId);
  }

  // ---------------------------- Product Function ----------------------------

  public void resetData() {
    resetProduct();
  }

  private void readData() {
    if (productsMap.isEmpty()) {
      getProduct();
    } else if (isExpire()) {
      resetProduct();
    }
  }

  private boolean isExpire() {
    long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - prevResetTime);
    if (prevResetTime <= 0 || minutes > exprireTime) {
      return true;
    }
    return false;
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
}
