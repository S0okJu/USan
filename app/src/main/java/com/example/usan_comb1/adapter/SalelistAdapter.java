package com.example.usan_comb1.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.UpdateActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Salelist Adapter
public class SalelistAdapter extends RecyclerView.Adapter<SalelistAdapter.CustomViewHolder> {
    private Activity activity;
    private Context context;
    private ProductService mProductService;
    private Integer productId;
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
    public void onBindViewHolder(@NonNull CustomViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (position >= 0 && position < productList.size()) {
            //...
            com.example.usan_comb1.response.RetroProduct product = productList.get(position);
            Log.d("SalelistAdapter", "onBindViewHolder: position=" + position + ", title=" + product.getTitle());

            mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

            // 상품명과 가격, 상태를 출력하는 코드 추가
            holder.tvName.setText(product.getTitle());
            holder.tvPrice.setText(String.valueOf(product.getPrice()) + "원"); // 수정된 코드
            holder.tvStatus.setText(String.valueOf(product.getStatus()));

            // 삭제 버튼 클릭 이벤트 처리
            holder.btndelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 삭제 버튼 클릭 시 처리할 내용을 여기에 작성하세요.
                    // 해당 게시물의 ID를 가져옴 (예시로 "product_id"라고 가정)
                    productId = product.getProductId();
                    System.out.println(productId);

                    // 삭제 요청을 보낼 때 필요한 인증 토큰 (accessToken)을 가져옴
                    SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                    String accessToken = prefs.getString("access_token", "");

                    // ProductService 인터페이스의 deletePost() 메서드를 호출하여 DELETE 요청 전송
                    Call<Void> call = mProductService.deletePost(accessToken, productId);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                // 삭제 성공 처리
                                // 예: Toast 메시지를 통한 사용자에게 알림
                                Toast.makeText(context, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                                // 삭제 후에는 필요한 작업을 수행하도록 구현
                                // 예: 삭제된 항목을 리스트에서 제거하고, UI 업데이트 등
                            } else {
                                // 삭제 실패 처리
                                // 예: 서버 응답에 따른 오류 처리
                                Toast.makeText(context, "게시물 삭제 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // 통신 실패 처리
                            // 예: 네트워크 연결 오류 등
                            Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            //...
        } else {
            Log.e("SalelistAdapter", "Invalid position: " + position);
        }


        // 상태 버튼 클릭 이벤트 처리
        holder.btnstate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 상태 버튼 클릭 시 처리할 내용을 여기에 작성하세요.
                // 예: 상태 변경 작업 수행 및 결과 처리
            }
        });

        // 수정 버튼 클릭 이벤트 처리
        holder.btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 업데이트 버튼 클릭 시 처리할 내용을 여기에 작성하세요.
                // 예: 데이터 업데이트 작업 수행 및 결과 처리

                Intent intent = new Intent(activity, UpdateActivity.class);
                activity.startActivity(intent);
            }
        });


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
        Button btnstate, btndelete, btnupdate;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.image);
            tvName = itemView.findViewById(R.id.title);
            tvPrice = itemView.findViewById(R.id.price);
            tvStatus = itemView.findViewById(R.id.status);
            btnstate = itemView.findViewById(R.id.btnstatechange);
            btnupdate = itemView.findViewById(R.id.btnupdate);
            btndelete = itemView.findViewById(R.id.btndelete);
        }
    }
}