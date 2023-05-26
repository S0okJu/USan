package com.example.usan_comb1.request;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

// 프로필 이미지 업로드 request

public class UploadRequest {
    private String username;
    private String filename;

    public UploadRequest(String username, String filename) {
        this.username = username;
        this.filename = filename;
    }

    public String getUsername() {
        return username;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
