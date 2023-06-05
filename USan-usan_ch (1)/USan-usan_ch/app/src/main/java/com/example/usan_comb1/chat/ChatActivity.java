package com.example.usan_comb1.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.usan_comb1.R;
import com.example.usan_comb1.chat.model.Users;
import com.example.usan_comb1.databinding.ChatActivitySampleBinding;
import com.example.usan_comb1.map.ListOnline;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ChatActivitySampleBinding binding;
    private Users receiverUser;
    private List<ChatData> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    ImageButton buttonFile;

    private static final int REQUEST_CODE_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChatActivitySampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();

        //button_file(이미지 전송) 클릭 이벤트
        buttonFile = findViewById(R.id.button_file);
        buttonFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 파일 선택 또는 카메라 촬영을 위한 Intent 생성
                Intent intent = new Intent();
                intent.setType("image/*");  // 이미지 파일 필터링
                intent.setAction(Intent.ACTION_GET_CONTENT);  // 파일 선택 Intent
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_IMAGE);
            }
        });


    }

    private void init() {
        preferenceManager = new PreferenceManager(this);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, preferenceManager.getString("userId"));
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put("senderId", preferenceManager.getString("userId"));
        message.put("receiverId", receiverUser.getId());
        message.put("message", binding.inputMessage.getText().toString());
        message.put("timestamp", new Date());
        database.collection("chats").add(message);
        binding.inputMessage.setText(null);
    }

    private void listenMessages() {
        database.collection("chats")
                .whereEqualTo("senderId", preferenceManager.getString("userId"))
                .whereEqualTo("receiverId", receiverUser.getId())
                .addSnapshotListener(eventListener);

        database.collection("chats")
                .whereEqualTo("senderId", receiverUser.getId())
                .whereEqualTo("receiverId", preferenceManager.getString("userId"))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatData chatData = new ChatData();
                    chatData.setSenderId(documentChange.getDocument().getString("senderId"));
                    chatData.setReceiverId(documentChange.getDocument().getString("receiverId"));
                    chatData.setMessage(documentChange.getDocument().getString("message"));
                    chatAdapter.addChat(chatData); // 메시지 추가
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                }
            }
        }
    };

    private void loadReceiverDetails() {
        receiverUser = (Users) getIntent().getSerializableExtra("user");
        binding.textName.setText(receiverUser.getName());
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }


    // onActivityResult 메서드 추가
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // 선택한 이미지를 가져옴
            Uri imageUri = data.getData();

            // 이미지 메시지 전송
            sendImageMessage(imageUri);
        }
    }

    private void sendImageMessage(Uri imageUri) {
        // Firebase Storage에 이미지 업로드
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("chat_images");
        String fileName = "image_" + System.currentTimeMillis() + getFileExtension(imageUri);
        StorageReference fileRef = storageReference.child(fileName);

        fileRef.  putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // 이미지 업로드 성공
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // 메시지 데이터 생성
                        ChatData chatData = new ChatData();
                        chatData.setSenderId(preferenceManager.getString("userId"));
                        chatData.setReceiverId(receiverUser.getId());
                        chatData.setMessage(imageUrl); // 이미지 URL 저장

                        // Firestore에 메시지 저장
                        database.collection("chats").document().set(chatData);

                        // RecyclerView에 메시지 추가
                        chatAdapter.addChat(chatData);
                        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                    });
                })
                .addOnFailureListener(e -> {
                    // 이미지 업로드 실패
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}
