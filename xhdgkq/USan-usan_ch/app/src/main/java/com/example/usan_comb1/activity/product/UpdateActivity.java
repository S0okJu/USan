package com.example.usan_comb1.activity.product;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.adapter.CardAdapter;
import com.example.usan_comb1.request.ProductRequest;
import com.example.usan_comb1.request.UpdateRequest;
import com.example.usan_comb1.response.ProductImageResponse;
import com.example.usan_comb1.response.UpdateResponse;
import com.example.usan_comb1.utilities.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상품 수정 Activity
public class UpdateActivity extends AppCompatActivity {

    private EditText eTitle, eContent, ePrice;
    private Button update_btn;
    private Spinner eAddressSpinner;
    private Integer productId;
    private ImageView productImg;
    private String username, accessToken;

    private static final int REQUEST_SELECT_IMAGE = 2;
    private static final int REQUEST_CROP_IMAGE = 3;
    private static final int REQUEST_READ_MEDIA_IMAGES = 1;
    private Uri imageUri; // Added variable to store selected image URI
    private String path;
    private String filename;

    private ProgressBar mProgressView;
    private ProductService mProductService;
    private UpdateRequest.Address addressObj;

    private UpdateResponse previousProduct; // 이전에 올린 게시글의 내용을 담을 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        eTitle = findViewById(R.id.updateTitle);
        eContent = findViewById(R.id.updateContent);
        ePrice = findViewById(R.id.updatePrice);
        eAddressSpinner = findViewById(R.id.updateAddress);
        productImg = findViewById(R.id.productImage);
        update_btn = findViewById(R.id.update_btn);



        mProgressView = (ProgressBar) findViewById(R.id.product_progress);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        // SharedPreferences file_prefs = getSharedPreferences("file", Context.MODE_PRIVATE);
        // filename = file_prefs.getString("filename", "");
        // System.out.println(filename);

        // Authorization
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        productId = getIntent().getIntExtra("productId", -1);

        productImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 파일 엑세스 권한 확인
                verifyStoragePermissions(UpdateActivity.this);
            }
        });

        // Set up coordinates spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eAddressSpinner.setAdapter(adapter);

        if (intent != null) {
            if (productId != -1) {
                getProduct(productId, accessToken);
            }
        }

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });
    }

    private void updateData() {
        eTitle.setError(null);
        eContent.setError(null);
        ePrice.setError(null);

        boolean cancel = false;
        View focusView = null;

        String title = eTitle.getText().toString();
        String content = eContent.getText().toString();
        String price = ePrice.getText().toString();

        // 제목의 유효성 검사
        if (title.isEmpty()) {
            eTitle.setError("제목을 입력해주세요.");
            focusView = eTitle;
            //title = "None";
            cancel = true;
        }

        // 내용의 유효성 검사
        if (content.isEmpty()) {
            eContent.setError("내용을 입력해주세요.");
            focusView = eContent;
            cancel = true;
            //content = "None";
        }

        // 가격의 유효성 검사
        if (price.isEmpty()) {
            ePrice.setError("가격을 입력해주세요.");
            focusView = ePrice;
            cancel = true;
            //price = "None";
        }

        // 주소의 유효성 검사
        String selectedAddress = (String) eAddressSpinner.getSelectedItem();
        if (selectedAddress == null || selectedAddress.isEmpty()) {
            showToast("주소를 선택해주세요.");
            cancel = true;
        } else {
            switch (selectedAddress) {
                case "간호대학":
                    addressObj = new UpdateRequest.Address("간호대학", 35.137759, 126.928947);
                    break;
                case "공과대학 1호관":
                    addressObj = new UpdateRequest.Address("공과대학 1호관", 35.141774, 126.925564);
                    break;
                case "공과대학 2호관":
                    addressObj = new UpdateRequest.Address("공과대학 2호관", 35.138634, 126.933557);
                    break;
                case "국제관":
                    addressObj = new UpdateRequest.Address("국제관", 35.142824, 126.931893);
                    break;
                case "미술대학":
                    addressObj = new UpdateRequest.Address("미술대학", 35.143912, 126.930246);
                    break;
                case "법과대학":
                    addressObj = new UpdateRequest.Address("법과대학", 35.139344, 126.935199);
                    break;
                case "본관":
                    addressObj = new UpdateRequest.Address("본관", 35.142688, 126.934678);
                    break;
                case "사회과학관":
                    addressObj = new UpdateRequest.Address("사회과학관", 35.146031, 126.934222);
                    break;
                case "생명공학관":
                    addressObj = new UpdateRequest.Address("생명공학관", 35.141166, 126.928570);
                    break;
                case "서석홀":
                    addressObj = new UpdateRequest.Address("서석홀", 35.145035, 126.932607);
                    break;
                case "의과대학":
                    addressObj = new UpdateRequest.Address("의과대학", 35.140486, 126.929584);
                    break;
                case "자연과학관":
                    addressObj = new UpdateRequest.Address("자연과학관", 35.139391, 126.928352);
                    break;
                case "중앙도서관":
                    addressObj = new UpdateRequest.Address("중앙도서관", 35.141706, 126.932129);
                    break;
                case "체육관":
                    addressObj = new UpdateRequest.Address("체육관", 35.140330, 126.927579);
                    break;
                case "e스포츠 경기장":
                    addressObj = new UpdateRequest.Address("e스포츠 경기장", 35.140820, 126.933031);
                    break;
                case "IT융합대학":
                    addressObj = new UpdateRequest.Address("IT융합대학", 35.139907, 126.934216);
                    break;

                default:
                    showToast("주소를 선택해주세요.");
                    return;
            }
        }


        // 이전 게시글의 내용을 업데이트하기 위해 UpdateRequest 객체 생성
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setProduct_id(productId);
        updateRequest.setTitle(title);
        updateRequest.setContent(content);
        updateRequest.setPrice(price);
        updateRequest.setAddress(addressObj);


        Call<UpdateRequest> call = mProductService.updateProduct(accessToken, productId, updateRequest);
        call.enqueue(new Callback<UpdateRequest>() {
            @Override
            public void onResponse(Call<UpdateRequest> call, Response<UpdateRequest> response) {
                if (response.isSuccessful()) {

                    // 성공적으로 업데이트된 경우
                    showUpdateSuccessDialog();
                } else {
                    // 업데이트 실패한 경우
                    Toast.makeText(UpdateActivity.this, "게시글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateRequest> call, Throwable t) {
                // 통신 실패한 경우
                Toast.makeText(UpdateActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
        }
    }


    private void getProduct(Integer productId, String accessToken) {
        Call<UpdateResponse> call = mProductService.getupdateProduct(accessToken, productId);
        call.enqueue(new Callback<UpdateResponse>() {
            @Override
            public void onResponse(Call<UpdateResponse> call, Response<UpdateResponse> response) {
                if (response.isSuccessful()) {
                    UpdateResponse product = response.body();
                    if (product != null) {
                        // 이전에 올린 게시글의 내용을 변수에 저장
                        previousProduct = new UpdateResponse();
                        previousProduct.setProduct_id(product.getProduct_id());
                        previousProduct.setTitle(product.getTitle());
                        previousProduct.setContent(product.getContent());
                        previousProduct.setAddress(product.getAddress());
                        previousProduct.setPrice(product.getPrice());
                        previousProduct.setImg(product.getImg());

                        // 이전 게시글의 내용을 화면에 표시
                        eTitle.setText(product.getTitle());
                        eContent.setText(product.getContent());
                        String previousAddress = product.getAddress().getName();

                        System.out.println(product.getImg());
                        downloadImage(accessToken, productId, product.getImg());

                        // 이전 주소가 spinner의 목록에 있는지 확인하고 인덱스를 찾습니다.
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) eAddressSpinner.getAdapter();
                        int addressIndex = adapter.getPosition(previousAddress);

                        // 인덱스를 spinner에 설정하여 이전 주소를 선택합니다.
                        eAddressSpinner.setSelection(addressIndex);
                        ePrice.setText(product.getPrice());
                    }
                } else {
                    Toast.makeText(UpdateActivity.this, "게시글을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateResponse> call, Throwable t) {
                Toast.makeText(UpdateActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("상품 정보가 성공적으로 수정되었습니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class GeocodeAsyncTask extends AsyncTask<String, Void, Address> {

        @Override
        protected Address doInBackground(String... strings) {
            String address = strings[0];
            Geocoder geocoder = new Geocoder(UpdateActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(address, 1);
                if (addresses.size() > 0) {
                    return addresses.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Address locationAddress) {
            if (locationAddress != null) {
                double latitude = locationAddress.getLatitude();
                double longitude = locationAddress.getLongitude();
                //showToast("위도 : " + latitude + "\n경도 : " + longitude);

                addressObj = new UpdateRequest.Address(locationAddress.getAddressLine(0), latitude, longitude);
                updateData();
            } else {
                showToast("위치를 찾을 수 없습니다.");
            }
        }
    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // 토스트 메시지를 출력하는 메서드
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have permission
        int permission = ActivityCompat.checkSelfPermission(
                activity, android.Manifest.permission.READ_MEDIA_IMAGES);

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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activity.startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    String imagePath = getRealPathFromUri(imageUri);
                    if (imagePath != null) {
                        uploadproductImage(accessToken, productId, imagePath);
                    }
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                String imagePath = getRealPathFromUri(imageUri);
                if (imagePath != null) {
                    uploadproductImage(accessToken, productId, imagePath);
                }
            }
        }
    }


    private String getRealPathFromUri(Uri uri) {
        if (uri == null) {
            // Handle null Uri
            return null;
        }

        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        if (cursor == null) {
            // Handle null cursor
            return null;
        }
        int columnIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(columnIdx);
        cursor.close();
        return result;
    }


    private void uploadproductImage(String accessToken, int productId, String imagePath) {
        String actualPath = Uri.parse(imagePath).getPath();
        File file = new File(actualPath);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("imgs", file.getName(), requestFile);
        Call<List<ProductImageResponse>> call = mProductService.uploadproductImage(accessToken, productId, body);
        call.enqueue(new Callback<List<ProductImageResponse>>() {
            @Override
            public void onResponse(Call<List<ProductImageResponse>> call, Response<List<ProductImageResponse>> response) {
                if (response.isSuccessful()) {
                    List<ProductImageResponse> imageResponses = response.body();
                    if (imageResponses != null && !imageResponses.isEmpty()) {
                        // 이미지 업로드 성공 처리
                        Toast.makeText(UpdateActivity.this, "사진을 업로드했습니다.", Toast.LENGTH_SHORT).show();
                        Log.i("Upload success", "Successfully uploaded image");

                        ProductImageResponse firstImageResponse = imageResponses.get(0);
                        String filename = firstImageResponse.getFileName();
                        System.out.println(filename);

                        // SharedPreferences file_pref = getSharedPreferences("file", Context.MODE_PRIVATE);
                        // SharedPreferences.Editor editor = file_pref.edit();
                        // editor.putString("filename", filename);
                        // editor.apply();

                        // Intent url_intent = new Intent(UpdateActivity.this, CardAdapter.class);
                        // url_intent.putExtra("imagePath", imagePath); // imagePath 값을 인텐트에 추가
                        // startActivity(url_intent);

                        downloadImage(accessToken, productId, filename);
                    } else {
                        // 서버 응답에 이미지 정보가 없는 경우 처리
                        Toast.makeText(UpdateActivity.this, "서버 응답에 이미지 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 이미지 업로드 실패 처리
                    Toast.makeText(UpdateActivity.this, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("Upload error", "Upload failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<ProductImageResponse>> call, Throwable t) {
                // 네트워크 오류 처리
                Toast.makeText(UpdateActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                Log.e("Upload error", t.getMessage());
            }
        });
    }

    // 이미지 다운로드
    private void downloadImage(String accessToken, int productId, String filename) {
        Call<ResponseBody> call = mProductService.downloadImage(accessToken, productId, filename);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        InputStream inputStream = responseBody.byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // 이미지를 이미지 뷰에 설정합니다.
                        productImg.setImageBitmap(bitmap);
                    } else {
                        // 이미지 데이터가 없는 경우 기본 이미지를 설정합니다.
                        productImg.setImageResource(R.drawable.img_error);
                        Log.e("Download error", "Download failed: " + response.message());
                    }
                } else {
                    // 서버 응답이 실패인 경우 기본 이미지를 설정합니다.
                    productImg.setImageResource(R.drawable.img_error);
                    Log.e("Download error", "Download failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 이미지 다운로드 중 오류가 발생한 경우 기본 이미지를 설정합니다.
                productImg.setImageResource(R.drawable.img_error);
                Log.e("Download error", "Download failed: " + t.getMessage());
            }
        });
    }

}