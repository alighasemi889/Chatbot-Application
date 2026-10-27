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
    private final String conversationId;

    public ChatAdapter(List<Message> items, String conversationId) {
        this.items = items;
        this.conversationId = conversationId;
    }

    static class VH extends RecyclerView.ViewHolder {
        final LinearLayout root;
        final TextView text;
        final TextView save;

        VH(View v) {
            super(v);
            root = v.findViewById(R.id.root);
            text = v.findViewById(R.id.text);
            save = v.findViewById(R.id.save_btn);
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
        final Message m = items.get(position);
        final Context ctx = h.itemView.getContext();
        h.text.setText(m.text);
        h.text.setOnLongClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("message", m.text));
                Toast.makeText(ctx, R.string.copied, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        if (m.fromUser) {
            h.root.setGravity(Gravity.END);
            h.text.setBackgroundResource(R.drawable.bubble_user);
            h.text.setTextColor(0xFFFFFFFF);
            h.save.setVisibility(View.GONE);
        } else {
            h.root.setGravity(Gravity.START);
            h.text.setBackgroundResource(R.drawable.bubble_bot);
            h.text.setTextColor(ctx.getColor(R.color.text_dark));

            boolean placeholder = m.text.equals(ctx.getString(R.string.typing))
                    || m.text.equals(ctx.getString(R.string.welcome));
            if (placeholder) {
                h.save.setVisibility(View.GONE);
            } else {
                h.save.setVisibility(View.VISIBLE);
                h.save.setText(ChatStore.isSaved(ctx, m.text)
                        ? R.string.saved_mark : R.string.save_answer);
                h.save.setOnClickListener(v -> {
                    if (ChatStore.isSaved(ctx, m.text)) {
                        ChatStore.removeSaved(ctx, m.text);
                    } else {
                        ChatStore.saveAnswer(ctx, conversationId, m.text);
                    }
                    notifyItemChanged(h.getBindingAdapterPosition());
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
