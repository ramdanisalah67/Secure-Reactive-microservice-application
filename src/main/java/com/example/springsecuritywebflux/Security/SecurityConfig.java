
package com.example.springsecuritywebflux.Security;


import com.example.springsecuritywebflux.Models.User;
import com.example.springsecuritywebflux.Repositories.UserRepository;
import com.example.springsecuritywebflux.Service.AuthConvert;
import com.example.springsecuritywebflux.Service.AuthManager;
import com.example.springsecuritywebflux.Service.JwtService;
import com.example.springsecuritywebflux.Service.UserDetailsServiceImpl;
import io.micrometer.core.instrument.binder.http.HttpServletRequestTagsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@Configuration
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Bean
    SecurityWebFilterChain webFluxSecurityFilterChain(ServerHttpSecurity http, AuthConvert authConvert, AuthManager authManager) {
        AuthenticationWebFilter jwtFilter =  new AuthenticationWebFilter(authManager);
        jwtFilter.setServerAuthenticationConverter(authConvert);

        http.csrf(csrf -> csrf.disable())
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/welcome","/register","/auth").permitAll()
                        .pathMatchers("/home").hasAuthority("USER")

                        .anyExchange().authenticated()
                )

                .addFilterAt(jwtFilter,SecurityWebFiltersOrder.AUTHENTICATION);




        return http.build();
    }


    //@Bean
    public ReactiveAuthenticationManager authenticationManager() {

        return authentication -> {
            System.out.println("hahah");
            return userDetailsService.findByUsername(authentication.getName())
                    .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                    .flatMap(userDetails -> {
                        if (passwordEncoder.matches(authentication.getCredentials().toString(), userDetails.getPassword())) {
                            return Mono.just(new UsernamePasswordAuthenticationToken(userDetails,
                                    null, userDetails.getAuthorities()));
                        } else {
                            return Mono.error(new UsernameNotFoundException("Invalid credentials"));
                        }
                    });
        };
        }



}
