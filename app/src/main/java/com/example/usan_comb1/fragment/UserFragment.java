package com.example.usan_comb1.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.FavoriteActivity;
import com.example.usan_comb1.activity.ProfileActivity;
import com.example.usan_comb1.activity.SalelistActivity;
import com.example.usan_comb1.activity.UploadActivity;
import com.example.usan_comb1.activity.UserActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFragment extends Fragment {

    private Button btnviewprf, btnsalelist;
    private ImageView imageView;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);


        btnviewprf = view.findViewById(R.id.btnviewprf);
        btnsalelist = view.findViewById(R.id.btnsalelist);
        imageView = view.findViewById(R.id.imageView);

        btnviewprf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserActivity.class);
                startActivity(intent);
            }
        });

        btnsalelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent 정보를 넘겨줌
                Intent intent = getActivity().getIntent();
                String username = intent.getStringExtra("username");

                Intent salelistIntent = new Intent(getActivity(), SalelistActivity.class);
                salelistIntent.putExtra("username", username);
                startActivity(salelistIntent);
            }
        });
        return view;
    }

}