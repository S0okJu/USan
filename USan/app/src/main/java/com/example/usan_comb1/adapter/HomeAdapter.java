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
import com.example.usan_comb1.response.PostList;
import com.example.usan_comb1.utilities.Constants;

import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// HomeFragment RecyclerView Adapter
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder>
{
    private ArrayList<PostList> dataArrayList;
    private Activity activity;
    private ProductService mProductService;
    private String accessToken;

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(View view, int position, PostList data);
    }



    private OnItemClickListener listener;

    // Set item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HomeAdapter(Activity activity, ArrayList<PostList> dataArrayList)
    {
        this.activity = activity;
        this.dataArrayList = dataArrayList;

        SharedPreferences prefs = activity.getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
    }

    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.ViewHolder holder, int position)
    {

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        PostList data = dataArrayList.get(position);

        // 이미지 다운로드 메소드 호출
        downloadImage(accessToken, data.getProduct_id(), data.getImg(), holder.coverImage);

        holder.txtTitle.setText(data.getTitle());
        holder.txtAuthor.setText(data.getAuthor());
        holder.txtPrice.setText(data.getPrice()+"원");

        // 아이템 클릭 리스너를 설정합니다.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(v, position, data);
            }
        });
    }

    @Override
    public int getItemCount()
    {

        if(dataArrayList != null) {
            return dataArrayList.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView coverImage;
        TextView txtTitle, txtAuthor, txtPrice;

        public ViewHolder(@NonNull View view)
        {
            super(view);

            coverImage = view.findViewById(R.id.adpt_coverImage);
            txtTitle = view.findViewById(R.id.adpt_title);
            txtAuthor = view.findViewById(R.id.adpt_author);
            txtPrice = view.findViewById(R.id.adpt_price);
        }
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
                        Toast.makeText(activity, "이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                        Log.e("Download error", "Download failed: " + response.message());
                    }
                } else {
                    // 서버 응답이 실패인 경우 기본 이미지를 설정합니다.
                    coverImage.setImageResource(R.drawable.img_error);
                    Toast.makeText(activity, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                    Log.e("Download error", "Download failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 이미지 다운로드 중 오류가 발생한 경우 기본 이미지를 설정합니다.
                coverImage.setImageResource(R.drawable.img_error);
                Toast.makeText(activity, "다운로드 오류", Toast.LENGTH_SHORT).show();
                Log.e("Download error", "Download failed: " + t.getMessage());
            }
        });
    }
}