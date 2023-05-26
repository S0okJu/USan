package com.example.usan_comb1.request;

// 이미지 다운로드 Request
public class DownImage {

    private Integer product_id;
    private String filename;

    public Integer getProduct_id() { return product_id; }

    public void setProduct_id(Integer product_id) { this.product_id = product_id; }

    public String getFilename() { return filename; }

    public void setFilename(String filename) { this.filename = filename; }
}
