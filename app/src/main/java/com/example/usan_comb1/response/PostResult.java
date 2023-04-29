package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

// 특정 상품 표시 Response
public class PostResult {
    @SerializedName("product_id")
    private Integer product_id;
    @SerializedName("title")
    private String title;
    @SerializedName("price")
    private String price;
    @SerializedName("author")
    private String author;
    @SerializedName("content")
    private String content;
    @SerializedName("address")
    private String address;
    @SerializedName("status")
    private boolean status;

    @SerializedName("favorite")
    private boolean favorite;
    @SerializedName("created_date")
    private String created_date;
    @SerializedName("modified_date")
    private String modified_date;

    public Integer getProduct_Id() { return product_id; }

    public String getPost_Title() { return title; }

    public String getPost_Price() { return price; }

    public String getPost_Author() { return author; }

    public String getPost_Content() { return content; }

    public String getPost_Address() { return address; }

    public boolean isStatus() { return status; }

    public boolean isFavorite() { return favorite; }

    public String getCreated_date() { return created_date; }

    public String getModified_date() { return modified_date; }
}
