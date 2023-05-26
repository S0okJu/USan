package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    @SerializedName("username") private String username;
    @SerializedName("filename") private String filename;

    public ProfileResponse(String username, String filename) {
        this.filename = filename;
        this.username = username;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) {this.username = username;}

    public String getFilename() { return filename; }

    public void setFilename(String filename) {this.filename = filename;}
}