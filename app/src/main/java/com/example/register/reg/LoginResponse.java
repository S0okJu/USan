package com.example.register.reg;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("access_token")
    private String access_token;

    @SerializedName("username")
    private String username;

    public String getMessage() {
        return message;
    }

    public String getAccessToken() {
        return access_token;
    }

    // Username 추가
    public String getUsername(){return username;}

}

