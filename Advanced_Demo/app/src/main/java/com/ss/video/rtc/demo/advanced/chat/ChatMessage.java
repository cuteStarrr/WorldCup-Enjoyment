package com.ss.video.rtc.demo.advanced.chat;

public class ChatMessage {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;
    public static final int TYPE_PRIVATE = 0;
    public static final int TYPE_PUBLIC = 1;
    public static final int TYPE_SEI = 2;

    private final String content;
    private int broadcastType;
    private String user;
    private int sourceType;

    public String getContent() {
        return content;
    }

    public int getSourceType() {
        return sourceType;
    }

    public String getUser(){
        return user;
    }

    public int getBroadcastType() {
        return broadcastType;
    }

    public ChatMessage(String content, String user, int sourceType, int broadcastType) {
        this.content = content;
        this.sourceType = sourceType;
        this.user = user;
        this.broadcastType = broadcastType;
    }
}
