package com.fix4home.fix4home.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends BaseBusinessException {
    
    public TokenRefreshException(String token, String message) {
        super("TOKEN_REFRESH_ERROR", 
              "Failed for [" + token + "]: " + message,
              message,
              HttpStatus.FORBIDDEN);
    }

    public TokenRefreshException(String message) {
        super("TOKEN_REFRESH_ERROR",
              message,
              message,
              HttpStatus.FORBIDDEN);
    }
} 