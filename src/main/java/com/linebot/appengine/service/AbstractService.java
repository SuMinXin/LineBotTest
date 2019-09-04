package com.linebot.appengine.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.spring.boot.LineBotProperties;

public class AbstractService {

  @Autowired
  protected LineBotProperties lineBotProperties;

  @Autowired
  protected LineMessagingClient client;

}
