package com.example.usan_comb1.request;

import com.google.gson.annotations.SerializedName;

// 회원가입 Request
public class RegisterData {

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("password")
    private String password;

    @SerializedName("email")
    private String email;

    public RegisterData(String nickname, String password, String email) {
        this.nickname = nickname;
        this.password = password;
        this.email = email;
    }
}
