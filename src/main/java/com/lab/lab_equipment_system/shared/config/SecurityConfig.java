package com.lab.lab_equipment_system.shared.config;

import com.lab.lab_equipment_system.features.auth.security.AccountSessionFilter;
import com.lab.lab_equipment_system.features.auth.service.UserService;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher sessionEvents() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            UserService users,
            SessionRegistry sessions) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**", "/error").permitAll()
                // Quản lý tài khoản và phân quyền: chỉ quản trị viên
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                // Chức năng của sinh viên
                .requestMatchers("/student/loans", "/student/loans/**", "/student/notifications", "/student/notifications/**").hasRole("STUDENT")
                .requestMatchers("/equipment", "/equipment/**").hasAnyRole("STAFF", "ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/student/equipment", false).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
            .sessionManagement(session -> session.maximumSessions(-1).sessionRegistry(sessions)
                .expiredSessionStrategy(event -> AccountSessionFilter
                    .reject(event.getRequest(), event.getResponse())))
            .addFilterBefore(new AccountSessionFilter(users),
                AuthorizationFilter.class)
            .build();
    }
}
