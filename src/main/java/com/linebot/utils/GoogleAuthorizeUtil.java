package com.linebot.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.SheetsScopes;

@Component
public final class GoogleAuthorizeUtil {

  private GoogleAuthorizeUtil() {}

  private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);

  public static Credential authorize() throws IOException {
    return GoogleCredential
        .fromStream(GoogleAuthorizeUtil.class.getResourceAsStream("/credentials.json"))
        .createScoped(SCOPES);
  }

}
