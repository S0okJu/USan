package com.example.usan_comb1.activity.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.ProfileUpRequest;
import com.example.usan_comb1.response.ProfileResponse;
import com.example.usan_comb1.response.UploadResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private static final int REQUEST_CROP_IMAGE = 3;
    private String username;
    private String accessToken;
    private Uri imageUri; // Added variable to store selected image URI
    private File croppedImageFile;
    private File selectedImageFile;

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

    private void cropImage(Uri sourceUri) {
        try {
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), sourceUri);
            Bitmap croppedBitmap = resizeBitmap(originalBitmap, 500, 500); // 원하는 크기로 이미지 리사이징

            // 리사이즈된 이미지를 파일로 저장
            File croppedImageFile = saveBitmapToFile(croppedBitmap);

            // 파일을 업로드
            uploadImage(croppedImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // 이미지를 정방향으로 잘라내기 위해 cropImage() 메서드 호출
                cropImage(imageUri);
            }
        } else if (requestCode == REQUEST_CROP_IMAGE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                // 이미지를 잘라낸 결과를 받아옴
                Bitmap croppedBitmap = extras.getParcelable("data");
                if (croppedBitmap != null) {
                    // 잘라낸 이미지를 파일로 저장
                    File croppedImageFile = saveBitmapToFile(croppedBitmap);
                    // 파일을 업로드
                    uploadImage(croppedImageFile);
                }
            }
        }
    }

    private File saveBitmapToFile(Bitmap bitmap) {
        try {
            File imageFile = createImageFile();
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private File createImageFile() throws IOException {
        // 이미지 파일을 저장할 디렉토리 생성
        File storageDir = getExternalCacheDir();
        File imageFile = File.createTempFile(
                "profile_image",  /* 파일 이름 */
                ".jpg",         /* 파일 확장자 */
                storageDir      /* 저장될 디렉토리 */
        );
        return imageFile;
    }

    private void copyInputStreamToFile(InputStream inputStream, File file) {
        try {
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(UserUpdateActivity.this, "파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(UserUpdateActivity.this, "파일 복사 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(File croppedImageFile) {
        if (croppedImageFile == null) {
            Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 파일을 RequestBody로 변환
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), croppedImageFile);
        // RequestBody를 MultipartBody.Part로 변환
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("imgs", croppedImageFile.getName(), requestBody);
        // API를 호출하여 이미지 업로드
        Call<UploadResponse> call = mProductService.uploadImage(accessToken, username, imagePart);
        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if (response.isSuccessful()) {
                    // 이미지 업로드 성공 처리
                    UploadResponse uploadResponse = response.body();
                    String imageUrl = uploadResponse.getFileName();

                    // 이미지 URL을 사용하여 필요한 작업 수행
                    Toast.makeText(UserUpdateActivity.this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                } else {
                    // 이미지 업로드 실패 처리
                    Toast.makeText(UserUpdateActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                // 네트워크 오류 처리
                Toast.makeText(UserUpdateActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


// 프로필 이미지 다운로드
    /*
    private void downloadImage() {
        Call<ResponseBody> call = mProductService.downloadImage(username);
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
                    }
                } else {
                    // 서버 응답이 실패인 경우 기본 이미지를 설정합니다.
                    imgprofile.setImageResource(R.drawable.ic_default_profile);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 이미지 다운로드 중 오류가 발생한 경우 기본 이미지를 설정합니다.
                imgprofile.setImageResource(R.drawable.ic_default_profile);
            }
        });
    }
     */
}