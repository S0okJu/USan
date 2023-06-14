package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

// 프로필 이미지 다운로드 리스폰스
public class DownProfileResponse {
    @SerializedName("path")
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}