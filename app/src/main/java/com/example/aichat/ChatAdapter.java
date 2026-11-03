package com.example.aichat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
        final ImageButton copy;

        VH(View v) {
            super(v);
            root = v.findViewById(R.id.root);
            text = v.findViewById(R.id.text);
            copy = v.findViewById(R.id.copy);
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
        Context ctx = h.itemView.getContext();
        Message m = items.get(position);
        h.text.setText(MessageFormatter.format(m.text, ctx.getColor(R.color.code_bg)));

        // Bot bubbles are plain text; user bubbles get the tinted pill.
        h.text.setBackgroundResource(m.fromUser ? R.drawable.bubble_user : R.drawable.bubble_bot);
        h.root.setGravity(m.fromUser ? Gravity.END : Gravity.START);

        boolean typing = !m.fromUser && ctx.getString(R.string.typing).equals(m.text);
        boolean showCopy = !m.fromUser && !typing;
        h.copy.setVisibility(showCopy ? View.VISIBLE : View.GONE);

        View.OnClickListener copyAction = v -> {
            ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("message", MessageFormatter.plain(m.text)));
                Toast.makeText(ctx, R.string.copied, Toast.LENGTH_SHORT).show();
            }
        };
        h.copy.setOnClickListener(copyAction);
        h.text.setOnLongClickListener(v -> {
            copyAction.onClick(v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
