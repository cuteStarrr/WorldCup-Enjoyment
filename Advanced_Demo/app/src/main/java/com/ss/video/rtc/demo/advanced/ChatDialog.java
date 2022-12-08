package com.ss.video.rtc.demo.advanced;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

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
import java.util.Objects;

public class ChatDialog extends DialogFragment {
    private RTCRoom mRTCRoom;
    private String mUserId;
    public static final String TAG_FOR_SHOW = "chattingDialog";
    private List<ChatMessage> messageList = new ArrayList<>();

    private Button button_send;
    private ViewFlipper mViewFlipper;
    private EditText mEditText_input;
    private RecyclerView mRecyclerView_message;
    private RecyclerView mRecyclerView_receiver;
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
        mEditText_input = view.findViewById(R.id.input_text);
        mRecyclerView_message = view.findViewById(R.id.recyclerView_message);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        mRecyclerView_message.setLayoutManager(linearLayoutManager);
        mMessageAdapter = new MessageAdapter(messageList);
        mRecyclerView_message.setAdapter(mMessageAdapter);
        button_send.setOnClickListener((v) -> {
            String content = mEditText_input.getText().toString().trim();
            if (!content.equals("")){
                mRTCRoom.sendRoomMessage(content);
                ChatMessage message = new ChatMessage(content, mUserId, ChatMessage.TYPE_SENT, ChatMessage.TYPE_PUBLIC);
                addMessage(message);
                mEditText_input.setText("");
            }
            //dismiss();
        });
        //设置显示位置为屏幕右边
        Window window = Objects.requireNonNull(this.getDialog()).getWindow();
        window.setGravity(Gravity.END);
        //显示时后面activity不变暗
        window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        //设置点击发送至事件
        mViewFlipper = view.findViewById(R.id.viewFlipper_chat);
        TextView textView_receiver = view.findViewById(R.id.textview_receiver);
        textView_receiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewFlipper.showNext();
            }
        });
        return view;
    }

    public void addMessage(ChatMessage message){
        requireActivity().runOnUiThread(()->{
            messageList.add(message);
            mMessageAdapter.notifyItemInserted(messageList.size()-1);
            mRecyclerView_message.scrollToPosition(messageList.size()-1);
        });
    }
}
