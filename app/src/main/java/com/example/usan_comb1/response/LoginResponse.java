package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

// 로그인 Response
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

