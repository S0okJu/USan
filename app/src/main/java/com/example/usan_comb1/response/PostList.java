package com.example.usan_comb1.response;

import android.graphics.Bitmap;

import java.io.Serializable;

// 페이지 별 상품 정보 Response
public class PostList implements Serializable {

    private Integer product_id;
    private String title;
    //private String price;
    private String author;
    private boolean status;
    private boolean favorite;
    private String modified_date;
    private String img;

    private Bitmap bitmap; // Bitmap 변수 추가

    public Integer getProduct_id() { return product_id; }

    public String getTitle() { return title; }

    //public String getPrice() { return price; }

    public String getAuthor() { return author; }

    public boolean isStatus() { return status; }

    public boolean isFavorite() { return favorite; }

    public String getModified_date() { return modified_date; }

    public String getImg() { return img; }


    public void setProduct_id(Integer id) { this.product_id = product_id; }

    public void setTitle(String title) { this.title = title; }

    public void setAuthor(String author) { this.author = author; }

    public void setStatus(boolean status) { this.status = status; }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public void setModified_date(String modified_date) { this.modified_date = modified_date; }

    public void setImg(String img) { this.img = img; }


    public Bitmap getBitmap() { return bitmap; }

    public void setBitmap(Bitmap bitmap) { this.bitmap = bitmap; }


    /*
     PostList(Integer product_id, String title, String author, boolean status, boolean favorite, String modified_date, String img_path) {
        this.product_id = product_id;
        this.title = title;
        this.author = author;
        this.status = status;
        this.favorite = favorite;
        this.modified_date = modified_date;
        this.img_path = img_path;
    }
     */
}
