package com.example.backend.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Encripta las contraseñas
    }

   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/assets/**", "/js/**", "/product/**", "/user/**").permitAll() // PÚBLICO
                .requestMatchers("/", "/index", "/item-detail", "/search","/search-result", "/user-registration", "/login", "/shopping-cart").permitAll() // PÚBLICO
                .requestMatchers("/create-review", "/payment", "/profile/**").hasAnyRole("USER", "ADMIN") // USUARIOS Y ADMINS
                .requestMatchers("/admin/**").hasRole("ADMIN") // SOLO ADMINS
                .anyRequest().authenticated()
        );

        // CONFIGURACIÓN DEL LOGIN 
        http.formLogin(formLogin -> formLogin
                .loginPage("/login") 
                .loginProcessingUrl("/login") 
                .defaultSuccessUrl("/", true) 
                .permitAll()
        );

        http.exceptionHandling(exception -> exception
                 .accessDeniedPage("/error/403")
        );

        return http.build();


    }

}