package com.linebot.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.linebot.bean.Product;

@Service
public class ActiveService {

  @Value("${actice.cache.expired-time}")
  private Integer expriredTime;

  @Autowired
  private OrderService orderService;

  @Autowired
  private SheetProductService productService;

  private Lock lock = new ReentrantLock();

  private static long prevResetTime = 0;

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

  /**
   * 取得商品清單
   * 
   * @param isSaleable 可售商品
   */
  public List<Product> getProducts(boolean isSaleable) {
    Collection<Product> products = productService.getProductMap(isExpire()).values();

    // 只回傳可賣商品
    if (isSaleable) {
      return products.stream().filter(act -> act.getAmount() > 0).collect(Collectors.toList());
    } else {
      return new ArrayList<>(products);
    }
  }

  public Product sellProduct(String productId) {
    lock.lock();
    try {
      Product product = getProduct(productId);

      int quantity = product.getAmount();

      // 已售完
      if (quantity <= 0) {
        throw new ActiveException("Sold Out");
      }

      // 扣量(一次只能買一組)
      quantity--;

      // 更新剩餘數量
      productService.updateProduct(productId, quantity);

      // 結算
      if (quantity <= 0) {
        orderService.transferOrders(productId);
      }

      return product;
    } finally {
      lock.unlock();
    }
  }

  public Product getProduct(String productId) {
    Product product = productService.getProduct(productId);
    // 查無商品
    if (product == null) {
      throw new ActiveException("Active Not Exist");
    }
    return product;
  }

  public void resetData() {
    setPrevResetTime();
    productService.resetProductMap();
  }

  private boolean isExpire() {
    long now = System.currentTimeMillis();
    long remainingTime = now - prevResetTime;
    if (TimeUnit.MILLISECONDS.toMinutes(remainingTime) >= expriredTime) {
      setPrevResetTime();
      return true;
    }
    return false;
  }

  private void setPrevResetTime() {
    prevResetTime = System.currentTimeMillis();
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

}
