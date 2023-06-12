package com.example.usan_comb1.adapter;

import android.app.Activity;
import android.content.Context;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.DownImage;
import com.example.usan_comb1.response.PostList;

import java.io.File;
import java.util.ArrayList;

// HomeFragment RecyclerView Adapter
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder>
{
    private ArrayList<PostList> dataArrayList;
    private Activity activity;
    private ProductService mProductService;

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
        PostList data = dataArrayList.get(position);
        String fileName = data.getImg(); // 이미지 파일 이름
        SharedPreferences pref = activity.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("image_file_name", fileName);
        editor.apply();

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        SharedPreferences prefs = activity.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        fileName = prefs.getString("image_file_name", "");

        if (!TextUtils.isEmpty(fileName)) {
            File imageFile = new File(activity.getFilesDir(), fileName);

            Glide.with(activity)
                    .load(imageFile)
                    .into(holder.coverImage);
        } else {
            // 이미지 파일 이름이 저장되지 않은 경우 기본 이미지 또는 에러 이미지 로드
            Glide.with(activity)
                    .load(R.drawable.img_error)
                    .into(holder.coverImage);
        }

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
}
