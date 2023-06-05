package com.example.usan_comb1.request;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

// 프로필 이미지 업로드 request

public class UploadRequest {
    private String username;
    private String filename;

    public MultipartBody.Part createImagePart(File file) {
        // 요청에 첨부할 이미지 파일의 RequestBody 생성
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);

        // MultipartBody.Part 생성
        MultipartBody.Part part = MultipartBody.Part.createFormData("img", file.getName(), requestBody);

        return part;
    }

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