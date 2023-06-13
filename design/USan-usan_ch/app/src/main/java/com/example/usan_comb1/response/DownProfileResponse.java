package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

// 프로필 이미지 다운로드 리스폰스
public class DownProfileResponse {
    @SerializedName("filename")
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}