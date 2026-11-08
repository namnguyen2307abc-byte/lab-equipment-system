package com.lab.lab_equipment_system.features.auth.service;

import com.lab.lab_equipment_system.features.auth.security.AccountPrincipal;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AccountSessionServiceImpl implements AccountSessionService {
    private final SessionRegistry sessions;
    public AccountSessionServiceImpl(SessionRegistry sessions) { this.sessions = sessions; }

    @Override
    public void revokeUserSessions(Long accountId) {
        Runnable expire = () -> sessions.getAllPrincipals().forEach(key ->
            sessions.getAllSessions(key, false).forEach(session -> {
                if (session.getPrincipal() instanceof AccountPrincipal principal
                        && accountId.equals(principal.getAccountId())) session.expireNow();
            }));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { expire.run(); }
            });
        } else expire.run();
    }
}
