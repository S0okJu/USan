package com.example.usan_comb1.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.usan_comb1.activity.chat.ChatActivity;
import com.example.usan_comb1.adapter.RecentConversationsAdapter;
import com.example.usan_comb1.databinding.ActivityChatUsersBinding;
import com.example.usan_comb1.listeners.ConversationListener;
import com.example.usan_comb1.models.ChatData;
import com.example.usan_comb1.models.Users;
import com.example.usan_comb1.utilities.FindFromFirebase;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatFragment extends Fragment implements ConversationListener {
    private ActivityChatUsersBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatData> conversations;
    private FirebaseFirestore database;
    private RecentConversationsAdapter recentConversationsAdapter;
    private String chatId;
    public Integer role;
    public String accessToken;
    public String title;
    private FindFromFirebase findFromFirebase = new FindFromFirebase();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityChatUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(requireContext());

        init();
        setListeners();
        listenConversations();
    }

    private void init() {
        conversations = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        recentConversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecyclerView.setAdapter(recentConversationsAdapter);
        accessToken = preferenceManager.getString("access_token");
    }



    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void listenConversations(){
        database.collection("conversation")
                .whereEqualTo("senderId", preferenceManager.getString("userId"))
                .addSnapshotListener(eventListener);
        database.collection("conversation")
                .whereEqualTo("receiverId", preferenceManager.getString("userId"))
                .addSnapshotListener(eventListener);
    }


    private final EventListener<QuerySnapshot> eventListener = (value, error )->{
        if(error != null){
            return;
        }
        if(value !=null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString("senderId");
                    String receiverId = documentChange.getDocument().getString("receiverId");
                    title = documentChange.getDocument().getString("title");
                    chatId = documentChange.getDocument().getString("chatId");
                    role = findFromFirebase.checkSeller(accessToken, chatId);

                    ChatData chatMessage = new ChatData();
                    chatMessage.setSenderId(senderId);
                    chatMessage.setReceiverId(receiverId);


                    if(preferenceManager.getString("userId").equals(senderId)){
//                        chatMessage.conversionImage = documentChange.getDocument().getString("receiverImage");
                        chatMessage.setConversationId(documentChange.getDocument().getString("receiverId"));
                        chatMessage.setConversationName(documentChange.getDocument().getString("receiverName"));
                    }else{
                        //                        chatMessage.conversionImage = documentChange.getDocument().getString("receiverImage");
                        chatMessage.setConversationId(documentChange.getDocument().getString("senderId"));
                        chatMessage.setConversationName(documentChange.getDocument().getString("senderName"));
                    }
                    chatMessage.setMessage(documentChange.getDocument().getString("message"));
                    chatMessage.setTimestamp(documentChange.getDocument().getDate("timestamp"));
                    conversations.add(chatMessage);

                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for(int i = 0 ; i < conversations.size(); i++){
                        String senderId = documentChange.getDocument().getString("senderId");
                        String receiverId = documentChange.getDocument().getString("receiverId");
                        if(conversations.get(i).getSenderId().equals(senderId) && conversations.get(i).getReceiverId().equals(receiverId)){
                            conversations.get(i).setMessage(documentChange.getDocument().getString("message"));
                            conversations.get(i).setTimestamp(documentChange.getDocument().getDate("timestamp"));
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations,(obj1, obj2)->obj2.getTimestamp().compareTo(obj1.getTimestamp()));
            recentConversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    @Override
    public void onUserClicked(Users user) {

    }

    @Override
    public void onConversationClicked(Users users) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("prevInfo","recent"); // 이전 Activity 정보를 알아보기 위해 추가
        intent.putExtra("title",title);
        intent.putExtra("chatId",chatId);
        intent.putExtra("role",role);
        intent.putExtra("user",users);
        startActivity(intent);
    }
}