package com.example.aichat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
    private final List<Message> items;

    public ChatAdapter(List<Message> items) {
        this.items = items;
    }

    static class VH extends RecyclerView.ViewHolder {
        final LinearLayout root;
        final TextView text;

        VH(View v) {
            super(v);
            root = v.findViewById(R.id.root);
            text = v.findViewById(R.id.text);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Message m = items.get(position);
        h.text.setText(m.text);
        h.text.setOnLongClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("message", m.text));
                Toast.makeText(v.getContext(), R.string.copied, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        if (m.fromUser) {
            h.root.setGravity(Gravity.END);
            h.text.setBackgroundResource(R.drawable.bubble_user);
            h.text.setTextColor(0xFFFFFFFF);
        } else {
            h.root.setGravity(Gravity.START);
            h.text.setBackgroundResource(R.drawable.bubble_bot);
            h.text.setTextColor(h.itemView.getContext().getColor(R.color.text_dark));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
