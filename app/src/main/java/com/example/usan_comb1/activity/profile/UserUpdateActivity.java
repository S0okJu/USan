package com.example.usan_comb1.activity.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.ProfileUpRequest;
import com.example.usan_comb1.response.ProfileResponse;
import com.example.usan_comb1.response.UploadResponse;

import java.io.File;
import java.io.InputStream;

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
    private static final int REQUEST_SELECT_IMAGE = 2;
    private static final int REQUEST_CROP_IMAGE = 3;
    private String username;
    private String accessToken;
    private Uri imageUri; // Added variable to store selected image URI
    private static final int REQUEST_READ_MEDIA_IMAGES = 1;
    private static String[] PERMISSIONS_STORAGE;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_update);

        PERMISSIONS_STORAGE = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

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

        downloadImage();

        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 파일 엑세스 권한 확인
                verifyStoragePermissions(UserUpdateActivity.this);
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

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have permission
        int permission = ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.READ_MEDIA_IMAGES);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_READ_MEDIA_IMAGES
            );
        } else {
            // Permission granted, open the gallery
            openGallery(activity);
        }
    }

    private static void openGallery(Activity activity) {
        // Open gallery code
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            String imagePath = getRealPathFromUri(imageUri);
            uploadImage(accessToken, username, imagePath);
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int columnIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(columnIdx);
        cursor.close();
        return result;
    }

    private void uploadImage(String accessToken, String username, String imagePath) {
        // Convert file path to actual path
        String actualPath = Uri.parse(imagePath).getPath();
        File file = new File(actualPath);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("img", file.getName(), requestFile);
        Call<UploadResponse> call = mProductService.uploadImage(accessToken, username, body);
        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if (response.isSuccessful()) {
                    // 이미지 업로드 성공 처리
                    Toast.makeText(UserUpdateActivity.this, "사진을 업로드했습니다.", Toast.LENGTH_SHORT).show();
                    Log.i("Upload success", "Successfully uploaded image");
                    downloadImage();
                } else {
                    // 이미지 업로드 실패 처리
                    Toast.makeText(UserUpdateActivity.this, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("Upload error", "Upload failed: " + response.message());
                    return;
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                // 네트워크 오류 처리
                Toast.makeText(UserUpdateActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                Log.e("Upload error", t.getMessage());
            }

        });
    }

    // 프로필 이미지 다운로드
    private void downloadImage() {
        Call<ResponseBody> call = mProductService.downloadProfileImage(accessToken, username);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        // 이미지 데이터를 읽어옵니다.
                        InputStream inputStream = responseBody.byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // 이미지를 이미지 뷰에 설정합니다.
                        imgprofile.setImageBitmap(bitmap);
                    } else {
                        // 이미지 데이터가 없는 경우 기본 이미지를 설정합니다.
                        imgprofile.setImageResource(R.drawable.ic_default_profile);
                        Toast.makeText(UserUpdateActivity.this, "이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                        Log.e("Download error", "Download failed: " + response.message());
                    }
                } else {
                    // 서버 응답이 실패인 경우 기본 이미지를 설정합니다.
                    imgprofile.setImageResource(R.drawable.ic_default_profile);
                    Toast.makeText(UserUpdateActivity.this, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                    Log.e("Download error", "Download failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 이미지 다운로드 중 오류가 발생한 경우 기본 이미지를 설정합니다.
                imgprofile.setImageResource(R.drawable.ic_default_profile);
                Toast.makeText(UserUpdateActivity.this, "다운로드 오류", Toast.LENGTH_SHORT).show();
                Log.e("Download error", "Download failed: " + t.getMessage());
            }
        });
    }
}