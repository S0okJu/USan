package com.example.usan_comb1.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.usan_comb1.R;
import com.example.usan_comb1.activity.UserUpdateActivity;
import com.example.usan_comb1.chat.ChatActivity;
import com.example.usan_comb1.chat.RecentConversationsAdapter;
import com.example.usan_comb1.chat.UsersActivity;
import com.example.usan_comb1.chat.model.ChatData;
import com.example.usan_comb1.chat.model.Users;
import com.example.usan_comb1.databinding.ActivityChatUsersBinding;
import com.example.usan_comb1.listeners.ConversationListener;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatFragment extends Fragment implements ConversationListener {
    private ActivityChatUsersBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatData> conversations;
    private FirebaseFirestore database;
    private RecentConversationsAdapter recentConversationsAdapter;

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
        getUsers();
        listenConversations();
    }

    private void init() {
        conversations = new ArrayList<>();
        database = FirebaseFirestore.getInstance();
        recentConversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecyclerView.setAdapter(recentConversationsAdapter);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").get().addOnCompleteListener(task -> {
            loading(false);
            String curUserId = preferenceManager.getString("userId");

            if (task.isSuccessful() && task.getResult() != null) {
                List<Users> users = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    if (Objects.equals(curUserId, queryDocumentSnapshot.getId())) {
                        continue;
                    }
                    Users user = new Users();
                    user.name = queryDocumentSnapshot.getString("username");
                    user.id = queryDocumentSnapshot.getId();

                    users.add(user);
                }
                if (users.size() > 0) {
                    recentConversationsAdapter = new RecentConversationsAdapter(conversations, this);
                    binding.conversationsRecyclerView.setAdapter(recentConversationsAdapter);
                    binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    System.out.println("0..");
                }
            }
        });
    }

    private void listenConversations() {
        String userId = preferenceManager.getString("userId");
        EventListener<QuerySnapshot> eventListener = (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        String senderId = documentChange.getDocument().getString("senderId");
                        String receiverId = documentChange.getDocument().getString("receiverId");
                        ChatData chatMessage = new ChatData();
                        chatMessage.setSenderId(senderId);
                        chatMessage.setReceiverId(receiverId);

                        if (userId.equals(senderId)) {
                            chatMessage.setConversationId(documentChange.getDocument().getString("receiverId"));
                            chatMessage.setConversationName(documentChange.getDocument().getString("receiverName"));
                        } else {
                            chatMessage.setConversationId(documentChange.getDocument().getString("senderId"));
                            chatMessage.setConversationName(documentChange.getDocument().getString("senderName"));
                        }
                        chatMessage.setMessage(documentChange.getDocument().getString("message"));
                        chatMessage.setTimestamp(documentChange.getDocument().getDate("timestamp"));
                        conversations.add(chatMessage);
                    } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                        for (int i = 0; i < conversations.size(); i++) {
                            String senderId = documentChange.getDocument().getString("senderId");
                            String receiverId = documentChange.getDocument().getString("receiverId");
                            if (conversations.get(i).getSenderId().equals(senderId) && conversations.get(i).getReceiverId().equals(receiverId)) {
                                conversations.get(i).setMessage(documentChange.getDocument().getString("message"));
                                conversations.get(i).setTimestamp(documentChange.getDocument().getDate("timestamp"));
                                break;
                            }
                        }
                    }
                }
                Collections.sort(conversations, (obj1, obj2) -> obj2.getTimestamp().compareTo(obj1.getTimestamp()));
                recentConversationsAdapter.notifyDataSetChanged();
                binding.conversationsRecyclerView.smoothScrollToPosition(0);
                binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }
        };

        database.collection("conversation")
                .whereEqualTo("senderId", userId)
                .addSnapshotListener(eventListener);
        database.collection("conversation")
                .whereEqualTo("receiverId", userId)
                .addSnapshotListener(eventListener);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(Users user) {

    }

    @Override
    public void onConversationClicked(Users users) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("user", users);
        startActivity(intent);
    }
}