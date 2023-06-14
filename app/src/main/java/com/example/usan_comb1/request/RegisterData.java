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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
