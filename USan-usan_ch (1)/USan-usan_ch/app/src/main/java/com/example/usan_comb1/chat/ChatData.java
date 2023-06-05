package com.example.usan_comb1.chat;

import java.io.Serializable;

import com.google.firebase.Timestamp;

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
    private String senderId, receiverId, message, dateTime, imageUrl;

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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }


    public String getImageUrl() { return imageUrl;}

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl;}
}