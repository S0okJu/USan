package com.example.usan_comb1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usan_comb1.databinding.ItemContainerRevceivedMessageBinding;
import com.example.usan_comb1.databinding.ItemContainerSentMessageBinding;
import com.example.usan_comb1.models.ChatData;
import com.squareup.picasso.Picasso;

import java.util.List;



public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatData> chatMessages;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatData> chatMessages, String senderId) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            ItemContainerSentMessageBinding binding = ItemContainerSentMessageBinding.inflate(layoutInflater, parent, false);
            return new SentMessageViewHolder(binding);
        } else {
            ItemContainerRevceivedMessageBinding binding = ItemContainerRevceivedMessageBinding.inflate(layoutInflater, parent, false);
            return new ReceivedMessageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatData chat = chatMessages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.setData(chat);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.setData(chat);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).getSenderId().equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    public void addChat(ChatData chatData) {
        chatMessages.add(chatData);
        notifyItemInserted(chatMessages.size() - 1);
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;
        private final ImageView imageView;

        SentMessageViewHolder(ItemContainerSentMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.imageView = binding.imageMessage;
        }

        void setData(ChatData chatData) {
            binding.textMessage.setText(chatData.getMessage());
            if (chatData.getMessage().startsWith("https://firebasestorage.googleapis.com/")) {
                // 이미지 URL인 경우 이미지를 로드하여 표시
                Picasso.get().load(chatData.getMessage()).into(imageView);
                imageView.setVisibility(View.VISIBLE);
                binding.textMessage.setVisibility(View.GONE);
            } else {
                // 텍스트 메시지인 경우 텍스트를 표시
                imageView.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerRevceivedMessageBinding binding;
        private final ImageView imageView;

        ReceivedMessageViewHolder(ItemContainerRevceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.imageView = binding.imageMessage;
        }

        void setData(ChatData chatData) {
            binding.textMessage.setText(chatData.getMessage());
            if (chatData.getMessage().startsWith("https://firebasestorage.googleapis.com/")) {
                // 이미지 URL인 경우 이미지를 로드하여 표시
                Picasso.get().load(chatData.getMessage()).into(imageView);
                imageView.setVisibility(View.VISIBLE);
                binding.textMessage.setVisibility(View.GONE);
            } else {
                // 텍스트 메시지인 경우 텍스트를 표시
                imageView.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.VISIBLE);
            }
        }
    }


}