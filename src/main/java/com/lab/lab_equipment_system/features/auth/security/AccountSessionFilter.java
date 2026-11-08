package com.lab.lab_equipment_system.features.auth.security;

import com.lab.lab_equipment_system.features.auth.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

/** Rechecks persisted account state before any protected controller can run. */
public final class AccountSessionFilter extends OncePerRequestFilter {
    private final UserService users;
    public AccountSessionFilter(UserService users) { this.users = users; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/login") || path.equals("/error") || path.startsWith("/css/")
            || path.startsWith("/js/") || path.startsWith("/images/") || path.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain chain) throws IOException, ServletException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            boolean valid = false;
            if (authentication.getPrincipal() instanceof AccountPrincipal previous) {
                try {
                    var current = users.loadUserByUsername(authentication.getName());
                    // Security 7 also attaches authentication-factor authorities at login.
                    // Compare application roles while preserving those factor authorities.
                    var roles = authentication.getAuthorities().stream()
                        .filter(a -> a.getAuthority().startsWith("ROLE_"))
                        .collect(java.util.stream.Collectors.toSet());
                    valid = current instanceof AccountPrincipal account && previous.matches(account)
                        && roles.equals(new java.util.HashSet<>(account.getAuthorities()));
                } catch (UsernameNotFoundException ignored) { /* Deleted accounts lose their sessions. */ }
            }
            if (!valid) { reject(request, response); return; }
        }
        chain.doFilter(request, response);
    }

    public static void reject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        if (request.getMethod().equals("GET") || request.getMethod().equals("HEAD")) {
            response.sendRedirect(request.getContextPath() + "/login?expired");
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Phiên đăng nhập đã thay đổi. Vui lòng đăng nhập lại.");
        }
    }
}
