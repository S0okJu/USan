package com.example.usan_comb1.response;
import com.google.gson.annotations.SerializedName;

public class SaleResponse {

    private String img;
    @SerializedName("title")
    private String title;

    @SerializedName("price")
    private int price;

    @SerializedName("status")
    private int status;

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }

    public int getStatus(){return status;}

    public String getImg() { return img; }
}
