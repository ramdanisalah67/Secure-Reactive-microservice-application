package com.example.springsecuritywebflux.Exception;

public class AuthenticationFailedException extends  RuntimeException{
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
