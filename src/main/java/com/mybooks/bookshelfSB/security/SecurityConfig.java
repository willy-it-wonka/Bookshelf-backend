package com.mybooks.bookshelfSB.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfig authenticationConfig;

    @Autowired
    public SecurityConfig(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(POST, "/api/register").permitAll()
                        .requestMatchers(GET, "/api/register/confirm**").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationConfig.authenticationProvider());
        return http.build();
    }

}
