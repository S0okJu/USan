package com.example.usan_comb1.request;

// 이메일 중복 확인 request
public class EmailCheckRequest {
    private String email;

    public EmailCheckRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
