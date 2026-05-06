package com.example.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService userDetailsService;

        @Autowired
        private JwtRequestFilter jwtRequestFilter;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // Bean required for REST API login authentication
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        // =======================================================
        // CONFIGURATION 1: REST API (JWT and Stateless)
        // =======================================================
        @Bean
        @Order(1)
        public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/api/**")
                                .csrf(csrf -> csrf.disable()) // CSRF disabled for stateless REST
                                .cors(cors -> cors.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api-docs",
                                                                "/api-docs.yaml",
                                                                "/api-docs/**")
                                                .permitAll()
                                                .requestMatchers("/api/v1/auth/**", "/api/v1/users/register")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/v1/products/**")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/v1/reviews/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                // Add JWT filter before the username/password authentication filter
                                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // =======================================================
        // CONFIGURATION 2: WEB (Mustache form-based login)
        // =======================================================
        @Bean
        @Order(2)
        public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(authorize -> authorize

                                .requestMatchers(
                                                "/api-docs",
                                                "/api-docs.yaml",
                                                "/api-docs/**",
                                                "/api-docs.html",
                                                "/v3/api-docs",
                                                "/v3/api-docs.yaml",
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html")
                                .permitAll()

                                .requestMatchers("/css/**", "/assets/**", "/js/**", "/product/**", "/user/**")
                                .permitAll()
                                .requestMatchers("/", "/index", "/item-detail", "/search", "/search-result",
                                                "/user_registration",
                                                "/user-registration", "/login", "/shopping-cart")
                                .permitAll()
                                .requestMatchers("/create-review", "/payment", "/profile/**")
                                .hasAnyRole("USER", "ADMIN")
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated());

                http.userDetailsService(userDetailsService);

                http.formLogin(formLogin -> formLogin
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/", true)
                                .permitAll());

                http.exceptionHandling(exception -> exception
                                .accessDeniedPage("/error/403"));

                return http.build();
        }
}