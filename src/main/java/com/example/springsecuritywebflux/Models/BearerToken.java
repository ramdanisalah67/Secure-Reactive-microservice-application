package com.example.springsecuritywebflux.Models;

import com.example.springsecuritywebflux.Service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;


public class BearerToken extends AbstractAuthenticationToken {
    private String token ;


    public BearerToken(String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;

    }
    @Override
    public String getCredentials() {
        return token;
    }

    @Override
    public String getPrincipal() {
        return token;
    }


}
