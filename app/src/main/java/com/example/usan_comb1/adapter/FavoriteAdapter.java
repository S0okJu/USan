package com.example.usan_comb1.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.usan_comb1.FavoriteProduct;
import com.example.usan_comb1.R;
import com.example.usan_comb1.request.DownImage;
import com.example.usan_comb1.response.PostList;

import java.util.ArrayList;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private ArrayList<FavoriteProduct> mFavoriteProducts;
    private Context mContext;

    public FavoriteAdapter(Context context, ArrayList<FavoriteProduct> favoriteProducts) {
        mContext = context;
        mFavoriteProducts = favoriteProducts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteProduct favoriteProduct = mFavoriteProducts.get(position);

        holder.titleTextView.setText(favoriteProduct.getTitle());
        holder.authorTextView.setText(favoriteProduct.getAuthor());
        //holder.priceTextView.setText(String.valueOf(favoriteProduct.getPrice()));

        Glide.with(mContext)
                .load(favoriteProduct.getImageUrl())
                .placeholder(R.drawable.error)
                .into(holder.coverImageView);
    }

    @Override
    public int getItemCount() {
        return mFavoriteProducts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView coverImageView;
        public TextView titleTextView;
        public TextView authorTextView;
        //public TextView priceTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            coverImageView = itemView.findViewById(R.id.adpt_coverImage);
            titleTextView = itemView.findViewById(R.id.adpt_title);
            authorTextView = itemView.findViewById(R.id.adpt_author);
        }
    }
}