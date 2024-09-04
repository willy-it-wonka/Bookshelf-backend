package com.mybooks.bookshelf.security;

import com.mybooks.bookshelf.exception.JwtAuthenticationException;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JsonWebTokenFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ERROR_LOG = "Authentication error: ";

    private final JsonWebToken jsonWebToken;
    private final UserService userService;

    public JsonWebTokenFilter(JsonWebToken jsonWebToken, UserService userService) {
        this.jsonWebToken = jsonWebToken;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        String userId = null;
        String jwt = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
                jwt = authorizationHeader.substring(7);
                userId = jsonWebToken.extractUserId(jwt);
            }

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.loadUserById(Long.parseLong(userId));

                if (jsonWebToken.isTokenValid(jwt, user)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtAuthenticationException e) {
            log.error(ERROR_LOG + e.getMessage(), e);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            response.getWriter().flush();
        }
    }

}
