package com.example.aichat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";
    private static final String DEFAULT_URL = "https://api.together.ai/v1/chat/completions";
    private static final String DEFAULT_KEY = "dahl_GxSwCZGe12r1uaSCESZF9cdrRhKKoVkw9";
    private static final String DEFAULT_MODEL = "meta-llama/Meta-Llama-3.1-70B-Instruct-Turbo";
    
    private final List<Message> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView recycler;
    private EditText input;
    private View sendBtn;
    private SharedPreferences prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        
        // Check login
        String currentUser = UserManager.getCurrentUser(this);
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_name) + " (" + currentUser + ")");
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        recycler = findViewById(R.id.recycler);
        input = findViewById(R.id.input);
        sendBtn = findViewById(R.id.send);

        adapter = new ChatAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recycler.setLayoutManager(lm);
        recycler.setAdapter(adapter);

        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        if (conversationId == null) {
            conversationId = UUID.randomUUID().toString();
            getIntent().putExtra(EXTRA_CONVERSATION_ID, conversationId);
        }
        List<Message> saved = ChatStore.loadMessages(this, conversationId);
        if (!saved.isEmpty()) {
            messages.addAll(saved);
        } else {
            messages.add(new Message(getString(R.string.welcome), false));
        }
        adapter.notifyDataSetChanged();

        sendBtn.setOnClickListener(v -> send());
    }

    private void send() {
        final String q = input.getText().toString().trim();
        if (q.isEmpty()) return;
        input.setText("");

        messages.add(new Message(q, true));
        final Message typing = new Message(getString(R.string.typing), false);
        messages.add(typing);
        adapter.notifyItemRangeInserted(messages.size() - 2, 2);
        scrollToEnd();
        sendBtn.setEnabled(false);

        final List<Message> history = new ArrayList<>(messages.subList(0, messages.size() - 1));
        final String url = getSetting("url", DEFAULT_URL);
        final String key = getSetting("key", DEFAULT_KEY);
        final String model = getSetting("model", DEFAULT_MODEL);

        executor.execute(() -> {
            String reply;
            try {
                reply = ApiClient.chat(url, key, model, history);
            } catch (Exception e) {
                reply = "خطا: " + e.getMessage();
            }
            final String finalReply = reply;
            ui.post(() -> {
                typing.text = finalReply;
                adapter.notifyItemChanged(messages.indexOf(typing));
                sendBtn.setEnabled(true);
                scrollToEnd();
                ChatStore.saveConversation(this, conversationId, messages);
            });
        });
    }

    /** Returns the saved value, or the built-in default if nothing non-empty was saved. */
    private String getSetting(String name, String fallback) {
        String saved = prefs.getString(name, "");
        return saved.trim().isEmpty() ? fallback : saved.trim();
    }

    private void scrollToEnd() {
        recycler.scrollToPosition(messages.size() - 1);
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
        if (item.getItemId() == R.id.action_settings) {
            showSettings();
            return true;
        } else if (item.getItemId() == R.id.action_theme) {
            ThemeManager.toggle(this);
            return true;
        } else if (item.getItemId() == R.id.action_new_chat) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_tasks) {
            startActivity(new Intent(this, TaskActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_history) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            UserManager.logout(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        View v = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        final EditText etUrl = v.findViewById(R.id.et_url);
        final EditText etKey = v.findViewById(R.id.et_key);
        final EditText etModel = v.findViewById(R.id.et_model);
        etUrl.setText(getSetting("url", DEFAULT_URL));
        etKey.setText(getSetting("key", DEFAULT_KEY));
        etModel.setText(getSetting("model", DEFAULT_MODEL));

        new AlertDialog.Builder(this)
                .setTitle(R.string.settings)
                .setView(v)
                .setPositiveButton(R.string.save, (d, w) -> prefs.edit()
                        .putString("url", etUrl.getText().toString().trim())
                        .putString("key", etKey.getText().toString().trim())
                        .putString("model", etModel.getText().toString().trim())
                        .apply())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
