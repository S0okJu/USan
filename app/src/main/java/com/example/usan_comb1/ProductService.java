package com.example.usan_comb1;

import com.example.usan_comb1.request.LoginData;
import com.example.usan_comb1.request.ProductRequest;
import com.example.usan_comb1.request.RegisterData;
import com.example.usan_comb1.request.UpdateRequest;
import com.example.usan_comb1.response.LoginResponse;
import com.example.usan_comb1.response.PostResult;
import com.example.usan_comb1.response.ProductResponse;
import com.example.usan_comb1.response.RegisterResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductService {
    //@통신 방식("통신 API명")

    // 이미지 다운로드
    @GET("imgs/download/<product_id>/<filename>")
    Call<ResponseBody> downloadImage(@Path("product_id") Integer productId, @Path("filename") String filename);

    // 페이지 별 상품 정보
    @GET("display/productlist?page_per=10&page=1&type=0")
    Call<String> string_call();

    // 특정 상품 표시
    @GET("product/{id}")
    Call<PostResult> getProduct(@Path("id") Integer product_id);

    // 상품 추가
    @POST("product/post")
    Call<ProductResponse> postProduct(@Body ProductRequest request);

    // 상품 수정
    @POST("/product/modify")
    Call<ResponseBody> updateProduct(@Path("id") Integer productId, @Body UpdateRequest updateProduct);

    @POST("/users/login")
    Call<LoginResponse> userLogin(@Body LoginData data);

    @POST("/users/register")
    Call<RegisterResponse> userRegister(@Body RegisterData data);

    /*
    통신을 정의해주는 interface를 만들어 통신을 위한 함수를 만들어줍니다.
    getLoginResponse 함수로 LoginRequest.java에 정의해준 데이터들을 서버 Body에 보낸 후 LoginResponse로 데이터를 받겠다는 의미를 가집니다.

    출처 - https://code-hyoon.tistory.com/9
     */
}
