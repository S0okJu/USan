package com.example.usan_comb1.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.product.AuthorSellProductDetailActivity;
import com.example.usan_comb1.response.RetroProduct;

import java.io.File;
import java.util.List;

// 상세페이지 내부 작성자 판매 물품 Adapter
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private Context context;
    private static List<RetroProduct> cardList;
    private ProductService mProductService;

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
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RetroProduct cardlist = cardList.get(position);
        holder.txtView.setText(cardlist.getTitle());


        String fileName = cardlist.getImg(); // 이미지 파일 이름
        SharedPreferences pref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("image_file_name", fileName);
        editor.apply();

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        SharedPreferences prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        fileName = prefs.getString("image_file_name", "");

        if (!TextUtils.isEmpty(fileName)) {
            File imageFile = new File(context.getFilesDir(), fileName);

            Glide.with(context)
                    .load(imageFile)
                    .into(holder.imageView);
        } else {
            // 이미지 파일 이름이 저장되지 않은 경우 기본 이미지 또는 에러 이미지 로드
            Glide.with(context)
                    .load(R.drawable.img_error)
                    .into(holder.imageView);
        }
    }

    public void setUserPosts(List<RetroProduct> userPosts) {
        this.cardList = userPosts;
        notifyDataSetChanged();
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
            imageView = itemView.findViewById(R.id.image);
            txtView = itemView.findViewById(R.id.title);

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

        // 이미지 다운로드 메서드
        private void downloadImage(String imageUrl) {
            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .into(imageView);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_sell_item, parent, false);
        return new ViewHolder(view, listener);
    }

}
