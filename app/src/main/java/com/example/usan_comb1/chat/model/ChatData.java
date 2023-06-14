package com.example.usan_comb1.chat.model;

import java.io.Serializable;
import java.util.Date;

//public class ChatData implements Serializable {
//    private String msg;
//    private String username;
//    private String senderId;
//    private String receiverId;
//    public ChatData() {
//        // Default constructor required for calls to DataSnapshot.getValue(ChatData.class)
//    }
//
//    public ChatData(String senderId, String receiverId, String msg) {
//        this.senderId = senderId;
//        this.receiverId = receiverId;
//        this.msg = msg;
//    }
//
//    public String getMsg() {
//        return msg;
//    }
//
//    public void setMsg(String msg) {
//        this.msg = msg;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getSenderId() {
//        return senderId;
//    }
//
//    public void setSenderId(String senderId) {
//        this.senderId = senderId;
//    }
//
//    public String getReceiverId() {
//        return receiverId;
//    }
//
//    public void setReceiverId(String receiverId) {
//        this.receiverId = receiverId;
//    }
//
//}
public class ChatData implements Serializable {
    private String senderId, receiverId, message, imageUrl;
    private Date timestamp;
    public String conversationId, conversationName, conversionImage;
    public String chatId;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() { return imageUrl;}

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl;}

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public String getConversionImage() {
        return conversionImage;
    }

    public void setConversionImage(String conversionImage) {
        this.conversionImage = conversionImage;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}