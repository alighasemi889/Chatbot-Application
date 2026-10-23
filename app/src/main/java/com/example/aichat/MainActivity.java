package com.example.aichat;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String DEFAULT_URL = "https://api.openai.com/v1/chat/completions";
    private final List<Message> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView recycler;
    private EditText input;
    private View sendBtn;
    private SharedPreferences prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler ui = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        recycler = findViewById(R.id.recycler);
        input = findViewById(R.id.input);
        sendBtn = findViewById(R.id.send);

        adapter = new ChatAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recycler.setLayoutManager(lm);
        recycler.setAdapter(adapter);

        messages.add(new Message(getString(R.string.welcome), false));
        adapter.notifyItemInserted(0);

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
        final String url = prefs.getString("url", DEFAULT_URL);
        final String key = prefs.getString("key", "");
        final String model = prefs.getString("model", "gpt-4o-mini");

        executor.execute(() -> {
            String reply;
            try {
                if (key.isEmpty()) {
                    Thread.sleep(700);
                    reply = getString(R.string.demo_reply, q);
                } else {
                    reply = ApiClient.chat(url, key, model, history);
                }
            } catch (Exception e) {
                reply = "خطا: " + e.getMessage();
            }
            final String finalReply = reply;
            ui.post(() -> {
                typing.text = finalReply;
                adapter.notifyItemChanged(messages.indexOf(typing));
                sendBtn.setEnabled(true);
                scrollToEnd();
            });
        });
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            showSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        View v = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        final EditText etUrl = v.findViewById(R.id.et_url);
        final EditText etKey = v.findViewById(R.id.et_key);
        final EditText etModel = v.findViewById(R.id.et_model);
        etUrl.setText(prefs.getString("url", DEFAULT_URL));
        etKey.setText(prefs.getString("key", ""));
        etModel.setText(prefs.getString("model", "gpt-4o-mini"));

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
