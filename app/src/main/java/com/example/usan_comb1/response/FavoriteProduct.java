package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

public class FavoriteProduct {
    private int product_id;
    private String title;
    private String author;
    private String img;

    @SerializedName("access_token")
    private String access_token;


    public int getProductId() { return product_id; }

    public void setProductId(int product_id) { this.product_id = product_id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) {this.author = author; }

    public String getImg() { return img; }

    public void setImg(String img) { this.img = img; }

    public String getAccessToken() { return access_token; }
}