package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

//
public class RetroProduct {
    @SerializedName("title") private String title;
    @SerializedName("status") private boolean status;
    @SerializedName("img") private String img;
    @SerializedName("price") private Integer price;
    @SerializedName("product_id") private Integer productId;

    public RetroProduct(String title, boolean status, String img, Integer price, Integer productId) {
        this.title = title;
        this.status = status;
        this.img = img;
        this.price = price;
        this.productId = productId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) { this.img = img;}

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}