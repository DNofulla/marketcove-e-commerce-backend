package com.dnofulla.marketcove.backend_api.security;

import com.dnofulla.marketcove.backend_api.service.UserService;
import com.dnofulla.marketcove.backend_api.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter to validate JWT tokens on each request
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Skip JWT validation for certain endpoints
        if (shouldSkipFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = jwtUtil.extractTokenFromHeader(authHeader);
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            userEmail = jwtUtil.extractUsername(jwt);

            // If user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userService.loadUserByUsername(userEmail);

                // Validate token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Successfully authenticated user: {}", userEmail);
                } else {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Determine if the filter should be skipped for certain endpoints
     */
    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip authentication for public endpoints
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/") ||
                path.equals("/") ||
                path.startsWith("/health") ||
                path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/error");
    }
}