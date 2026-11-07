package com.example.aichat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
        final LinearLayout column;
        final TextView text;
        final ImageButton copy;
        final ImageView botAvatar;
        final ImageView userAvatar;

        VH(View v) {
            super(v);
            root = v.findViewById(R.id.root);
            column = v.findViewById(R.id.column);
            text = v.findViewById(R.id.text);
            copy = v.findViewById(R.id.copy);
            botAvatar = v.findViewById(R.id.bot_avatar);
            userAvatar = v.findViewById(R.id.user_avatar);
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

        // User messages sit on the right with the user avatar; bot messages on the left with the sparkle avatar.
        h.text.setBackgroundResource(m.fromUser ? R.drawable.bubble_user : R.drawable.bubble_bot);
        h.column.setGravity(m.fromUser ? Gravity.END : Gravity.START);
        h.botAvatar.setVisibility(m.fromUser ? View.GONE : View.VISIBLE);
        h.userAvatar.setVisibility(m.fromUser ? View.VISIBLE : View.GONE);

        boolean typing = !m.fromUser && ctx.getString(R.string.typing).equals(m.text);
        h.copy.setVisibility(!m.fromUser && !typing ? View.VISIBLE : View.GONE);

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
