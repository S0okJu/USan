package com.example.usan_comb1.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.List;

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
        DownImage downImage = new DownImage();

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        if (Constants.BASE_URL + data.getImg() != null) {
            Glide.with(activity)
                    .load(Constants.BASE_URL + data.getImg())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.coverImage);
        } else {
            // 이미지가 null인 경우, 기본 이미지 또는 에러 이미지를 설정해 줄 수 있습니다.
            Glide.with(activity)
                    .load(R.drawable.error)
                    .into(holder.coverImage);
        }



        holder.txtTitle.setText(data.getTitle());
        holder.txtAuthor.setText(data.getAuthor());
        //holder.txtPrice.setText(data.getPrice() + "원");


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
}