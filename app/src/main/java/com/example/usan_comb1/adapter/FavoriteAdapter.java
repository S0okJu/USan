package com.example.usan_comb1.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.usan_comb1.R;
import com.example.usan_comb1.request.DownImage;
import com.example.usan_comb1.response.FavoriteProduct;
import com.example.usan_comb1.response.PostList;
import com.example.usan_comb1.response.RetroProduct;

import java.util.ArrayList;
import java.util.List;

// 관심 목록 RecyclerView Adapter
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder>
{
    private List<FavoriteProduct> favoriteList;
    private ArrayList<FavoriteProduct> dataArrayList;
    private Activity activity;

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(int position, FavoriteProduct data);
    }

    private OnItemClickListener listener;

    // Set item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public FavoriteAdapter(Activity activity, ArrayList<FavoriteProduct> dataArrayList)
    {
        this.activity = activity;
        this.dataArrayList = dataArrayList;
    }

    @NonNull
    @Override
    public FavoriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteAdapter.ViewHolder holder, int position)
    {
        FavoriteProduct data = dataArrayList.get(position);
        DownImage downImage = new DownImage();

        if (downImage.getFilename() != null) {
            Glide.with(activity)
                    .load(data.getImg())
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

        if(dataArrayList != null) {
            return dataArrayList.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView coverImage;
        TextView txtTitle, txtAuthor;

        public ViewHolder(@NonNull View view)
        {
            super(view);

            coverImage = view.findViewById(R.id.adpt_coverImage);
            txtTitle = view.findViewById(R.id.adpt_title);
            txtAuthor = view.findViewById(R.id.adpt_author);
        }
    }
}