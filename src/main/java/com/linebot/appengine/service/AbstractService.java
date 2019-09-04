package com.linebot.appengine.service;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.spring.boot.LineBotProperties;

public class AbstractService {

	@Autowired
	protected LineBotProperties lineBotProperties;

	@Autowired
	protected LineMessagingClient client;

//  @PostConstruct
//  private void init() {
//    client = LineMessagingClient.builder(lineBotProperties.getChannelToken()).build();
//  }

}
