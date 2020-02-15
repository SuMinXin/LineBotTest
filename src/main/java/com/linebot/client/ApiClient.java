package com.linebot.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.linebot.utils.JsonUtils;

@Component
public final class ApiClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

	private static final String API_URL = "https://core.api.test.com/";

	public String post(String action, Object request) {
		SSLConnectionSocketFactory sslsf = null;
		try {
			HttpPost httpPost = new HttpPost(API_URL + action);
			httpPost.addHeader("Content-Type", "application/json");
			httpPost.setEntity(new StringEntity(JsonUtils.objToString(request), StandardCharsets.UTF_8));
			try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
				CloseableHttpResponse response = httpClient.execute(httpPost);
				try {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						return EntityUtils.toString(entity);
					}
				} finally {
					response.close();
				}
			} catch (IOException e) {
				LOGGER.error("CloseableHttpClient-IOException:{}", e.getLocalizedMessage());
			}
		} catch (Exception e) {
			LOGGER.error("HttpPost-Exception:{}", e.getLocalizedMessage());
		}
		return "";
	}
}
