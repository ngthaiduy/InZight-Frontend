package com.example.inzightapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inzightapp.R;
import com.example.inzightapp.model.response.ChatMessageResponse;
import com.example.inzightapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final Context context;
    private List<ChatMessageResponse> messageList = new ArrayList<>();
    private OnMessageClickListener listener;

    public interface OnMessageClickListener {
        void onMessageClick(ChatMessageResponse message);
    }

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void setMessageList(List<ChatMessageResponse> messages) {
        this.messageList = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ChatMessageResponse message = messageList.get(position);

        // Set contact name (sender or receiver name)
        String contactName = message.getSenderName() != null ? message.getSenderName() : "Unknown";
        holder.tvContactName.setText(contactName);

        // Set last message preview
        String lastMessage = message.getContent() != null ? message.getContent() : "";
        holder.tvLastMessage.setText(lastMessage);

        // Set timestamp
        holder.tvTimestamp.setText(formatTime(message.getCreatedAt()));

        // Load avatar - special handling for Finbot
        if (message.getSenderId() != null && message.getSenderId().equals(-999L)) {
            // Finbot - use finbot icon
            holder.imgAvatar.setImageResource(R.drawable.imagebot);
        } else {
            // Regular user - load from URL
            String avatarUrl = "https://i.pravatar.cc/150?u=" + (message.getSenderId() != null ? message.getSenderId() : "");
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(holder.imgAvatar);
        }

        // Show online indicator (you can customize this logic)
        holder.onlineIndicator.setVisibility(View.VISIBLE);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMessageClick(message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        View onlineIndicator;
        TextView tvContactName;
        TextView tvLastMessage;
        TextView tvTimestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }

    private String formatTime(String createdAt) {
        try {
            if (createdAt == null) return "";
            Instant instant = Instant.parse(createdAt);
            LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return TimeUtils.formatTime(time);
        } catch (Exception e) {
            return "";
        }
    }
}

