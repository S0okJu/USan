package com.example.usan_comb1.request;

// 상품 수정 Request
public class UpdateRequest {
    private int product_id;
    private String product_title;
    private String product_content;
    private String address;
    private String price;


    public int getProduct_id() { return product_id; }

    public String getProduct_title() { return product_title; }

    public String getProduct_content() { return product_content; }

    public String getAddress() { return address; }

    public String getPrice() { return price; }


    public void setId(int product_id) { this.product_id = product_id; }

    public void setTitle(String title) {
        this.product_title = title;
    }

    public void setContent(String content) {
        this.product_content = content;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public UpdateRequest(int product_id, String product_title, String product_content, String address, String price) {
        this.product_id = product_id;
        this.product_title = product_title;
        this.product_content = product_content;
        this.address = address;
        this.price = price;
    }

    // UpdateProduct 객체 생성 (매개변수가 있는 생성자 사용)
    UpdateRequest updateProduct = new UpdateRequest(0,"", "", "", "");
}
