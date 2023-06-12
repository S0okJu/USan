package com.example.usan_comb1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class RetrofitClient {

    private static final String BASE_URL = "http://3.36.126.20:51859/";

    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitInstance() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static ProductService getProductService() {
        return getRetrofitInstance().create(ProductService.class);
    }
}