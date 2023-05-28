package com.example.usan_comb1.map;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.usan_comb1.R;

public class ListOnlineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtEmail;
    ItemClickListener itemClickListener;
    public ListOnlineViewHolder(View itemView){
        super(itemView);
        txtEmail = (TextView) itemView.findViewById(R.id.txt_email);
    }
    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition());
    }
}
