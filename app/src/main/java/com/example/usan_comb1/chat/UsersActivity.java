package com.example.usan_comb1.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.chat.model.Users;
import com.example.usan_comb1.databinding.ActivityChatUsersBinding;
import com.example.usan_comb1.listeners.UserListener;
import com.example.usan_comb1.map.User;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UsersActivity extends AppCompatActivity implements UserListener{
    private ActivityChatUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityChatUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
        // Preference
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection("users").get().addOnCompleteListener(task->{
            loading(false);
            String curUserId = PreferenceManager.getString("userId");

            if(task.isSuccessful() && task.getResult() != null){
                List<Users> users = new ArrayList<>();
                for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                    if(Objects.equals(curUserId, queryDocumentSnapshot.getId())){
                        continue;
                    }
                    Users user = new Users();
                    user.name = queryDocumentSnapshot.getString("username");
//                    user.email = queryDocumentSnapshot.getString("email");
                    user.id = queryDocumentSnapshot.getId();
                    users.add(user);
                }
                if(users.size() > 0){
                    UsersAdapter usersAdapter = new UsersAdapter(users, this);
                    binding.usersRecyclerView.setAdapter(usersAdapter);
                    binding.usersRecyclerView.setVisibility(View.VISIBLE);
                }else{
                    System.out.println("0..");
                }
            }
        });
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(Users user){
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("user",user);
        startActivity(intent);
        finish();
    }

}
