package com.example.usan_comb1.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// 상품 수정 Request
public class UpdateRequest {

    @SerializedName("product_id")
    private int product_id;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("address")
    private Address address;

    @SerializedName("price")
    private String price;

    @SerializedName("image_filenames")
    private String img;

    public int getProduct_id() { return product_id; }

    public void setProduct_id(int product_id) { this.product_id = product_id; }

    public String getImg() { return img; }

    public void setImg(String img) { this.img = img; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public Address getAddress() { return address; }

    public void setAddress(Address address) { this.address = address; }

    public String getPrice() { return price; }

    public void setPrice(String price) { this.price = price; }



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