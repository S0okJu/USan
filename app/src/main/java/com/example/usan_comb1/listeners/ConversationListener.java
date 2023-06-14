package com.example.usan_comb1.listeners;

import com.example.usan_comb1.chat.model.Users;

public interface ConversationListener {
    void onUserClicked(Users user);

    void onConversationClicked(Users users);
}
