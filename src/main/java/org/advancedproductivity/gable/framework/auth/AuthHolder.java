package org.advancedproductivity.gable.framework.auth;


import java.util.concurrent.ConcurrentHashMap;

public class AuthHolder {
    public static final ConcurrentHashMap<String, AuthHandler> HOLDER = new ConcurrentHashMap<>();

    static {
    }
}
