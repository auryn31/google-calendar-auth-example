package com.example.googleauth.controller;

import com.example.googleauth.configuration.GoogleConfiguration;
import com.example.googleauth.service.GoogleCalendarService;
import com.example.googleauth.service.StateService;
import com.google.api.services.calendar.CalendarScopes;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class RouteController {

    private final GoogleConfiguration configuration;
    private final StateService state;
    private final GoogleCalendarService calendarService;

    @GetMapping("/")
    public ModelAndView index(Map<String, Object> model) {
        if (state.token.isPresent()) {
            val entriesOpt = calendarService.list(state.token.get());
            if(entriesOpt.isEmpty()) {
                model.put("error","Could not load calendar entries");
                return new ModelAndView("error", model);
            }
            val entries = entriesOpt.get();
            model.put("entries", entries);
            return new ModelAndView("index", model);
        } else {
            val googleUrl = String.format("https://accounts.google.com/o/oauth2/auth?client_id=%s&redirect_uri=%s&scope=%s&response_type=code&access_type=offline"
                    , configuration.getClientId()
                    , configuration.getRedirectUri()
                    , CalendarScopes.CALENDAR);
            model.put("googleUrl", googleUrl);
            return new ModelAndView("login", model);
        }
    }

    @GetMapping("/token")
    public ModelAndView token(Map<String, Object> model,@RequestParam String code) {
        val tokenOpt = calendarService.authorize(code);
        if(tokenOpt.isEmpty()) {
            model.put("error","There is no token available");
            return new ModelAndView("error", model);
        }
        val token = tokenOpt.get();
        state.token = Optional.of(token);

        return new ModelAndView("redirect:/");
    }
}
