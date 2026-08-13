package com.starshop.exception;

import org.apache.shiro.authc.AuthenticationException;

public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

}
