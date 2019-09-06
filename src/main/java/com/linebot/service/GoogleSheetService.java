package com.linebot.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.linebot.utils.GoogleAuthorizeUtil;

/**
 * https://developers.google.com/sheets/api/quickstart/java 1. 建立 Google API Console 及註冊
 * https://console.developers.google.com/flows/enableapi?apiid=sheets.googleapis.com 2. 新增憑證
 *
 */
public final class GoogleSheetService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleSheetService.class);

  public static final String APPLICATION_NAME = "line-bot-testing";

  private static GoogleSheetService instance = null;

  private Sheets sheets;

  private GoogleSheetService(Sheets sheets) {
    this.sheets = sheets;
  }

  public static synchronized GoogleSheetService getInstance() {
    if (instance == null) {
      instance = new GoogleSheetService(buildSheets());
    }
    return instance;
  }

  private static Sheets buildSheets() {
    try {
      return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
          JacksonFactory.getDefaultInstance(), GoogleAuthorizeUtil.authorize())
              .setApplicationName(APPLICATION_NAME).build();
    } catch (IOException | GeneralSecurityException e) {
      LOGGER.error("Build Sheets Fail.", e);
    }
    return null;
  }

  public List<List<Object>> readGoogleSheet(String spreadSheetId, String range) {
    try {
      // Build a new authorized API client service.
      ValueRange response = sheets.spreadsheets().values().get(spreadSheetId, range).execute();
      List<List<Object>> values = response.getValues();
      if (values == null || values.isEmpty()) {
        LOGGER.info("No data found.");
      } else {
        return response.getValues();
      }
    } catch (IOException e) {
      LOGGER.error("Read Google Sheet Fail.", e);
    }
    return Collections.emptyList();
  }

  public void updateGoogleSheet(String spreadSheetId, List<ValueRange> data) {
    try {
      BatchUpdateValuesRequest batchBody =
          new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED").setData(data);
      sheets.spreadsheets().values().batchUpdate(spreadSheetId, batchBody).execute();
    } catch (IOException e) {
      LOGGER.error("Update Google Sheet Fail.", e);
    }
  }

}
