package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

public class CheckRoleResponse {
    @SerializedName("role")
    private Integer role;

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }
}
