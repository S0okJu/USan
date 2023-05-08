package com.example.usan_comb1.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usan_comb1.R;

import java.util.ArrayList;
import java.util.List;

// Salelist Adapter
public class SalelistAdapter extends RecyclerView.Adapter<SalelistAdapter.CustomViewHolder> {
    private Activity activity;
    private Context context;
    private List<com.example.usan_comb1.response.RetroProduct> productList;
    private ArrayList<com.example.usan_comb1.response.RetroProduct> dataArrayList;


    public SalelistAdapter(Activity activity, List<com.example.usan_comb1.response.RetroProduct> productList) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.productList = productList;
        this.dataArrayList = new ArrayList<>(productList);
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        com.example.usan_comb1.response.RetroProduct product = productList.get(position);
        Log.d("SalelistAdapter", "onBindViewHolder: position=" + position + ", title=" + product.getTitle());

        // 상품명과 가격, 상태를 출력하는 코드 추가
        holder.tvName.setText(product.getTitle());
        holder.tvPrice.setText(String.valueOf(product.getPrice())); // 수정된 코드
        holder.tvStatus.setText(String.valueOf(product.getStatus()));

        com.example.usan_comb1.response.RetroProduct data = productList.get(position);

        /*
        DownImage downImage = new DownImage(data.getImg());

        if (downImage.getFilename() != null) {
            Glide.with(activity)
                    .load(downImage.getFilename())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.coverImage);
        } else {
            Glide.with(activity)
                    .load(R.drawable.error)
                    .into(holder.coverImage);
        }


         */
    }
    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    public void setProductList(List<com.example.usan_comb1.response.RetroProduct> productList) {
        if (productList != null) {
            this.productList.clear();
            this.productList.addAll(productList);
            this.dataArrayList = new ArrayList<>(productList);
            notifyDataSetChanged();
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvStatus;
        ImageView coverImage;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.image);
            tvName = itemView.findViewById(R.id.title);
            tvPrice = itemView.findViewById(R.id.price);
            tvStatus = itemView.findViewById(R.id.status);

        }
    }
}