package com.example.usan_comb1.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.DownImage;
import com.example.usan_comb1.response.FavoriteProduct;
import com.example.usan_comb1.response.PostList;
import com.example.usan_comb1.response.RetroProduct;
import com.example.usan_comb1.utilities.Constants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 관심 목록 RecyclerView Adapter
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder>
{
    private List<FavoriteProduct> favoriteList;
    //private ArrayList<FavoriteProduct> dataArrayList;
    private Activity activity;
    private Context context;
    public boolean isFavorite;
    private ProductService mProductService;
    private Integer productId;
    private String accessToken;

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(int position, FavoriteProduct data);
    }

    private OnItemClickListener listener;

    // Set item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public FavoriteAdapter(Activity activity, List<FavoriteProduct> favoriteList)
    {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.favoriteList = favoriteList;

        SharedPreferences prefs = activity.getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
    }



    @NonNull
    @Override
    public FavoriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteAdapter.ViewHolder holder, int position)
    {
        FavoriteProduct data = favoriteList.get(position);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        holder.txtTitle.setText(data.getTitle());
        holder.txtAuthor.setText(data.getAuthor());
        //holder.txtPrice.setText(data.getPrice() + "원");

        // 이미지 다운로드 메소드 호출
        downloadImage(accessToken, data.getProductId(), data.getImg(), holder.coverImage);


        // 추가되어 있는 관심물품을 다시 클릭하여 삭제하는 작업
        holder.btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productId = data.getProductId(); // productId 값을 저장합니다.

                isFavorite = !isFavorite;
                if (isFavorite) {
                    holder.btnFavorite.setImageResource(R.drawable.unselect_ic_heart);
                    removeFavorite(productId);
                } else {
                    holder.btnFavorite.setImageResource(R.drawable.select_ic_heart);
                    addFavorite(productId);
                }
            }
        });

        // 아이템 클릭 리스너를 설정합니다.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, data);
            }
        });


    }

    @Override
    public int getItemCount()
    {
        if(favoriteList != null) {
            return favoriteList.size();
        } else {
            return 1;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView coverImage, btnFavorite;
        TextView txtTitle, txtAuthor, txtPrice, emptyView;

        public ViewHolder(@NonNull View view)
        {
            super(view);

            coverImage = view.findViewById(R.id.fav_image);
            txtTitle = view.findViewById(R.id.fav_title);
            txtAuthor = view.findViewById(R.id.fav_author);
            //txtPrice = view.findViewById(R.id.fav_price);
            btnFavorite = view.findViewById(R.id.fav_btn);
        }
    }

    // 토스트 메시지를 출력하는 메서드
    private void showToast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    // 관심상품 목록에 추가하는 메서드
    public void addFavorite(Integer productId) {

        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");

        Call<Void> call = mProductService.setFavorite(accessToken, productId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // * body !=null 삭제
                if (response.isSuccessful()) {
                    showToast("관심물품으로 등록되었습니다.");
                }
                else {
                    showToast("서버에서 정상적으로 처리되지 않았습니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("서버 에러가 발생하였습니다.");
            }
        });
    }

    // 관심상품 목록에서 제거하는 메서드
    private void removeFavorite(Integer productId) {
        // 서버와 통신하여 즐겨찾기 목록에서 제거
        // Retrofit2

        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");

        Call<Void> call = mProductService.unFavorite(accessToken, productId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("관심물품에서 제거되었습니다.");
                } else {
                    showToast("서버에서 정상적으로 처리되지 않았습니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("서버 에러가 발생하였습니다.");
            }
        });
    }

    // 이미지 다운로드
    private void downloadImage(String accessToken, int productId, String filename, ImageView coverImage) {
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
                        coverImage.setImageBitmap(bitmap);
                    } else {
                        // 이미지 데이터가 없는 경우 기본 이미지를 설정합니다.
                        coverImage.setImageResource(R.drawable.img_error);
                        Toast.makeText(context, "이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                        Log.e("Download error", "Download failed: " + response.message());
                    }
                } else {
                    // 서버 응답이 실패인 경우 기본 이미지를 설정합니다.
                    coverImage.setImageResource(R.drawable.img_error);
                    Toast.makeText(context, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                    Log.e("Download error", "Download failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 이미지 다운로드 중 오류가 발생한 경우 기본 이미지를 설정합니다.
                coverImage.setImageResource(R.drawable.img_error);
                Toast.makeText(context, "다운로드 오류", Toast.LENGTH_SHORT).show();
                Log.e("Download error", "Download failed: " + t.getMessage());
            }
        });
    }
}