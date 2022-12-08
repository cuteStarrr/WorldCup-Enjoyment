package com.ss.video.rtc.demo.advanced.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.rtc.demo.advanced.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<ChatMessage> mMsgList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        TextView leftUser;
        TextView rightUser;

        public ViewHolder(View view) {
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.layout_message_left);
            rightLayout = (LinearLayout) view.findViewById(R.id.layout_message_right);
            leftMsg = (TextView) view.findViewById(R.id.textview_left_msg);
            rightMsg = (TextView) view.findViewById(R.id.textview_right_msg);
            leftUser = view.findViewById(R.id.textview_left_msg_user);
            rightUser = view.findViewById(R.id.textview_right_msg_user);
        }
    }

    public MessageAdapter(List<ChatMessage>  msgList) {
        mMsgList = msgList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_item, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatMessage msg = mMsgList.get(position);
        if (msg.getSourceType() == ChatMessage.TYPE_RECEIVED) {
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
            holder.leftUser.setText(msg.getUser());
        } else if (msg.getSourceType() == ChatMessage.TYPE_SENT) {
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.getContent());
            holder.rightUser.setText(msg.getUser());
        }
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }
}
