package com.ss.video.rtc.demo.advanced.chat;

import java.util.Objects;

public class Receiver {
    public static final int TYPE_ALL = 0;
    public static final int TYPE_ONE = 1;

    private final String user;
    private final int broadcastType; //私聊或给所有人

    public String getUser(){
        return user;
    }

    public int getBroadcastType() {
        return broadcastType;
    }

    public Receiver(String user, int broadcastType) {
        this.user = user;
        this.broadcastType = broadcastType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receiver receiver = (Receiver) o;
        return broadcastType == receiver.broadcastType && Objects.equals(user, receiver.user);
    }
}
