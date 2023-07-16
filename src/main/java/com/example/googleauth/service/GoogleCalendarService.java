package com.example.googleauth.service;

import com.example.googleauth.configuration.GoogleConfiguration;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import lombok.AllArgsConstructor;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class GoogleCalendarService {
    private final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);
    private final GoogleConfiguration config;

    public Optional<TokenResponse> authorize(String code) {
        val scopes = new ArrayList<String>();
        scopes.add("https://www.googleapis.com/auth/calendar");
        try {
            return Optional.of(new AuthorizationCodeTokenRequest(new NetHttpTransport(), new GsonFactory(),
                    new GenericUrl("https://oauth2.googleapis.com/token"), code)
                    .setRedirectUri(config.getRedirectUri())
                    .setCode(code)
                    .setScopes(scopes)
                    .set("client_id",config.getClientId())
                    .set("client_secret",config.getClientSecret())
                    .set("project_id",config.getProjectId())
                    .set("access_type", "offline")
                    .set("prompt", "consent")
                    .execute());
        } catch (IOException e) {
            logger.error("Error while logging in to google",e);
            return Optional.empty();
        }
    }

    public Optional<List<Event>> list(TokenResponse tokenResponse) {
        try {

            val calendar = new Calendar.Builder(new NetHttpTransport(), new GsonFactory(), null).setApplicationName(config.getProjectId()).setHttpRequestInitializer(request -> {
                request.getHeaders().setAuthorization("Bearer " + tokenResponse.getAccessToken());
                request.getHeaders().setContentType("application/json");
            }).build();
            val events = calendar.events().list("primary").setMaxResults(20).setTimeMin(new DateTime(System.currentTimeMillis())).setSingleEvents(true).setOrderBy("startTime").execute();
            return Optional.of(events.getItems());
        } catch (Exception e) {
            logger.error("Could not load calendar entries", e);
            return Optional.empty();
        }
    }

}
