package com.lab.lab_equipment_system.features.auth.service;

public interface AccountSessionService {
    void revokeUserSessions(Long accountId);
}
