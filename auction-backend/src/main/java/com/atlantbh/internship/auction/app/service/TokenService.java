package com.atlantbh.internship.auction.app.service;

import com.atlantbh.internship.auction.app.entity.Token;
import com.atlantbh.internship.auction.app.service.auth.UserAuthentication;

import java.time.Instant;
import java.util.Optional;

public interface TokenService {
    String generateToken(final UserAuthentication user, final Boolean rememberMe);

    Optional<Token> findToken(final String token);

    boolean hasExpired(final Instant currentTime, final Token token);

    void deleteUserTokens(final String principal);

    void deleteToken(final String token);
}
