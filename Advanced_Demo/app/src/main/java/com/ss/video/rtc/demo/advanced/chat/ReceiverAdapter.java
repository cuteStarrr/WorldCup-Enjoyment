package com.ss.video.rtc.demo.advanced.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.rtc.demo.advanced.R;
import com.ss.video.rtc.demo.advanced.ChatDialog;

import java.util.List;

public class ReceiverAdapter extends RecyclerView.Adapter<ReceiverAdapter.ViewHolder> {
    private List<Receiver> mReceiverList;
    private Receiver selected_receiver;
    private int selected_receiver_index = 0;
    private final ChatDialog mChatDialog;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textView_name;
        Receiver receiver;
        boolean selected;
        private final View mView;
        private ReceiverAdapter mReceiverAdapter;

        public ViewHolder(View view) {
            super(view);
            textView_name = view.findViewById(R.id.textView_receiverName);
            this.mView = view;
            view.setOnClickListener(this);
        }

        public void bind(Receiver receiver, boolean selected, ReceiverAdapter receiverAdapter){
            this.receiver = receiver;
            textView_name.setText(receiver.getUser());
            this.selected = selected;
            if (selected)
                setSelected();
            this.mReceiverAdapter = receiverAdapter;
        }

        public void setSelected(){
            textView_name.setTextColor(mView.getResources().getColor(R.color.blue)); //设置选中文字颜色为蓝色
            //设置右侧选中图标
            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = mView.getResources().getDrawable(R.drawable.ic_baseline_check_24);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            textView_name.setCompoundDrawables(null, null, drawable, null);
        }

//        public void setUnSelected(){
//            textView_name.setTextColor(mView.getResources().getColor(R.color.black));
//            textView_name.setCompoundDrawables(null, null, null, null);
//        }

        @Override
        public void onClick(View v) {
            setSelected();
            int index = this.getAdapterPosition();
            mReceiverAdapter.changeSelected(index, this.receiver);
        }
    }

    public ReceiverAdapter(List<Receiver> mReceiverList, ChatDialog mChatDialog) {
        this.mReceiverList = mReceiverList;
        this.mChatDialog = mChatDialog;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_receiver_item, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Receiver receiver = mReceiverList.get(position);
        holder.bind(receiver, receiver.equals(this.selected_receiver), this);
    }

    @Override
    public int getItemCount() {
        return mReceiverList.size();
    }

    public void setDefaultReceiver(){
        if (mReceiverList.size() == 0)
            return;
        this.selected_receiver = mReceiverList.get(0);
        this.selected_receiver_index = 0;
    }

    public void changeSelected(int position, Receiver receiver){
        mChatDialog.onReceiverChanged(receiver, position);
        this.selected_receiver = receiver;
        this.selected_receiver_index = position;
    }

}
