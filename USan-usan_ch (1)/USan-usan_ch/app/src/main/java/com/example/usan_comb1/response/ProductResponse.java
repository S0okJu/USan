package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

// 상품 추가 Response
public class ProductResponse {

    @SerializedName("status_code")
    private int status_code;

    @SerializedName("message")
    private String message;

    @SerializedName("access_token")
    private String access_token;

    @SerializedName("product_id")
    private int product_id;

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAccessToken() {
        return access_token;
    }
}
