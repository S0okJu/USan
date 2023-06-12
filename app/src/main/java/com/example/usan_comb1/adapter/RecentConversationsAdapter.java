package com.example.usan_comb1.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usan_comb1.databinding.ItemContainerRecentConversationBinding;
import com.example.usan_comb1.listeners.ConversationListener;
import com.example.usan_comb1.models.ChatData;
import com.example.usan_comb1.models.Users;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder>{

    private final List<ChatData> chatMessages;
    private final ConversationListener conversationListener;

    public RecentConversationsAdapter(List<ChatData> chatMessages, ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerRecentConversationBinding itemContainerRecentConversationBinding = ItemContainerRecentConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ConversationViewHolder(itemContainerRecentConversationBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversationBinding binding;
        ConversationViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConversationBinding){
            super(itemContainerRecentConversationBinding.getRoot());
            binding = itemContainerRecentConversationBinding;
        }

        void setData(ChatData chatMessage){
            // binding.imageProfile.setImageBitmap(getConversationImage(chatMessage.getConversionImage()));
            binding.textName.setText(chatMessage.getConversationName());
            binding.textRecentMessage.setText(chatMessage.getMessage());
            binding.getRoot().setOnClickListener(v->{
                Users users = new Users();
                users.setId(chatMessage.getConversationId());
                users.setName(chatMessage.getConversationName());
                conversationListener.onConversationClicked(users);
            });
        }
    }

    private Bitmap getConversationImage(String encodeImage){
        byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }
}