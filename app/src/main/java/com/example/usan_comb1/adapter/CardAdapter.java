package com.example.usan_comb1.adapter;

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
import com.example.usan_comb1.activity.product.UpdateActivity;
import com.example.usan_comb1.response.RetroProduct;
import com.example.usan_comb1.utilities.Constants;

import java.io.InputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상세페이지 내부 작성자 판매 물품 Adapter
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private Context context;
    private List<RetroProduct> cardList;
    private ProductService mProductService;
    private String accessToken;

    public CardAdapter(Context context, List<RetroProduct> cardList) {
        this.context = context;
        this.cardList = cardList;

        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_sell_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RetroProduct cardlist = cardList.get(position);
        holder.txtView.setText(cardlist.getTitle());

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        // 이미지 다운로드 메소드 호출
        downloadImage(accessToken, cardlist.getProductId(), cardlist.getImg(), holder.imageView);
    }

    @Override
    public int getItemCount() {
        return cardList != null ? cardList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cardimg);
            txtView = itemView.findViewById(R.id.txtview);
        }
    }

    // 이미지 다운로드
    private void downloadImage(String accessToken, int productId, String filename, ImageView imageView) {
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
                        imageView.setImageBitmap(bitmap);
                    } else {
                        // 이미지 데이터가 없는 경우 기본 이미지를 설정합니다.
                        imageView.setImageResource(R.drawable.img_error);
                        Log.e("Download error", "Download failed: " + response.message());
                    }
                } else {
                    // 서버 응답이 실패인 경우 기본 이미지를 설정합니다.
                    imageView.setImageResource(R.drawable.img_error);
                    Log.e("Download error", "Download failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 이미지 다운로드 중 오류가 발생한 경우 기본 이미지를 설정합니다.
                imageView.setImageResource(R.drawable.img_error);
                Log.e("Download error", "Download failed: " + t.getMessage());
            }
        });
    }


}