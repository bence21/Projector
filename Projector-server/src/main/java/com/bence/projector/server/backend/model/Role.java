package com.bence.projector.server.backend.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum Role {
    ROLE_USER(0),
    ROLE_ADMIN(1),
    ROLE_REVIEWER(2);
    private static ConcurrentHashMap<Integer, Role> roleMap;
    private final int value;

    Role(int value) {
        this.value = value;
    }

    public static Role getInstance(int value) {
        Map<Integer, Role> roleMap = getRoleMap();
        return roleMap.get(value);
    }

    private static Map<Integer, Role> getRoleMap() {
        if (roleMap == null) {
            Role[] values = Role.values();
            roleMap = new ConcurrentHashMap<>(values.length);
            for (Role role : values) {
                roleMap.put(role.getValue(), role);
            }
        }
        return roleMap;
    }

    public int getValue() {
        return value;
    }
}
