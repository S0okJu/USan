package com.example.usan_comb1;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.usan_comb1.response.UploadResponse;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileUploader {
    private ProductService mProductService;
    private String accessToken;
    private String username;
    private SharedPreferences prefs;

    public ProfileUploader(Context context) {
        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);
        prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        username = prefs.getString("username", "");
        accessToken = prefs.getString("access_token", "");
    }

    public void uploadProfileImage(String username, String imagePath, final ProfileUploadCallback callback) {
        File imageFile = new File(imagePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("imgs", imageFile.getName(), requestFile);

        if (mProductService == null) {
            callback.onUploadFailed("Failed to initialize ProductService");
            return;
        }

        Call<UploadResponse> call = mProductService.uploadImage("Bearer " + accessToken, username, body);
        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if (response.isSuccessful()) {
                    callback.onUploadSuccess();
                } else {
                    callback.onUploadFailed("Upload failed");
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                callback.onUploadFailed(t.getMessage());
            }
        });
    }

    public interface ProfileUploadCallback {
        void onUploadSuccess();
        void onUploadFailed(String errorMessage);
    }
}

