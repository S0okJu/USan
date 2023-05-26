package com.example.usan_comb1;

import com.example.usan_comb1.request.LoginData;
import com.example.usan_comb1.request.ProductRequest;
import com.example.usan_comb1.request.ProfileUpRequest;
import com.example.usan_comb1.request.RegisterData;
import com.example.usan_comb1.request.UpdateRequest;
import com.example.usan_comb1.response.FavoriteProduct;
import com.example.usan_comb1.response.LoginResponse;
import com.example.usan_comb1.response.PostResult;
import com.example.usan_comb1.response.ProductResponse;
import com.example.usan_comb1.response.ProfileResponse;
import com.example.usan_comb1.response.RegisterResponse;
import com.example.usan_comb1.response.RetroProduct;
import com.example.usan_comb1.response.UpdateResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductService {
    //@통신 방식("통신 API명")

    //사용자별 판매 물품
    @GET("/display/{username}/productlist")
    Call<List<RetroProduct>> getProductList(@Header("Authorization") String accessToken, @Path("username") String username, @Query("page_per") Integer page_per, @Query("page") Integer page);

    // 작성자가 등록한 모든 상품 정보를 가져오는 API
    @GET("products/author/{author_id}")
    Call<List<PostResult>> getProductsByAuthor(@Header("Authorization") String accessToken,@Path("author_id") Integer authorId);

    // 판매 완료 해제
    @GET("product/status&type=0")
    Call<Void> unStatus(@Header("Authorization") String accessToken, @Query("product_id") Integer productId);

    // 판매 완료
    @GET("product/status?type=1")
    Call<Void> setStatus(@Header("Authorization") String accessToken,@Query("product_id") Integer productId);

    // 관심 물건 등록 해제
    @GET("product/favorite?type=0")
    Call<Void> unFavorite(@Header("Authorization") String accessToken, @Query("product_id") Integer productId);

    // 관심 물건 등록
    @GET("product/favorite?type=1")
    Call<Void> setFavorite(@Header("Authorization") String accessToken, @Query("product_id") Integer productId);

    // 관심 물건 목록
    @GET("/display/{username}/favorite")
    Call<List<FavoriteProduct>> favorite_list(@Header("Authorization") String accessToken, @Path("username") String username, @Query("page") int page);

    // 이미지 다운로드
    @GET("imgs/download/{product_id}/{filename}")
    Call<ResponseBody> downloadImage(@Path("product_id") Integer productId, @Path("filename") String filename);

    // 페이지 별 상품 정보
    @GET("display/productlist?page_per=10&page=1&type=0")
    Call<String> string_call(@Header("Authorization") String accessToken);

    // 특정 상품 표시
    @GET("product/{id}?type=0")
    Call<PostResult> getProduct(@Header("Authorization") String accessToken, @Path("id") Integer product_id);

    // 상품 추가
    @POST("product/post")
    Call<ProductResponse> postProduct(@Header("Authorization") String accessToken, @Body ProductRequest request);

    // 수정할 상품 표시
    @GET("product/{id}?type=1")
    Call<UpdateResponse> getupdateProduct(@Header("Authorization") String accessToken, @Path("id") Integer product_id);

    // 상품 수정
    @POST("product/modify")
    Call<UpdateRequest> updateProduct(@Header("Authorization") String accessToken, @Query("id") Integer productId, @Body UpdateRequest updateProduct);

    // 상품 삭제
    // @query -> path
    @GET("product/delete/{product_id}")
    Call<Void> deletePost(@Header("Authorization") String accessToken, @Path("product_id") Integer productId);

    // 사용자 로그인
    @POST("/users/login")
    Call<LoginResponse> userLogin(@Body LoginData data);

    // 사용자 회원가입
    @POST("/users/register")
    Call<RegisterResponse> userRegister(@Body RegisterData data);

    // 사용자 프로필 가져오기
    @GET("/profile/{username}")
    Call<ProfileResponse> getProfile(@Header("Authorization") String authorization, @Path("username") String username);

    // 사용자 닉네임 수정
    @POST("/profile/{username}/modify")
    Call<Void> modifyProfile(@Header("Authorization") String accessToken, @Path("username") String username, @Body ProfileUpRequest profileUpRequest);

    // 사용자 프로필 이미지
    @Headers("Content-Type: multipart/form-data")
    @Multipart
    @POST("/profile/{username}/upload")
    Call<ResponseBody> uploadImage(@Header("Authorization") String accessToken, @Path("username") String username, @Part MultipartBody.Part imagePart);


    /*
    통신을 정의해주는 interface를 만들어 통신을 위한 함수를 만들어줍니다.
    getLoginResponse 함수로 LoginRequest.java에 정의해준 데이터들을 서버 Body에 보낸 후 LoginResponse로 데이터를 받겠다는 의미를 가집니다.

    출처 - https://code-hyoon.tistory.com/9
     */
}
