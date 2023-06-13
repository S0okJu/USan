package com.example.usan_comb1.request;

import com.google.gson.annotations.SerializedName;

// 프로필 닉네임 수정

public class UploadRequest {
    @SerializedName("username")
    String username;

    @SerializedName("filename")
    String filename;


    public UploadRequest(String username, String filename) {
        this.username = username ;
        this.filename = filename ;
    }
}