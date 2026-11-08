package com.lab.lab_equipment_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**", "/error").permitAll()
                .requestMatchers("/student/loans", "/student/loans/**", "/student/notifications", "/student/notifications/**").hasRole("STUDENT")
                .requestMatchers("/equipment", "/equipment/**", "/api/equipment", "/api/equipment/**", "/api/devices", "/api/devices/**", "/api/categories", "/api/categories/**").hasAnyRole("STAFF", "ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/student/equipment", false).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
            .build();
    }
}
