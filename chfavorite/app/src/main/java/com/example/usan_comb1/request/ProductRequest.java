package com.example.usan_comb1.request;

import com.google.gson.annotations.SerializedName;

// 상품 추가 Request
public class ProductRequest {

    @SerializedName("title")
    private String title;

    @SerializedName("author")
    private String author;

    @SerializedName("content")
    private String content;

    @SerializedName("address")
    private String address;

    @SerializedName("price")
    private String price;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public ProductRequest(String title, String author, String content, String address, String price) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.address = address;
        this.price = price;
    }
}
