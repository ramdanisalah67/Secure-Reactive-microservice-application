package com.example.springsecuritywebflux.Service;

import com.example.springsecuritywebflux.Models.BearerToken;
import com.example.springsecuritywebflux.Models.User;
import com.example.springsecuritywebflux.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class AuthManager implements ReactiveAuthenticationManager {
    private  final JwtService jwtService;
    private final ReactiveUserDetailsService reactiveUserDetailsService;
    private final UserRepository repository;
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
            return Mono.justOrEmpty(
                    authentication
            ).cast(BearerToken.class).flatMap(auth ->{
                String email = jwtService.extractUsername(auth.getCredentials());
                Mono<UserDetails> foundUser = reactiveUserDetailsService.findByUsername(email).defaultIfEmpty(new UserDetails() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return null;
                    }

                    @Override
                    public String getPassword() {
                        return null;
                    }

                    @Override
                    public String getUsername() {
                        return null;
                    }

                    @Override
                    public boolean isAccountNonExpired() {
                        return false;
                    }

                    @Override
                    public boolean isAccountNonLocked() {
                        return false;
                    }

                    @Override
                    public boolean isCredentialsNonExpired() {
                        return false;
                    }

                    @Override
                    public boolean isEnabled() {
                        return false;
                    }
                });
              Mono<Authentication> authenticatedUser =  foundUser.flatMap(u->{
                    if(jwtService.validateToken(auth.getCredentials(),u)){

                        return repository.findByEmail(u.getUsername()).map(e->
                                        new UsernamePasswordAuthenticationToken(
                                                u.getUsername(),u.getPassword(),e.getAuthorities())


                        );}

                    if (!jwtService.validateToken(auth.getCredentials(),u)) return Mono.error(new IllegalArgumentException("invalid token"));

                    if(u.getUsername() == null ) Mono.error(new IllegalArgumentException("email not found in token"));


                  return Mono.error(new IllegalArgumentException("something wrong"));
              });
                return authenticatedUser;

            });
    }







}
