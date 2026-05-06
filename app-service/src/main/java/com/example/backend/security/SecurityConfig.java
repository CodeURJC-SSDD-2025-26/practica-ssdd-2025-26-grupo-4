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

    // Bean necesario para hacer el login en la API REST
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // =======================================================
    // CONFIGURACIÓN 1: PARA LA API REST (JWT y Stateless)
    // =======================================================
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**") // Solo aplica a lo que empiece por /api/
                .csrf(csrf -> csrf.disable()) // Desactivar CSRF para REST es vital
                .cors(cors -> cors.disable()) // Por ahora desactivamos CORS para evitar jaleos
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ¡Sin
                                                                                                              // sesiones!
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll() // Login libre
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                        .anyRequest().authenticated() // Todo lo demás con Token
                )
                // ESTO ES CLAVE: Metemos el filtro JWT antes del de usuario y contraseña
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =======================================================
    // CONFIGURACIÓN 2: PARA LA WEB MUSTACHE (Form Login clásico)
    // =======================================================
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/assets/**", "/js/**", "/product/**", "/user/**").permitAll()
                .requestMatchers("/", "/index", "/item-detail", "/search", "/search-result", "/user_registration",
                        "/user-registration", "/login", "/shopping-cart")
                .permitAll()
                .requestMatchers("/create-review", "/payment", "/profile/**").hasAnyRole("USER", "ADMIN")
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