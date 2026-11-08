package com.lab.lab_equipment_system.features.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/** Keeps the credential revision after Spring erases the login password. */
public final class AccountPrincipal extends User {
    private static final long serialVersionUID = 1L;
    private final Long accountId;
    private final String credentialRevision;

    public AccountPrincipal(Long accountId, String username, String passwordHash, boolean active,
                            Collection<? extends GrantedAuthority> authorities) {
        super(username, passwordHash, active, true, true, true, authorities);
        this.accountId = accountId;
        this.credentialRevision = fingerprint(passwordHash);
    }

    public Long getAccountId() { return accountId; }

    public boolean matches(AccountPrincipal current) {
        return current.isEnabled() && accountId.equals(current.accountId)
            && getAuthorities().equals(current.getAuthorities())
            && credentialRevision.equals(current.credentialRevision);
    }

    private static String fingerprint(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
