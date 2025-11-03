package com.fix4home.fix4home.exception;

import com.fix4home.fix4home.model.enums.UserStatus;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when trying to access an inactive account
 */
public class AccountNotActiveException extends BaseBusinessException {
    
    public AccountNotActiveException(String message) {
        super(
            "ACCOUNT_NOT_ACTIVE",
            message,
            "Your account is not active. Please contact support.",
            HttpStatus.FORBIDDEN
        );
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create exception for account with specific status
     */
    public static AccountNotActiveException withStatus(UserStatus status) {
        return new AccountNotActiveException(
            "Account is not active. Current status: " + status
        );
    }

    /**
     * Create exception for inactive account
     */
    public static AccountNotActiveException inactive() {
        return new AccountNotActiveException("Account is not active");
    }

    /**
     * Create exception for suspended account
     */
    public static AccountNotActiveException suspended() {
        return new AccountNotActiveException("Account has been suspended");
    }

    /**
     * Create exception for pending approval (backward compatibility)
     */
    public static AccountNotActiveException pendingApproval() {
        return new AccountNotActiveException("Account is pending approval. Please wait for admin approval.");
    }
} 