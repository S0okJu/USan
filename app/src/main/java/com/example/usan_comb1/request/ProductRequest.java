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
    private Address address;

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

    public Address getAddress() { return address; }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public ProductRequest(String title, String author, String content, Address address, String price) {
        this.title = title;
        this.author = author;
        this.content = content;
        this.address = address;
        this.price = price;
    }

    public static class Address {
        @SerializedName("name")
        private String name;

        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        public String getName() { return name; }

        public void setName(String name) { this.name = name; }

        public double getLatitude() { return latitude; }

        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }

        public void setLongitude(double longitude) { this.longitude = longitude; }

        public Address(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
