package com.example.googleauth.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Data
public class StateService {
    public Optional<TokenResponse> token = Optional.empty();
}
