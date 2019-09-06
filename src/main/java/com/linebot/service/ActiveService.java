package com.linebot.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.linebot.bean.Promotion;

@Service
public class ActiveService {

  private static List<Promotion> actives = new ArrayList<>();

  private Lock lock = new ReentrantLock();

  private GoogleSheetService sheetService = GoogleSheetService.getInstance();

  private static final String SHEET_ID = "1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s";

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
    updateSheet();
    actives.clear();
    readData();
  }

  private void readData() {
    if (actives.isEmpty()) {
      // https://docs.google.com/spreadsheets/d/1qenyxoIhzbHK-09nVxnVpDBlp3LepvQ7ALmXZKzPV7s/edit#gid=0
      String range = "Action!A2:H";
      List<List<Object>> response = sheetService.readGoogleSheet(SHEET_ID, range);
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

  public void updateSheet() {
    List<ValueRange> data = actives.stream().map(active -> {
      String range = "Action2!F" + (Integer.valueOf(active.getId()) + 1);
      return new ValueRange().setRange(range)
          .setValues(Arrays.asList(Arrays.asList(active.getAmount())));
    }).collect(Collectors.toList());

    sheetService.updateGoogleSheet(SHEET_ID, data);
  }

}
