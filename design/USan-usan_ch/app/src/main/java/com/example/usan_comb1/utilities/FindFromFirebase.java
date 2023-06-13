package com.example.usan_comb1.utilities;

import static com.example.usan_comb1.utilities.Constants.BUYER;
import static com.example.usan_comb1.utilities.Constants.SELLER;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.response.CheckRoleResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FindFromFirebase {
    public String chatId;
    public Integer role;
    private static ProductService mProductService;

    public FirebaseFirestore db = FirebaseFirestore.getInstance();



    public String getChatIdFromConversation(String senderId, String receiverId){
        final String[] returnChatId = {null};
        db.collection("conversation")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String chatId = null;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();

                                if (data.get("senderId").equals(senderId) &&
                                        data.get("receiverId").equals(receiverId)) {
                                    chatId = (String) data.get("chatId");

                                    break;
                                }
                            }
                        } else {
                            Log.w("Firebase", "Error getting documents.", task.getException());
                        }
                        returnChatId[0] = chatId;
                    }
                });
        return returnChatId[0];
    }


    public String getChatIdFromProductDetail(String senderId, String receiverId) {
        // Setup
        chatId = null;
        DatabaseReference transRef = FirebaseDatabase.getInstance().getReference("transaction");
        Query roleQuery = transRef.orderByChild("buyerId").equalTo(senderId);

        roleQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String receiverId = (String) childSnapshot.child("sellerId").getValue();
                        if (receiverId.equals(receiverId)) {
                            String key = childSnapshot.getKey();
                            chatId = key;
                            break;
                        }
                    }
                } else {
                    Query sellerQuery = transRef.orderByChild("sellerId").equalTo(senderId);
                    sellerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                String receiver = (String) childSnapshot.child("buyerId").getValue();
                                if (receiver.equals(receiverId)) {
                                    String key = childSnapshot.getKey();
                                    chatId = key;
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle possible errors.
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
        return chatId;
    }

    public Integer checkSeller(String accessToken, String chatId) {
        mProductService = RetrofitClient.getProductService();
        role = null;

        Call<CheckRoleResponse> call = mProductService.getRole(accessToken, chatId);
        call.enqueue(new Callback<CheckRoleResponse>() {
            @Override
            public void onResponse(Call<CheckRoleResponse> call, Response<CheckRoleResponse> response) {
                CheckRoleResponse result = response.body();
                if(result.getRole()==SELLER){
                    role = SELLER;
                }else{
                    role = BUYER;
                }
            }

            @Override
            public void onFailure(Call<CheckRoleResponse> call, Throwable t) {

            }
        });

        return role;
    }
}
