package com.example.usan_comb1.models;

public class User {
    private String username;
    private String email;

    // Create getters and setters for username and email

    // Empty constructor (required by Firebase)
    public User() {}

    // Constructor
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}