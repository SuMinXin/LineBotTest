package com.linebot.appengine.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@Service
@LineMessageHandler
public class UtilService {

	@Value("${line.bot.channelToken}")
	protected String channelToken;

	protected LineMessagingClient lineMessageClient;
}
