package com.example.usan_comb1.adapter;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.example.usan_comb1.activity.product.AuthorSellProductDetailActivity;
import com.example.usan_comb1.activity.product.UpdateActivity;
import com.example.usan_comb1.response.RetroProduct;
import com.example.usan_comb1.utilities.Constants;
import com.example.usan_comb1.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상세페이지 내부 작성자 판매 물품 Adapter
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private Context context;
    private static List<RetroProduct> cardList;
    private ProductService mProductService;
    private String accessToken;


    public interface OnItemClickListener {
        void onItemClick(int productId);
    }

    private static OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

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
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RetroProduct cardlist = cardList.get(position);
        holder.txtView.setText(cardlist.getTitle());

        // 이 부분 추가
        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(cardlist.getProductId());
                }
            });
        }

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        // 이미지 다운로드 메소드 호출
        downloadImage(accessToken, cardlist.getProductId(), 1, holder.imageView);
    }

    @Override
    public int getItemCount() {
        return cardList != null ? cardList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtView;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cardimg);
            txtView = itemView.findViewById(R.id.txtview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        RetroProduct cardlist = cardList.get(position);
                        int productId = cardlist.getProductId();

                        if (CardAdapter.listener != null) {
                            CardAdapter.listener.onItemClick(productId);
                        }

                        // 클릭된 아이템의 productId를 전달하고, AuthorSellProductDetailActivity로 전환
                        Intent intent = new Intent(itemView.getContext(), AuthorSellProductDetailActivity.class);
                        intent.putExtra("product_id", productId);
                        itemView.getContext().startActivity(intent);
                    }
                }
            });

        }
    }

    // 이미지 다운로드
    private void downloadImage(String accessToken, int productId, int num, ImageView imageView) {
        Call<ResponseBody> call = mProductService.downloadImage(accessToken, productId, num);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        try {
                            String jsonString = responseBody.string();
                            System.out.println(jsonString);
                            JSONObject jsonObject = new JSONObject(jsonString);
                            System.out.println(jsonObject);
                            JSONArray imageUrls = jsonObject.getJSONArray("imgs");

                            if (imageUrls != null && imageUrls.length() > 0) {
                                String imageUrl = Constants.BASE_URL + imageUrls.getString(0);// 첫 번째 이미지 URL 가져오기
                                System.out.println(imageUrl);
                                Glide.with(context)
                                        .load(imageUrl)
                                        .into(imageView);
                            } else {
                                // 이미지 데이터가 없는 경우 기본 이미지를 설정합니다.
                                imageView.setImageResource(R.drawable.img_error);
                                Log.e("Download error", "Download failed: No image URLs available");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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