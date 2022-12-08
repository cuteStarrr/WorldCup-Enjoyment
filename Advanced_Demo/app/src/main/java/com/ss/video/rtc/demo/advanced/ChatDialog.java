package com.ss.video.rtc.demo.advanced;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.bytertc.engine.RTCRoom;
import com.ss.rtc.demo.advanced.R;
import com.ss.video.rtc.demo.advanced.chat.ChatMessage;
import com.ss.video.rtc.demo.advanced.chat.MessageAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatDialog extends DialogFragment {
    private RTCRoom mRTCRoom;
    private String mUserId;
    public static final String TAG_FOR_SHOW = "chattingDialog";
    private List<ChatMessage> messageList = new ArrayList<>();

    private Button button_send;
    private EditText editText_input;
    private RecyclerView mRecyclerView;
    private MessageAdapter mMessageAdapter;

    public void setConfig(RTCRoom mRTCRoom, String mUserId) {
        this.mRTCRoom = mRTCRoom;
        this.mUserId = mUserId;
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_chat,null);
        button_send = view.findViewById(R.id.button_send);
        editText_input = view.findViewById(R.id.input_text);
        mRecyclerView = view.findViewById(R.id.msg_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mMessageAdapter = new MessageAdapter(messageList);
        mRecyclerView.setAdapter(mMessageAdapter);
        if (button_send != null) {
            button_send.setOnClickListener((v) -> {
                String content = "";
                if (editText_input != null) {
                    content = editText_input.getText().toString().trim();
                    mRTCRoom.sendRoomMessage(content);
                    ChatMessage message = new ChatMessage(content, mUserId, ChatMessage.TYPE_SENT);
                    addMessage(message);
                    editText_input.setText("");
                }
                //dismiss();
            });
        }
        return view;
    }

    public void addMessage(ChatMessage message){
        messageList.add(message);
        mMessageAdapter.notifyItemInserted(messageList.size()-1);
        mRecyclerView.scrollToPosition(messageList.size()-1);
    }
}
