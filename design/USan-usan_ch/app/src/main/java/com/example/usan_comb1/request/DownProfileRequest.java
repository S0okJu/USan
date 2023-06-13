package com.example.usan_comb1.request;

import com.google.gson.annotations.SerializedName;

// 프로필 닉네임 수정

public class DownProfileRequest {
    @SerializedName("username")
    String username;


    public DownProfileRequest(String username) {
        this.username = username ;
    }
}