package com.example.usan_comb1.request;

import com.google.gson.annotations.SerializedName;

// 로그인 Request
public class LoginData {
    @SerializedName("email")
    String email;

    @SerializedName("password")
    String password;

    public LoginData(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
