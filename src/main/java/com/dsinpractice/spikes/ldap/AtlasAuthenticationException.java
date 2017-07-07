package com.dsinpractice.spikes.ldap;


import org.springframework.security.core.AuthenticationException;

class AtlasAuthenticationException extends AuthenticationException {

    public AtlasAuthenticationException(String message) {
        super(message);
    }

    public AtlasAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}