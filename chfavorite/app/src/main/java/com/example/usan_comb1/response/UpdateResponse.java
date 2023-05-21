package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

// 상품 수정 Response
public class UpdateResponse {
    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
