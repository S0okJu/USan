package com.example.usan_comb1.request;

import com.google.gson.annotations.SerializedName;

// 상품 이미지 다운로드 리퀘스트

public class DownProductRequest {
    @SerializedName("username")
    String username;

    @SerializedName("filename")
    String filename;

    public DownProductRequest(String username, String filename) {
        this.username = username ;
        this.filename = filename ;
    }
}