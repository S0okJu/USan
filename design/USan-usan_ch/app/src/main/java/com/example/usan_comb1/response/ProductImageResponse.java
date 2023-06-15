package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

// 상품 이미지 업로드
public class ProductImageResponse {
    @SerializedName("path")
    private String path;

    @SerializedName("filename")
    private String filename;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return filename;
    }

    public void setFileName(String filename) {
        this.filename = filename;
    }
}
