package com.example.usan_comb1.response;

// 특정 상품 표시 Response
public class PostResult {

    private Integer product_id;
    private String title;
    private String price;
    private String author;
    private String content;
    private String address;
    private boolean status;
    private boolean favorite;
    private String created_date;
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

    public void setProduct_Id(Integer product_id) { this.product_id = product_id; }

    public void setPost_Title(String title) { this.title = title; }

    public void setPost_Price(String price) { this.price = price; }

    public void setPost_Author(String author) { this.author = author; }

    public void setPost_Content(String content) { this.content = content; }

    public void setAddress(String address) { this.address = address; }

    public void setStatus(boolean status) { this.status = status; }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public void setCreated_date(String created_date) { this.created_date = created_date; }

    public void setModified_date(String modified_date) { this.modified_date = modified_date; }
}
