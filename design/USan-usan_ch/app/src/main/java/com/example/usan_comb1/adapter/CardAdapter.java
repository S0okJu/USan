package com.example.usan_comb1.adapter;

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
import com.example.usan_comb1.R;
import com.example.usan_comb1.response.RetroProduct;
import com.example.usan_comb1.utilities.Constants;

import java.util.List;

// 상세페이지 내부 작성자 판매 물품 Adapter
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private Context context;
    private List<RetroProduct> cardList;

    public CardAdapter(Context context, List<RetroProduct> cardList) {
        this.context = context;
        this.cardList = cardList;
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

        if (Constants.BASE_URL + cardlist.getImg() != null) {
            Glide.with(context)
                    .load(Constants.BASE_URL + cardlist.getImg())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
        } else {
            // 이미지가 null인 경우, 기본 이미지 또는 에러 이미지를 설정해 줄 수 있습니다.
            Glide.with(context)
                    .load(R.drawable.error)
                    .into(holder.imageView);
        }
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
}
