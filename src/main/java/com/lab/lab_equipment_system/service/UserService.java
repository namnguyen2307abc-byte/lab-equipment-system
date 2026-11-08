package com.lab.lab_equipment_system.service;

import com.lab.lab_equipment_system.shared.entity.User;
import com.lab.lab_equipment_system.dto.UserResponse;
import com.lab.lab_equipment_system.mapper.StudentDtoMapper;
import com.lab.lab_equipment_system.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository users;
    public UserService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = findLogin(login);
        if (user == null) throw new UsernameNotFoundException("Không tìm thấy người dùng.");
        return new org.springframework.security.core.userdetails.User(user.getUsername() == null ? user.getEmail() : user.getUsername(),
            user.getPasswordHash(), user.isActive(), true, true, true,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }

    public UserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) return null;
        User user = findLogin(auth.getName());
        return user != null && user.isActive() ? StudentDtoMapper.toUserResponse(user) : null;
    }

    private User findLogin(String login) {
        return users.findByUsernameIgnoreCase(login).or(() -> users.findByEmailIgnoreCase(login)).orElse(null);
    }
}
