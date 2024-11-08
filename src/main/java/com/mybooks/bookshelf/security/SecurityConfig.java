package com.mybooks.bookshelf.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String LOGOUT_METHOD = "DELETE";
    private static final String LOGOUT_MESSAGE = "Logged out.";
    private static final String CORS_PATH_PATTERN = "/**";
    private static final List<String> CORS_ALLOWED_HEADERS = Arrays.asList("Authorization", "Content-Type", "Accept");
    private static final List<String> CORS_ALLOWED_METHODS = Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE");

    private final JsonWebTokenFilter jsonWebTokenFilter;

    @Value("${security.cors.allowed-origin}")
    private String corsAllowedOrigin;

    public SecurityConfig(JsonWebTokenFilter jsonWebTokenFilter) {
        this.jsonWebTokenFilter = jsonWebTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(POST, "/api/v1/users").permitAll()
                        .requestMatchers(GET, "/api/v1/users/confirmation**").permitAll()
                        .requestMatchers(GET, "/confirmation-success.html").permitAll()
                        .requestMatchers(GET, "/confirmation-error.html").permitAll()
                        .requestMatchers(POST, "/api/v1/users/session").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jsonWebTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/api/v1/users/session", LOGOUT_METHOD))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                            response.setContentType("text/plain");
                            response.getWriter().println(LOGOUT_MESSAGE);
                        })
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(corsAllowedOrigin);
        configuration.setAllowedHeaders(CORS_ALLOWED_HEADERS);
        configuration.setAllowedMethods(CORS_ALLOWED_METHODS);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(CORS_PATH_PATTERN, configuration);
        return source;
    }

}
