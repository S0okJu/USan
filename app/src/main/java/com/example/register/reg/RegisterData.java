package com.example.register.reg;

import com.google.gson.annotations.SerializedName;

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
