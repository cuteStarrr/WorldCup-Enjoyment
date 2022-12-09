package com.ss.video.rtc.demo.advanced;

import static com.ss.bytertc.engine.type.MessageConfig.MessageConfigReliableOrdered;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
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
import com.ss.video.rtc.demo.advanced.chat.Receiver;
import com.ss.video.rtc.demo.advanced.chat.ReceiverAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatDialog extends DialogFragment {
    String TAG = "ChatDialog";
    private RTCRoom mRTCRoom;
    private String mUserId;
    public static final String TAG_FOR_SHOW = "chattingDialog";
    private List<ChatMessage> messageList = new ArrayList<>();
    private List<Receiver> receiverList = new ArrayList<>();
    private Receiver mReceiver;

    private Button button_send;
    private ViewFlipper mViewFlipper;
    private EditText mEditText_input;
    private RecyclerView mRecyclerView_message;
    private RecyclerView mRecyclerView_receiver;
    private TextView mTextview_receiver;
    private final MessageAdapter mMessageAdapter = new MessageAdapter(messageList);
    private final ReceiverAdapter mReceiverAdapter = new ReceiverAdapter(receiverList, this);

    public void setConfig(RTCRoom mRTCRoom, String mUserId) {
        this.mRTCRoom = mRTCRoom;
        this.mUserId = mUserId;
        if (receiverList.isEmpty() || receiverList.get(0).getBroadcastType() != Receiver.TYPE_ALL){
            //收信息人列表加上所有人，并设置默认收信人为所有人
            mReceiver = new Receiver("所有人", Receiver.TYPE_ALL);
            receiverList.add(0, mReceiver);
            mReceiverAdapter.setDefaultReceiver();
            mReceiverAdapter.notifyItemInserted(receiverList.size()-1);
        }
    }


    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_chat,null);
        button_send = view.findViewById(R.id.button_send);
        mEditText_input = view.findViewById(R.id.input_text);
        mTextview_receiver = view.findViewById(R.id.textview_receiver);

        //设置两个recycleView相关
        mRecyclerView_message = view.findViewById(R.id.recyclerView_message);
        mRecyclerView_receiver = view.findViewById(R.id.recyclerView_receivers);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        mRecyclerView_message.setLayoutManager(linearLayoutManager);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this.getContext());
        mRecyclerView_receiver.setLayoutManager(linearLayoutManager1);
        mRecyclerView_message.setAdapter(mMessageAdapter);
        mRecyclerView_receiver.setAdapter(mReceiverAdapter);

        //设置发送按钮
        button_send.setOnClickListener((v) -> {
            String content = mEditText_input.getText().toString().trim();
            if (!content.equals("")){
                ChatMessage message;
                if (mReceiver.getBroadcastType() == Receiver.TYPE_ALL){
                    mRTCRoom.sendRoomMessage(content);
                    message = new ChatMessage(content, mUserId, ChatMessage.TYPE_SENT, ChatMessage.TYPE_PUBLIC);
                }
                else{
                    mRTCRoom.sendUserMessage(mReceiver.getUser(), content, MessageConfigReliableOrdered);
                    message = new ChatMessage(content, mReceiver.getUser(), ChatMessage.TYPE_SENT, ChatMessage.TYPE_PRIVATE);
                }
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
        mTextview_receiver.setOnClickListener(new View.OnClickListener() {
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

    public void addReceiver(String userId){
        Log.d(TAG, "addReceiver: ");
        Receiver receiver = new Receiver(userId, Receiver.TYPE_ONE);
        receiverList.add(receiver);
        mReceiverAdapter.notifyItemInserted(receiverList.size()-1);
    }

    public void removeReceiver(String userId){
        Log.d(TAG, "removeReceiver: ");
        int i=0;
        for (i=0; i<receiverList.size(); ++i){
            if (receiverList.get(i).getUser().equals(userId))
                break;
        }
        receiverList.remove(i);
        mReceiverAdapter.notifyItemRemoved(i);
    }

    public void onReceiverChanged(Receiver receiver, int position){
        this.mReceiver = receiver;
        mViewFlipper.showPrevious();
        mTextview_receiver.setText(receiver.getUser());
        requireActivity().runOnUiThread(()->mReceiverAdapter.notifyItemChanged(position));
    }
}
