package com.example.aichat;

public class Message {
    public String text;
    public final boolean fromUser;

    public Message(String text, boolean fromUser) {
        this.text = text;
        this.fromUser = fromUser;
    }
}
