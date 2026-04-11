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
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() //PÚBLICO
                .requestMatchers("/", "/index", "/item/detail/**", "/login", "/user_registration","/search/results").permitAll()//PUBLICO
                .requestMatchers("/profile", "/shopping/cart/**", "/payment/**", "/payment_correct/**", "/create/review/**").hasAnyRole("USER", "ADMIN") //USUARIOS Y ADMINS
                .requestMatchers("/admin/**", "/item/create", "/item/edit/**").hasRole("ADMIN") //SOLO ADMINS
                .anyRequest().authenticated()
        );

        http.formLogin(formLogin -> formLogin
                .loginPage("/login") 
                .loginProcessingUrl("/login") 
                .defaultSuccessUrl("/index", true) 
                .failureUrl("/login?error=true")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/index")
                .permitAll()
        );

        return http.build();
    }

}