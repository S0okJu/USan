package com.example.usan_comb1;

public class FavoriteProduct {
    private String productId;
    private String title;
    private String author;
    private String ImageUrl;

    public FavoriteProduct(String productId, String title, String author, String ImageUrl) {
        this.productId = productId;
        this.title = title;
        this.author = author;
        this.ImageUrl = ImageUrl;
    }

    public String getProductId() { return productId; }

    public void setProductId(String productId) { this.productId = productId; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) {this.author = author; }

    public String getImageUrl() { return ImageUrl; }

    public void setImageUrl(String ImageUrl) { this.ImageUrl = ImageUrl; }
}