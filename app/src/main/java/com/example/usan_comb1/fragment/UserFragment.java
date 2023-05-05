package com.example.usan_comb1.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.FavoriteActivity;
import com.example.usan_comb1.activity.UploadActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFragment extends Fragment {

    private Button delete_button, update_button, state_button, favorite_button;

    private ProductService mProductService;
    private Integer productId;

    private boolean isFinish = false;

    public UserFragment() {
        // Required empty public constructor
    }

    public static UserFragment newInstance() {
        UserFragment fragment = new UserFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        delete_button = view.findViewById(R.id.delete_button);
        update_button = view.findViewById(R.id.update_button);
        state_button = view.findViewById(R.id.state_button);
        favorite_button = view.findViewById(R.id.favorite_button);

        favorite_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FavoriteActivity.class);
                startActivity(intent);
            }
        });
        // 삭제 버튼 클릭 이벤트 핸들러
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("게시글 삭제")
                .setMessage("게시글을 삭제하시겠습니까?")
                        // setNegative - 왼쪽 / setPositive - 오른쪽
                        .setNegativeButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deletePost(); // 예 버튼을 눌렀을 때 게시글 삭제 메소드 호출
                            }
                        })
                        .setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 아니오 버튼을 눌렀을 때 아무 동작도 하지 않음
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

                // 내 게시글인지 확인
                //checkIsMyPost();
            }
        });

        /*

        // 수정 버튼 클릭 이벤트 핸들러
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onItemClick(View view, int position, PostList data) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra("product_id", data.getProduct_id());// 넘어갈 데이터를 인텐트에 추가합니다.
                    startActivity(intent);
                }
        });
                 */

        state_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = new String[]{"판매 중", "판매 완료"};
                final int[] selectedIndex = {0};

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("거래 상태를 선택하세요.")
                        .setSingleChoiceItems(
                                items,
                                0,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        selectedIndex[0] = which;
                                    }
                                }
                        )
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0) {
                                    statePost();
                                }
                                else {
                                    unstatePost();
                                }
                            }
                        }).create().show();
            }
        });

        return view;
    }

    private void deletePost() {
        Call<Void> call = mProductService.deletePost(productId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("게시글이 삭제되었습니다.");
                }
                else {
                    showToast("서버에서 정상적으로 처리되지 않았습니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("서버 에러가 발생하였습니다.");
            }
        });
    }

    /*
    // 게시글 작성자의 ID와 현재 사용자의 ID를 비교하여 내 게시글인지 확인
    private void checkIsMyPost() {
        if (productId != null) {
            Call<PostResult> call = mProductService.getProduct(productId);
            call.enqueue(new Callback<Post>() {
                @Override
                public void onResponse(Call<Post> call, Response<Post> response) {
                    if (response.isSuccessful()) {
                        // 현재 사용자의 ID와 게시글 작성자의 ID를 비교하여 내 게시글인지 확인
                        if (response.body().getUserId() == currentUserId) {
                            // 내 게시글이면 삭제 버튼 보여주기
                            deleteButton.setVisibility(View.VISIBLE);
                        } else {
                            // 다른 사람의 게시글이면 삭제 버튼 숨기기
                            deleteButton.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Post> call, Throwable t) {
                    Log.e(TAG, "게시글 불러오기 실패: " + t.getMessage());
                }
            });
        }
    }
     */

    private void statePost() {
        Call<Void> call = mProductService.setStatus(productId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    showToast("게시글 처리가 완료되었습니다.");
                } else {
                    showToast("서버에서 정상적으로 처리되지 않았습니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                    showToast("서버 에러가 발생하였습니다.");
            }
        });

    }

    private void unstatePost() {
        showToast("완료되었습니다.");
    }


    // 토스트 메시지를 출력하는 메서드
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}