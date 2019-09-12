package com.linebot.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.linebot.client.RedisClient;
import com.linebot.enums.UserAction;
import com.linebot.utils.JsonUtils;

@Service
public class UserActiveService {

  @Autowired
  private RedisClient redisClient;

  // private static final Logger LOGGER = LoggerFactory.getLogger(UserActiveService.class);

  // ---------------------------- 行為紀錄 ----------------------------
  public void userActionLog(UserAction action, String userID, String message, String active) {
    ActiveLog log = null;
    switch (action) {
      case MY_ORDER:
      case COUPON:
      case NEAR_STORE:
      case NEW_ACTIVE:
      case BUY_PRODUCT:
        log = commonActiveLog(action, userID);
        break;
      default:
        log = commonActiveLog(action, userID);
        log.setMessage(message);
        break;
    }
    redisClient.saveHashToken(getHashKey(action.name()), getKey(userID, active),
        JsonUtils.objToString(log));
  }

  // 產出統計結果
  public String allAction() {
    String pattern = "UserAction:";
    Map<String, EventDetail> result = new HashMap<>();
    Map<String, List<String>> datas = redisClient.retrieveByPattern(pattern.concat("*"));
    for (Entry<String, List<String>> data : datas.entrySet()) {
      String key = data.getKey().replace(pattern, "");
      List<String> value = data.getValue();
      result.put(key, setEventDetail(value));
    }
    return JsonUtils.objToString(result);
  }

  private EventDetail setEventDetail(List<String> datas) {
    EventDetail result = new EventDetail();
    int total = 0;
    for (String data : datas) {
      ActiveLog log = JsonUtils.fromJson(data, ActiveLog.class);
      if (log != null && log.actTime != null) {
        result.setTotal(++total);
        result.setTime(log.actTime);
      }
    }
    return result;
  }

  public String product(String id) {
    return "";
  }

  private String getHashKey(String action) {
    return "UserAction".concat(":").concat(action);
  }

  private String getKey(String userID, String active) {
    String result = userID.concat("::").concat(LocalDateTime.now().toString());
    if (!"".equals(active)) {
      result = result.concat("::").concat(active);
    }
    return result;
  }



  private ActiveLog commonActiveLog(UserAction action, String userID) {
    ActiveLog log = new ActiveLog();
    log.setAction(action);
    log.setUser(userID);
    log.setActTime(LocalDateTime.now());
    return log;
  }

  static class ActiveLog {
    private String user;
    private UserAction action;
    private LocalDateTime actTime;
    private String message;

    public String getUser() {
      return user;
    }

    public void setUser(String user) {
      this.user = user;
    }

    public UserAction getAction() {
      return action;
    }

    public void setAction(UserAction action) {
      this.action = action;
    }

    public LocalDateTime getActTime() {
      return actTime;
    }

    public void setActTime(LocalDateTime actTime) {
      this.actTime = actTime;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }

  // --------------------------------- 報表格式 ---------------------------------
  class EventDetail {
    private int total;
    private Map<String, Integer> time;

    public int getTotal() {
      return total;
    }

    public void setTotal(int total) {
      this.total = total;
    }

    public Map<String, Integer> getTime() {
      return time;
    }

    public void setTime(LocalDateTime time) {
      String range = String.valueOf(time.getHour());
      if (this.time == null) {
        this.time = new HashMap<>();
        this.time.put(range, 1);
      } else if (this.time.get(range) == null) {
        this.time.put(range, 1);
      } else {
        this.time.put(range, this.time.get(range) + 1);
      }
    }
  }

  class ActTime {
    private String range;
    private String amount;

    public String getRange() {
      return range;
    }

    public void setRange(String range) {
      this.range = range;
    }

    public String getAmount() {
      return amount;
    }

    public void setAmount(String amount) {
      this.amount = amount;
    }
  }
}
