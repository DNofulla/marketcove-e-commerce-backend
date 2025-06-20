package com.dnofulla.marketcove.backend_api.config;

import com.dnofulla.marketcove.backend_api.security.JwtAuthenticationFilter;
import com.dnofulla.marketcove.backend_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for MarketCove E-Commerce backend
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Configure security filter chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Swagger/OpenAPI Documentation endpoints
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs.yaml").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .requestMatchers("/error").permitAll()

                        // Public storefront and item endpoints (read-only)
                        .requestMatchers(HttpMethod.GET, "/api/storefronts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/storefronts/{storefrontId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/storefronts/slug/{slug}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/storefronts/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/storefronts/featured").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/storefronts/top-rated").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/{itemId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/sku/{sku}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/storefront/{storefrontId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/category/{category}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/price-range").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/featured").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/on-sale").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/best-selling").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/recent").permitAll()

                        // Storefront and item management endpoints (business owners and sellers only)
                        .requestMatchers("/api/storefronts/my-storefronts").hasAnyRole("BUSINESS_OWNER", "SELLER")
                        .requestMatchers(HttpMethod.POST, "/api/storefronts").hasAnyRole("BUSINESS_OWNER", "SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/storefronts/**").hasAnyRole("BUSINESS_OWNER", "SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/storefronts/**")
                        .hasAnyRole("BUSINESS_OWNER", "SELLER")
                        .requestMatchers("/api/items/my-items").hasAnyRole("BUSINESS_OWNER", "SELLER")
                        .requestMatchers("/api/items/low-stock").hasAnyRole("BUSINESS_OWNER", "SELLER")
                        .requestMatchers(HttpMethod.POST, "/api/items/**").hasAnyRole("BUSINESS_OWNER", "SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/items/**").hasAnyRole("BUSINESS_OWNER", "SELLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/items/**").hasAnyRole("BUSINESS_OWNER", "SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/items/**").hasAnyRole("BUSINESS_OWNER", "SELLER")

                        // Customer endpoints
                        .requestMatchers("/api/customer/**").hasRole("CUSTOMER")

                        // Seller endpoints
                        .requestMatchers("/api/seller/**").hasRole("SELLER")

                        // Business owner endpoints
                        .requestMatchers("/api/business/**").hasRole("BUSINESS_OWNER")

                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Common authenticated user endpoints
                        .requestMatchers("/api/user/**").hasAnyRole("CUSTOMER", "SELLER", "BUSINESS_OWNER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}