package com.example.usan_comb1;

import com.google.gson.annotations.SerializedName;

public class FavoriteProduct {
    private String product_id;
    private String title;
    private String author;
    private String img;

    @SerializedName("access_token")
    private String access_token;

    /*
    public FavoriteProduct(String product_id, String title, String author, String img) {
        this.product_id = product_id;
        this.title = title;
        this.author = author;
        this.img = img;
    }

     */

    public String getProductId() { return product_id; }

    public void setProductId(String productId) { this.product_id = product_id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) {this.author = author; }

    public String getImg() { return img; }

    public void setImg(String ImageUrl) { this.img = img; }

    public String getAccessToken() { return access_token; }
}