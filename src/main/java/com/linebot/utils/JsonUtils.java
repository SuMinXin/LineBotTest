package com.linebot.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public final class JsonUtils {

  private JsonUtils() {}

  public static <T> T fromJson(String jsonString, Class<T> type) {
    JSONObject jsonObject = JSONObject.parseObject(jsonString);
    return JSONObject.toJavaObject(jsonObject, type);
  }

  public static <T> List<T> fromJsonToList(String jsonString, Class<T> type) {
    JSONArray jsonArray = JSONArray.parseArray(jsonString);
    return jsonArray.toJavaList(type);
  }

  public static List<Object> jsonToList(JSONArray jsonArr) {
    List<Object> list = new ArrayList<>();
    for (Object obj : jsonArr) {
      if (obj instanceof JSONArray) {
        list.add(jsonToList((JSONArray) obj));
      } else if (obj instanceof JSONObject) {
        list.add(jsonToMap((JSONObject) obj));
      } else {
        list.add(obj);
      }
    }
    return list;
  }

  public static Map<String, Object> jsonToMap(String json) {
    JSONObject obj = JSONObject.parseObject(json);
    return jsonToMap(obj);
  }

  public static Map<String, Object> jsonToMap(JSONObject obj) {
    Set<?> set = obj.keySet();
    Map<String, Object> map = new HashMap<>(set.size());
    for (Entry<String, Object> entry : map.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof JSONArray) {
        map.put(entry.getKey(), jsonToList((JSONArray) value));
      } else if (value instanceof JSONObject) {
        map.put(entry.getKey(), jsonToMap((JSONObject) value));
      } else {
        map.put(entry.getKey(), entry.getValue());
      }
    }
    return map;
  }

  public static String objToString(Object obj) {
    return JSONObject.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect);
  }

}
