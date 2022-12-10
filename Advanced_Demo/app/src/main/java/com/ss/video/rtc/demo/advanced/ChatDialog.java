package com.ss.video.rtc.demo.advanced;

import static com.ss.bytertc.engine.type.MessageConfig.MessageConfigReliableOrdered;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
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

    private Handler handler = new Handler();
    private Runnable runnable_fade;
    private int fade_level = 0; //0-完全没有fade， 1-fade了一半
    private AlphaAnimation mAlphaAnimation_1;
    private AlphaAnimation mAlphaAnimation_2;
    private static final int FADE_TIME = 15000; //最后一次收到消息到fade之间的时间

    public void setConfig(RTCRoom mRTCRoom, String mUserId) {
        this.mRTCRoom = mRTCRoom;
        this.mUserId = mUserId;
        if (receiverList.isEmpty() || receiverList.get(0).getBroadcastType() != Receiver.TYPE_ALL) {
            //收信息人列表加上所有人，并设置默认收信人为所有人
            mReceiver = new Receiver("所有人", Receiver.TYPE_ALL);
            receiverList.add(0, mReceiver);
            mReceiverAdapter.setDefaultReceiver();
            mReceiverAdapter.notifyItemInserted(receiverList.size() - 1);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        //设置透明度变化动画
        mAlphaAnimation_1 = new AlphaAnimation(1.0f, 0.9f);
        mAlphaAnimation_1.setDuration(3000);
        mAlphaAnimation_1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                requireView().setAlpha(0.9f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mAlphaAnimation_2 = new AlphaAnimation(0.9f, 0f);
        mAlphaAnimation_2.setDuration(3000);
        mAlphaAnimation_2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        //设置倒计时
        fade_level = 0;
        runnable_fade = () -> {
            if (fade_level==0){
                requireView().startAnimation(mAlphaAnimation_1);
                fade_level = 1;
                handler.postDelayed(runnable_fade, FADE_TIME);
            }
            else{
                requireView().startAnimation(mAlphaAnimation_2);
            }
        };
        handler.postDelayed(runnable_fade, FADE_TIME);
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_chat, null);
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
        //添加分割线
        mRecyclerView_receiver.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        //设置发送按钮
        button_send.setOnClickListener((v) -> {
            String content = mEditText_input.getText().toString().trim();
            if (!content.equals("")) {
                ChatMessage message;
                if (mReceiver.getBroadcastType() == Receiver.TYPE_ALL) {
                    mRTCRoom.sendRoomMessage(content);
                    message = new ChatMessage(content, mUserId, ChatMessage.TYPE_SENT, ChatMessage.TYPE_PUBLIC);
                } else {
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
        //设置背景透明（为了方便fade)
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //设置点击发送至事件
        mViewFlipper = view.findViewById(R.id.viewFlipper_chat);
        if (mReceiver!=null)
            mTextview_receiver.setText(mReceiver.getUser());
        mTextview_receiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewFlipper.showNext();
                resetFade();
                handler.removeCallbacks(runnable_fade);
            }
        });

        //渐隐时点击恢复
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                resetFade();
            }
        });

        //收信人返回
        ImageView btn_back = view.findViewById(R.id.image_back_receiver);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewFlipper.showPrevious();
                handler.postDelayed(runnable_fade, FADE_TIME);
            }
        });

        return view;
    }

    private void resetFade(){
        handler.removeCallbacks(runnable_fade);
        handler.postDelayed(runnable_fade, FADE_TIME);
        if (fade_level==1){
            requireView().setAlpha(1f);
            fade_level = 0;
        }
    }

    public void addMessage(ChatMessage message) {
        messageList.add(message);
        requireActivity().runOnUiThread(() -> {
            mMessageAdapter.notifyItemInserted(messageList.size() - 1);
            mRecyclerView_message.scrollToPosition(messageList.size() - 1);
            resetFade();
        });
    }

    public void addReceiver(String userId) {
        Log.d(TAG, "addReceiver: ");
        Receiver receiver = new Receiver(userId, Receiver.TYPE_ONE);
        receiverList.add(receiver);
        mReceiverAdapter.notifyItemInserted(receiverList.size() - 1);
    }

    public void removeReceiver(String userId) {
        Log.d(TAG, "removeReceiver: ");
        int i = 0;
        for (i = 0; i < receiverList.size(); ++i) {
            if (receiverList.get(i).getUser().equals(userId))
                break;
        }
        receiverList.remove(i);
        mReceiverAdapter.notifyItemRemoved(i);
    }

    public void onReceiverChanged(Receiver receiver, int position) {
        //Log.d(TAG, "onReceiverChanged: "+position);
        requireActivity().runOnUiThread(() -> mReceiverAdapter.notifyItemChanged(position));
        this.mReceiver = receiver;
        mViewFlipper.showPrevious();
        mTextview_receiver.setText(receiver.getUser());
        handler.postDelayed(runnable_fade, FADE_TIME);
    }
}
