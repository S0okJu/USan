package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

// 상세페이지 내부 작성자 판매 물품 Response
public class CardList {
    @SerializedName("title") private String title;
    @SerializedName("product_id") private int product_id;
    @SerializedName("img") private String img;


    public CardList(String title, int product_id, String img) {
        this.title = title;
        this.img = img;
        this.product_id=product_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
       this.product_id = product_id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }


}



