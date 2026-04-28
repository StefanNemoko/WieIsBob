package com.example.wieisbob.auth;

import com.example.wieisbob.exception.UnauthorizedException;
import com.example.wieisbob.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new UnauthorizedException("Not authenticated.");
        }

        return (User) authentication.getPrincipal();
    }
}
