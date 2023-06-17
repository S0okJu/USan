package com.example.usan_comb1.request;

import com.google.gson.annotations.SerializedName;

// 프로필 닉네임 수정

public class ProfileUpRequest {
    @SerializedName("username")
    String username;


    public ProfileUpRequest(String username) {
        this.username = username ;
    }
}