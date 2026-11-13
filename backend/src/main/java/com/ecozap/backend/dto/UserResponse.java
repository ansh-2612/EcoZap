package com.ecozap.backend.dto;

import com.ecozap.backend.model.user;

public class UserResponse {

    private String id;
    private String name;
    private String email;

    public UserResponse(user user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}