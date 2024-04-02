package com.example.springsecuritywebflux.Service;


import com.example.springsecuritywebflux.Exception.AuthenticationFailedException;
import com.example.springsecuritywebflux.Models.User;
import com.example.springsecuritywebflux.Repositories.UserRepository;
import com.example.springsecuritywebflux.dto.AuthRequest;
import com.example.springsecuritywebflux.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    //private final ReactiveAuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public Mono<ResponseEntity<AuthResponse>> auth(AuthRequest request) {

        Mono<User> foundUser = repository.findByEmail(request.getEmail()).defaultIfEmpty( new User() {

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

        })  ;

        return   foundUser.map(u->{
            if(u.getEmail()==null){
                return  ResponseEntity.status(404).body(new AuthResponse("not token","email not exist"));
            }
            else{
                if(passwordEncoder.matches(request.getPassword(),u.getPassword())){

                    return
                            ResponseEntity.ok(
                                    new AuthResponse(jwtService.generateToken(request.getEmail()),"200")
                            );

                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse("email or password wrong","404"));
            }

        });
    }


    public Mono<String> register(User user){
        String email = user.getEmail();

        return repository.existsByEmail(email).
                flatMap((alreadyExist->{
                            if(alreadyExist) return Mono.just("User Already exist in Database") ;

                            else {
                                user.setPassword(passwordEncoder.encode(user.getPassword()));
                                return repository.save(user)
                                        .thenReturn("User registered!!");
                            }
                        }


                        )
                );
    }

}
