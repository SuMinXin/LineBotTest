package com.linebot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.linebot.bean.Promotion;

@Service
public class ActiveService {

  private static List<Promotion> actives = new ArrayList<>();

  private Lock lock = new ReentrantLock();

  // HI
  public List<String> getPromotionList() {
    readData();
    return actives.stream().map(action -> {
      StringBuilder active = new StringBuilder();
      active.append("活動名稱：");
      active.append(action.getName());
      active.append('\n');
      active.append(getActiveDesc(action));
      return active.toString();
    }).collect(Collectors.toList());
  }

  // YO
  public List<Promotion> getPromotions() {
    readData();
    return actives;
  }

  // Sell
  public String sell(String id) throws Exception {
    // 數量 -1
    lock.lock();
    String response = null;
    try {
      Promotion action =
          actives.stream().filter(o -> id.equals(o.getId())).findFirst().orElse(null);
      if (action == null) {
        throw new Exception("Active Not Exist");
      }
      response = action.getName();
      if (action.getAmount() > 0) {
        action.setAmount(action.getAmount() - 1);
      } else {
        throw new Exception("Sold Out");
      }
    } finally {
      lock.unlock();
    }
    return response;
  }

  public void resetData() {
    actives.clear();
    readData();
  }

  private void readData() {
    if (actives.isEmpty()) {
      // https://docs.google.com/spreadsheets/d/1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s/edit#gid=0
      String spreadsheetId = "1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s";
      String range = "Action!A2:H";
      List<List<Object>> response =
          GoogleSheetService.getInstance().readGoogleSheet(spreadsheetId, range);
      if (!response.isEmpty()) {
        actives.addAll(response.stream().map(this::toPromotion).collect(Collectors.toList()));
      }
    }
  }

  private Promotion toPromotion(List<Object> action) {
    Promotion promotion = new Promotion();
    promotion.setId(action.get(0).toString());
    promotion.setName(action.get(1).toString());
    promotion.setDesc(action.get(2).toString());
    promotion.setPrice(Integer.valueOf(action.get(3).toString()));
    promotion.setUnit(action.get(4).toString());
    promotion.setAmount(Integer.valueOf(action.get(5).toString()));
    promotion.setImageUrl(action.get(6).toString());
    promotion.setActiveUrl(action.get(7).toString());
    return promotion;
  }

  public String getActiveDesc(Promotion promotion) {
    StringBuilder description = new StringBuilder();
    description.append(promotion.getDesc());
    description.append('\n');
    description.append(promotion.getPrice());
    description.append("/");
    description.append(promotion.getUnit());
    description.append('\n');
    description.append("剩餘數量：").append(promotion.getAmount());
    return description.toString();
  }

}
