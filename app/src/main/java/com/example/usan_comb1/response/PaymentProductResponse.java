package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

public class PaymentProductResponse {

    @SerializedName("title")
    private String title;

    @SerializedName("author")
    private String author;

    @SerializedName("price")
    private int price;

    @SerializedName("image")
    private String image;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}