package com.example.usan_comb1.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.FavoriteActivity;
import com.example.usan_comb1.map.ListOnline;
import com.example.usan_comb1.activity.SalelistActivity;
import com.example.usan_comb1.activity.UserUpdateActivity;
import com.example.usan_comb1.map.MapActivity;
import com.example.usan_comb1.map.MapTracking;

import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFragment extends Fragment {

    private Button btnviewprf, btnsalelist, btnwishlist, btnimageup;
    private Button btngps;
    private ImageView imgprofile;
    private TextView tvname;
    private ProductService mProductService;

    private String username;
    private String accessToken;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);


        btnviewprf = view.findViewById(R.id.btnviewprf);
        btnwishlist = view.findViewById(R.id.btnwishlist);
        btnsalelist = view.findViewById(R.id.btnsalelist);
        imgprofile = view.findViewById(R.id.imageView);
        tvname = view.findViewById(R.id.name);

        btngps = view.findViewById(R.id.btngps);

        SharedPreferences prefs = getActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        username = prefs.getString("username", "");

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        tvname.setText(username);

        downloadImage();

        btnviewprf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserUpdateActivity.class);
                startActivity(intent);
            }
        });

        btnwishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent 정보를 넘겨줌
                Intent intent = getActivity().getIntent();
                String username = intent.getStringExtra("username");

                Intent wishlistIntent = new Intent(getActivity(), FavoriteActivity.class);
                wishlistIntent.putExtra("username", username);
                System.out.println("Fragment Wishlist : "+ username);
                startActivity(wishlistIntent);
            }
        });

        btnsalelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent 정보를 넘겨줌
                Intent intent = getActivity().getIntent();
                String username = intent.getStringExtra("username");

                Intent salelistIntent = new Intent(getActivity(), SalelistActivity.class);
                salelistIntent.putExtra("username", username);
                System.out.println("Fragment Salelist : "+ username);
                startActivity(salelistIntent);
            }
        });

        btngps.setOnClickListener(new View.OnClickListener() {

            private static final int REQUEST_CODE_LIST_ONLINE = 100;

            @Override
            public void onClick(View v) {
                // ListOnline으로 이동하는 부분
                Intent intent = new Intent(getActivity(), MapTracking.class);
                intent.putExtra("username", username);
                startActivityForResult(intent, REQUEST_CODE_LIST_ONLINE);
            }
        });

        return view;


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
                        Toast.makeText(getContext(), "이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                        Log.e("Download error", "Download failed: " + response.message());
                    }
                } else {
                    // 서버 응답이 실패인 경우 기본 이미지를 설정합니다.
                    imgprofile.setImageResource(R.drawable.ic_default_profile);
                    Toast.makeText(getContext(), "서버 응답 실패", Toast.LENGTH_SHORT).show();
                    Log.e("Download error", "Download failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 이미지 다운로드 중 오류가 발생한 경우 기본 이미지를 설정합니다.
                imgprofile.setImageResource(R.drawable.ic_default_profile);
                Toast.makeText(getContext(), "다운로드 오류", Toast.LENGTH_SHORT).show();
                Log.e("Download error", "Download failed: " + t.getMessage());
            }
        });
    }

}