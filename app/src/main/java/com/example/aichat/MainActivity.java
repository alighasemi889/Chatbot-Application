package com.example.aichat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";

    private static final String[] SUGGESTIONS = {
            "برنامه‌نویسی را برایم توضیح بده",
            "یک متن را خلاصه کن",
            "در نوشتن متن کمکم کن",
            "یک مشکل را مرحله‌به‌مرحله حل کن"
    };

    private final List<Message> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView recycler;
    private EditText input;
    private LinearLayout chips;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);

        String currentUser = UserManager.getCurrentUser(this);
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_name) + " (" + currentUser + ")");

        recycler = findViewById(R.id.recycler);
        input = findViewById(R.id.input);
        chips = findViewById(R.id.chips);
        Button sendBtn = findViewById(R.id.send);

        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        if (conversationId == null) {
            conversationId = UUID.randomUUID().toString();
            getIntent().putExtra(EXTRA_CONVERSATION_ID, conversationId);
        }

        adapter = new ChatAdapter(messages, conversationId);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recycler.setLayoutManager(lm);
        recycler.setAdapter(adapter);

        List<Message> saved = ChatStore.loadMessages(this, conversationId);
        if (!saved.isEmpty()) {
            messages.addAll(saved);
        } else {
            messages.add(new Message(getString(R.string.welcome), false));
        }
        adapter.notifyDataSetChanged();

        sendBtn.setOnClickListener(v -> send());
        buildSuggestionChips();
    }

    /** Suggestion chips are shown only before the user has sent any message. */
    private void buildSuggestionChips() {
        chips.removeAllViews();
        boolean hasUserMessage = false;
        for (Message m : messages) {
            if (m.fromUser) {
                hasUserMessage = true;
                break;
            }
        }
        chips.setVisibility(hasUserMessage ? View.GONE : View.VISIBLE);
        if (hasUserMessage) return;

        for (String s : SUGGESTIONS) {
            Button b = new Button(this);
            b.setText(s);
            b.setAllCaps(false);
            b.setTextColor(0xFFFFFFFF);
            b.setBackgroundResource(R.drawable.glass_button);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 6, 0, 6);
            b.setLayoutParams(lp);
            b.setOnClickListener(v -> {
                input.setText(s);
                send();
            });
            chips.addView(b);
        }
    }

    /** Answers from the local knowledge base. No external API is called. */
    private void send() {
        final String q = input.getText().toString().trim();
        if (q.isEmpty()) return;
        input.setText("");
        chips.setVisibility(View.GONE);

        messages.add(new Message(q, true));
        messages.add(new Message(KnowledgeBase.answer(q), false));
        adapter.notifyItemRangeInserted(messages.size() - 2, 2);
        recycler.scrollToPosition(messages.size() - 1);

        ChatStore.saveConversation(this, conversationId, messages);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem theme = menu.findItem(R.id.action_theme);
        if (theme != null) {
            theme.setTitle(ThemeManager.isDarkMode(this) ? R.string.light_mode : R.string.dark_mode);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_theme) {
            ThemeManager.toggle(this);
            return true;
        } else if (id == R.id.action_new_chat) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_rename) {
            showRenameDialog();
            return true;
        } else if (id == R.id.action_saved) {
            showSavedAnswers();
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_tasks) {
            startActivity(new Intent(this, TaskActivity.class));
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            UserManager.logout(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRenameDialog() {
        final EditText et = new EditText(this);
        et.setText(ChatStore.getTitle(this, conversationId));
        new AlertDialog.Builder(this)
                .setTitle(R.string.rename_chat)
                .setView(et)
                .setPositiveButton(R.string.save, (d, w) ->
                        ChatStore.renameConversation(this, conversationId, et.getText().toString()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showSavedAnswers() {
        List<String> saved = ChatStore.listSaved(this);
        if (saved.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_saved)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }
        String[] labels = new String[saved.size()];
        for (int i = 0; i < saved.size(); i++) {
            String s = saved.get(i).replaceAll("\\s+", " ");
            labels[i] = s.length() > 80 ? s.substring(0, 80) + "…" : s;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.saved_answers)
                .setItems(labels, null)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
