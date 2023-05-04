package com.example.register;

import com.example.register.profile.RetroProduct;
import com.example.register.reg.LoginData;
import com.example.register.reg.LoginResponse;
import com.example.register.reg.RegisterData;
import com.example.register.reg.RegisterResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceApi {
    @POST("/users/login")
    Call<LoginResponse> userLogin(@Body LoginData data);

    @POST("/users/register")
    Call<RegisterResponse> userRegister(@Body RegisterData data);

    @GET("/display/{username}/productlist")
    Call<List<RetroProduct>> getProductList(@Header("Authorization") String accessToken, @Path("username") String username, @Query("page_per") Integer page_per, @Query("page") Integer page);
}

