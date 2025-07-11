package com.restaurant_service.util;

import lombok.Getter;

@Getter
public class UserPrincipal {
    private final String id;
    private final String email;

    public UserPrincipal(String id, String email) {
        this.id = id;
        this.email = email;
    }

}

