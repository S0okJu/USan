package com.example.usan_comb1.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.ProfileUpRequest;
import com.example.usan_comb1.response.ProfileResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

 // 프로필 수정
public class UserUpdateActivity extends AppCompatActivity {

    private Button btnselect, btn_ch, btn_ok;
    private TextView tvname;
    private EditText et_updatename;
    private ImageView imgprofile;
    private ProductService mProductService;
    private static final int REQUEST_PERMISSION = 1;
    private static final int REQUEST_SELECT_IMAGE = 2;
    private String username;
    private String accessToken;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_update);

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        username = prefs.getString("username", "");

        tvname = findViewById(R.id.tv_name);
        et_updatename = findViewById(R.id.et_nameupdate);
        btn_ch = findViewById(R.id.btn_ch);
        btn_ok = findViewById(R.id.btn_ok);
        btnselect = findViewById(R.id.btnselect);
        imgprofile = findViewById(R.id.imgprofile);

        et_updatename.setVisibility(View.INVISIBLE);
        btn_ch.setVisibility(View.VISIBLE);
        btn_ok.setVisibility(View.INVISIBLE);

        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        Call<ProfileResponse> call = mProductService.getProfile(accessToken, username);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful()) {
                    ProfileResponse profile = response.body();
                    // 프로필 정보를 사용하여 필요한 작업 수행
                    String username = profile.getUsername(); // 프로필 정보에서 username 값을 가져옵니다.
                    tvname.setText(username); // TextView에 username 값을 설정합니다.
                } else {
                    // 오류 처리
                    Toast.makeText(UserUpdateActivity.this, "데이터 통신 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(UserUpdateActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btn_ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvname.setVisibility(View.INVISIBLE);
                et_updatename.setVisibility(View.VISIBLE);
                btn_ch.setVisibility(View.INVISIBLE);
                btn_ok.setVisibility(View.VISIBLE);

                et_updatename.setText(username);

                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String updatedName = et_updatename.getText().toString();
                        ProfileUpRequest profileUpRequest = new ProfileUpRequest(updatedName);
                        Call<Void> call = mProductService.modifyProfile(accessToken, username, profileUpRequest);
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    // 수정 성공 처리
                                    Toast.makeText(UserUpdateActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();

                                    // 수정된 username을 가져온 후 SharedPreferences에 다시 저장
                                    SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("username", updatedName); // 수정된 username을 저장
                                    editor.apply();

                                    tvname.setVisibility(View.VISIBLE);
                                    et_updatename.setVisibility(View.INVISIBLE);
                                    btn_ch.setVisibility(View.VISIBLE);
                                    btn_ok.setVisibility(View.INVISIBLE);
                                } else {
                                    // 수정 실패 처리
                                    Toast.makeText(UserUpdateActivity.this, "수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                // 네트워크 오류 처리
                                Toast.makeText(UserUpdateActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            uploadImage(username, imageUri, accessToken);
        }
    }


    public void uploadImage(String username, Uri imageUri, String accessToken) {
        // 이미지를 MultipartBody.Part로 변환
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File imageFile = createImageFileFromInputStream(inputStream);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("imgs", imageFile.getName(), requestFile);
            //이미지 업로드 요청
            Call<ResponseBody> call = mProductService.uploadImage("Bearer " + accessToken, username, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        // 이미지 업로드 성공 처리
                        Toast.makeText(UserUpdateActivity.this, "사진을 업로드했습니다.", Toast.LENGTH_SHORT).show();
                        Log.i("Upload success", "Successfully uploaded image");
                    } else {
                        // 이미지 업로드 실패 처리
                        Toast.makeText(UserUpdateActivity.this, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        Log.e("Upload error", "Upload failed: " + response.message());
                        return;
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 네트워크 오류 처리
                    Toast.makeText(UserUpdateActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                    Log.e("Upload error", t.getMessage());
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private File createImageFileFromInputStream(InputStream inputStream) {
        try {
            File file = new File(getCacheDir(), "temp_image.jpg");
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}