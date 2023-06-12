package com.example.usan_comb1.activity.chat;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.usan_comb1.utilities.Constants.BUYER;
import static com.example.usan_comb1.utilities.Constants.SELLER;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.R;
import com.example.usan_comb1.activity.map.MapTracking;
import com.example.usan_comb1.adapter.ChatAdapter;
import com.example.usan_comb1.databinding.ChatActivitySampleBinding;
import com.example.usan_comb1.models.ChatData;
import com.example.usan_comb1.models.Users;
import com.example.usan_comb1.utilities.FindFromFirebase;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private ChatActivitySampleBinding binding;
    public Users receiverUser;
    private List<ChatData> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    public DatabaseReference transRef;
    public FindFromFirebase findFromFirebase = new FindFromFirebase();
    ImageButton buttonFile;
    ImageButton buttonGps;
    private static final int REQUEST_CODE_IMAGE = 1001;
    private int role;
    private String chatId;
    private String conversationId;
    public String previousInfo;
    public String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChatActivitySampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // setup DB
        transRef = FirebaseDatabase.getInstance().getReference("transaction");

        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
        listenForTransactionConfirmation();

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
        userId = PreferenceManager.getString("userId");

        role = getIntent().getIntExtra("role",-1);
        previousInfo = getIntent().getStringExtra("prevInfo");
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put("chatId",chatId);
        message.put("senderId", preferenceManager.getString("userId"));
        message.put("receiverId", receiverUser.getId());
        message.put("message", binding.inputMessage.getText().toString());
        message.put("timestamp", new Date());
        database.collection("chats").add(message);

        // TODO 최근 목록 업데이트
        if (conversationId != null){
            updateConversation(binding.inputMessage.getText().toString());
        }else{
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put("chatId",chatId);
            conversation.put("senderId",preferenceManager.getString("userId"));
            conversation.put("senderName",preferenceManager.getString("username"));
            conversation.put("receiverId",receiverUser.getId());
            conversation.put("receiverName",receiverUser.getName());
            conversation.put("message",binding.inputMessage.getText().toString());
            conversation.put("timestamp",new Date());
            addOrUpdateConversation(conversation);
        }
        binding.inputMessage.setText(null);
    }

    private void listenMessages() {

        database.collection("chats")
                .whereEqualTo("chatId",chatId)
                .whereEqualTo("senderId", preferenceManager.getString("userId"))
                .addSnapshotListener(eventListener);

        database.collection("chats")
                .whereEqualTo("chatId",chatId)
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
                    chatData.setChatId(chatId);
                    chatData.setSenderId(documentChange.getDocument().getString("senderId"));
                    chatData.setReceiverId(documentChange.getDocument().getString("receiverId"));
                    chatData.setMessage(documentChange.getDocument().getString("message"));
                    chatAdapter.addChat(chatData); // 메시지 추가
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                }
            }
        }
        if(chatId == null){
            checkForConversion();
        }
    };

    private void loadReceiverDetails() {
        receiverUser = (Users) getIntent().getSerializableExtra("user");
        binding.textName.setText(receiverUser.getName());

        // RecentAdapter, DetailActivity에서부터 ChatActivity를 호출하는지에 따라 역할을 정하는 방식이 약간 다릅니다.
        // 이를 구분하기 위해 Intent를 통해 prevInfo를 전달받을 수 있도록 했습니다. - @D7MeKz
        chatId = getIntent().getStringExtra("chatId");
        role = getIntent().getIntExtra("role",-1);

    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.imageInfo.setOnClickListener(v -> setTransaction()); // 거래 정보 확장 버튼
        binding.buttonGps.setOnClickListener(v -> getMap()); // 채팅
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
                        chatData.setChatId(chatId);
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

    private void addOrUpdateConversation(HashMap<String, Object> conversation){
        // Check if conversation exists with the same chatId
        database.collection("conversation")
                .whereEqualTo("chatId", chatId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                documentSnapshot.getReference().update(conversation);
                            } else {
                                database.collection("conversation")
                                        .add(conversation)
                                        .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void getMap(){
        Intent intent = new Intent(ChatActivity.this, MapTracking.class);
        intent.putExtra("chatId",chatId);
        intent.putExtra("role",role);
        intent.putExtra("otherUser",receiverUser.getName());
        startActivity(intent);
    }

    private void updateConversation(String message){
        DocumentReference documentReference = database.collection("conversation").document(conversationId);
        documentReference.update(
                "message",message,
                "timestamp", new Date()
        );
    }


    private void checkForConversationRemotely(String senderId, String receiverId) {
        database.collection("conversation")
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task ->{
        if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    private void checkForConversion(){
        if(chatMessages.size() !=0){
            checkForConversationRemotely(
                    preferenceManager.getString("userId"),
                    receiverUser.getId()
            );
            checkForConversationRemotely(
                    receiverUser.getId(),
                    preferenceManager.getString("userId")
            );
        }
    }
    private void setTransaction() {
        DatabaseReference myRef = transRef.child(chatId);
        Map<String, Object> transInfo = new HashMap<>();

        if(role == SELLER){
            transInfo.put("chatId",chatId);
            transInfo.put("sellerId", preferenceManager.getString("userId"));
            transInfo.put("sellerName", preferenceManager.getString("username"));
            transInfo.put("sellerStatus",false);
            transInfo.put("buyerId","");
            transInfo.put("buyerName","");
            transInfo.put("buyerStatus",false);
            transInfo.put("check",false);
        }

        myRef.setValue(transInfo)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Data has been saved successfully."))
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to save data.", e));
    }

    private void listenForTransactionConfirmation() {
        transRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean checkStatus = dataSnapshot.child("check").getValue(Boolean.class);
                    if (checkStatus != null) {
                        if (!checkStatus && role == BUYER) {
                            showConfirmationDialog();

                        } else if (checkStatus) {
                            showTransactionCompleteDialog();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Purchase");
        builder.setMessage("구매를 승락하겠습니까?");
        builder.setPositiveButton("네", (dialog, which) -> {
            // Update Firebase with the buyer's information and set 'check' to true.
            transRef.child(chatId).child("buyerId").setValue(preferenceManager.getString("userId"));
            transRef.child(chatId).child("buyerName").setValue(preferenceManager.getString("username"));
            transRef.child(chatId).child("buyerStatus").setValue(true);
            transRef.child(chatId).child("check").setValue(true);
        });
        builder.setNegativeButton("아니요", (dialog, which) -> {

        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showTransactionCompleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Transaction Completed");
        builder.setMessage("구매가 승락되었습니다.");
        builder.setPositiveButton("OK", (dialog, which) -> {

        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}