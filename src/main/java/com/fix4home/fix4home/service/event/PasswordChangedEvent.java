package com.fix4home.fix4home.service.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Event fired when a user successfully changes their password.
 */
@Data
@AllArgsConstructor
public class PasswordChangedEvent {

    private Long userId;
    private String username;
}


