package com.example.usan_comb1.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.product.UpdateActivity;
import com.example.usan_comb1.response.RetroProduct;
import com.example.usan_comb1.utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Salelist Adapter
public class SalelistAdapter extends RecyclerView.Adapter<SalelistAdapter.CustomViewHolder> {
    private Activity activity;
    private Context context;
    private ProductService mProductService;
    private Integer productId;
    private String accessToken;
    private List<com.example.usan_comb1.response.RetroProduct> productList;
    private ArrayList<com.example.usan_comb1.response.RetroProduct> dataArrayList;

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(int position, RetroProduct data);
    }

    private SalelistAdapter.OnItemClickListener listener;

    // Set item click listener
    public void setOnItemClickListener(SalelistAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public SalelistAdapter(Activity activity, List<com.example.usan_comb1.response.RetroProduct> productList) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.productList = productList;
        this.dataArrayList = new ArrayList<>(productList);

        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
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
            RetroProduct product = productList.get(position);
            Log.d("SalelistAdapter", "onBindViewHolder: position=" + position + ", title=" + product.getTitle());

            mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

            // 이미지 다운로드 메소드 호출
            downloadImage(accessToken, product.getProductId(), 1, holder.coverImage);

            // 상품명과 가격, 상태를 출력하는 코드 추가
            holder.tvName.setText(product.getTitle());
            holder.tvPrice.setText(String.valueOf(product.getPrice()) + "원");  // 수정된 코드
            holder.tvStatus.setText("판매 중");

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(position, product);
                }
            });

            // 삭제 버튼 클릭 이벤트 처리
            holder.btndelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 삭제 버튼 클릭 시 처리할 내용을 여기에 작성하세요.
                    // 해당 게시물의 ID를 가져옴 (예시로 "product_id"라고 가정)
                    productId = product.getProductId();

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
                            Log.e("error", "failed: " + t.getMessage());
                        }
                    });
                }
            });

            holder.btnstate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    productId = product.getProductId();

                    AlertDialog.Builder dialog = new AlertDialog.Builder(activity, R.style.MyDialogTheme);
                    dialog.setTitle("판매 현황 설정");

                    boolean isSoldOut = product.getStatus(); // 현재 판매 상태 가져오기

                    if (isSoldOut) {
                        // 현재 판매 완료 상태이므로 판매 중으로 변경
                        dialog.setPositiveButton("판매 중", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 변경 요청을 보낼 때 필요한 인증 토큰 (accessToken)을 가져옴
                                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                                String accessToken = prefs.getString("access_token", "");

                                Call<Void> call = mProductService.unStatus(accessToken, productId);
                                call.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            // 상태 변경 성공 처리
                                            // 예: Toast 메시지를 통한 사용자에게 알림
                                            Toast.makeText(context, "상태가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                            holder.tvStatus.setText("판매 중");
                                            product.setStatus(false); // 판매 상태 업데이트
                                        } else {
                                            // 상태 변경 실패 처리
                                            // 예: 서버 응답에 따른 오류 처리
                                            Log.e("error", "failed: " + response.message());
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
                    } else {
                        // 현재 판매 중인 상태이므로 판매 완료로 변경
                        dialog.setPositiveButton("판매 완료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 변경 요청을 보낼 때 필요한 인증 토큰 (accessToken)을 가져옴
                                SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
                                String accessToken = prefs.getString("access_token", "");

                                Call<Void> call = mProductService.setStatus(accessToken, productId);
                                call.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            // 상태 변경 성공 처리
                                            // 예: Toast 메시지를 통한 사용자에게 알림
                                            Toast.makeText(context, "상태가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                            holder.tvStatus.setText("판매 완료");
                                            //product.setStatus(true); // 판매 상태 업데이트
                                        } else {
                                            // 상태 변경 실패 처리
                                            // 예: 서버 응답에 따른 오류 처리
                                            Toast.makeText(context, "상태 변경 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        // 통신 실패 처리
                                        // 예: 네트워크 연결 오류 등
                                        Log.e("error", "failed: " + t.getMessage());
                                    }
                                });
                            }
                        });
                    }

                    dialog.show();
                }
            });

            // 업데이트 버튼 클릭 이벤트 처리
            holder.btnupdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 업데이트 버튼 클릭 시 처리할 내용을 여기에 작성하세요.
                    // 예: 데이터 업데이트 작업 수행 및 결과 처리
                    // 업데이트 버튼 클릭 시 처리할 내용을 여기에 작성하세요.
                    // 예: 데이터 업데이트 작업 수행 및 결과 처리

                    productId = product.getProductId();

                    Intent intent = new Intent(activity, UpdateActivity.class);
                    intent.putExtra("productId", productId);
                    activity.startActivity(intent);
                }
            });

            //...
        }
        else {
            Log.e("SalelistAdapter", "Invalid position: " + position);
        }
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
            btnstate = itemView.findViewById(R.id.btnstate);
            btnupdate = itemView.findViewById(R.id.btnupdate);
            btndelete = itemView.findViewById(R.id.btndelete);
        }
    }

    // 이미지 다운로드
    private void downloadImage(String accessToken, int productId, int num, ImageView coverImage) {
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
                                        .into(coverImage);
                            } else {
                                // 이미지 데이터가 없는 경우 기본 이미지를 설정합니다.
                                coverImage.setImageResource(R.drawable.img_error);
                                Log.e("Download error", "Download failed: No image URLs available");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 이미지 데이터가 없는 경우 기본 이미지를 설정합니다.
                        coverImage.setImageResource(R.drawable.img_error);
                        Log.e("Download error", "Download failed: " + response.message());
                    }
                } else {
                    // 서버 응답이 실패인 경우 기본 이미지를 설정합니다.
                    coverImage.setImageResource(R.drawable.img_error);
                    Log.e("Download error", "Download failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 이미지 다운로드 중 오류가 발생한 경우 기본 이미지를 설정합니다.
                coverImage.setImageResource(R.drawable.img_error);
                Log.e("Download error", "Download failed: " + t.getMessage());
            }
        });
    }
}