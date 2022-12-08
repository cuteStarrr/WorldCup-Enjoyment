package com.ss.video.rtc.demo.advanced.chat;

public class ChatMessage {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;


    private final String content;
    private String user;
    private int type;

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public String getUser(){
        return user;
    }

    public ChatMessage(String content, String user, int type) {
        this.content = content;
        this.type = type;
        this.user = user;
    }
}
