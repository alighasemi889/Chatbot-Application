package com.example.aichat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";
    static final String DEFAULT_URL = "https://api.together.ai/v1/chat/completions";
    static final String DEFAULT_KEY = "dahl_GxSwCZGe12r1uaSCESZF9cdrRhKKoVkw9";
    static final String DEFAULT_MODEL = "meta-llama/Meta-Llama-3.1-70B-Instruct-Turbo";

    private final List<Message> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView recycler;
    private EditText input;
    private View sendBtn;
    private SharedPreferences prefs;
    private DrawerLayout drawer;
    private View homePanel;
    private TtsHelper tts;
    private SpeechHelper speech;
    private ActivityResultLauncher<Intent> presetLauncher;
    private ActivityResultLauncher<String> micPermission;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler ui = new Handler(Looper.getMainLooper());
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
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        recycler = findViewById(R.id.recycler);
        input = findViewById(R.id.input);
        sendBtn = findViewById(R.id.send);

        setupDrawer(currentUser);

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
        setupHome();
        tts = new TtsHelper(this);
        speech = new SpeechHelper();
        initLaunchers();
        BottomNav.setup(this, findViewById(R.id.bottom_nav), R.id.nav_home);
        updateMode();
    }

    private void setupDrawer(String currentUser) {
        drawer = findViewById(R.id.drawer_layout);
        NavigationView nav = findViewById(R.id.nav_view);


        TextView headerUser = nav.getHeaderView(0).findViewById(R.id.header_user);
        headerUser.setText(getString(R.string.nav_header_user, currentUser));

        findViewById(R.id.menu_btn).setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        nav.setNavigationItemSelectedListener(item -> {
            drawer.closeDrawer(GravityCompat.START);
            return handleAction(item.getItemId());
        });
    }

    /** Home-screen shortcuts. Features without a backend show a clear "not available yet" message. */
    private void setupHome() {
        homePanel = findViewById(R.id.home_panel);
        findViewById(R.id.notif_btn).setOnClickListener(v -> startActivity(new Intent(this, TaskActivity.class)));
        findViewById(R.id.mic_btn).setOnClickListener(v -> startVoiceInput());
        findViewById(R.id.q_call).setOnClickListener(v -> Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show());
        findViewById(R.id.q_message).setOnClickListener(v -> Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show());
        findViewById(R.id.q_apps).setOnClickListener(v -> presetLauncher.launch(new Intent(this, PresetActivity.class)));
        findViewById(R.id.q_reminder).setOnClickListener(v -> startActivity(new Intent(this, TaskActivity.class)));
        findViewById(R.id.q_ai).setOnClickListener(v -> input.requestFocus());
        findViewById(R.id.q_more).setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        findViewById(R.id.recent_card).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        findViewById(R.id.see_all).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
    }

    /** Shows the home hero until the conversation has real messages, then the chat list. */
    private void updateMode() {
        if (homePanel == null) return;
        boolean chatting = messages.size() > 1;
        homePanel.setVisibility(chatting ? View.GONE : View.VISIBLE);
        recycler.setVisibility(chatting ? View.VISIBLE : View.GONE);
    }

    private void initLaunchers() {
        presetLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
            if (r.getResultCode() == RESULT_OK && r.getData() != null) {
                String question = r.getData().getStringExtra(PresetActivity.EXTRA_QUESTION);
                if (question != null) send(question);
            }
        });
        micPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                startVoiceInput();
            } else {
                Toast.makeText(this, R.string.mic_permission, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Speech-to-text: fills the input box so the user can review before sending. */
    private void startVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            micPermission.launch(Manifest.permission.RECORD_AUDIO);
            return;
        }
        Toast.makeText(this, R.string.listening, Toast.LENGTH_SHORT).show();
        speech.start(this, UserPrefs.speechLangTag(this), new SpeechHelper.Callback() {
            @Override
            public void onResult(String text) {
                input.setText(text);
                input.setSelection(text.length());
            }

            @Override
            public void onError(int messageRes) {
                Toast.makeText(MainActivity.this, messageRes, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void send() {
        String q = input.getText().toString().trim();
        input.setText("");
        send(q);
    }

    /** Sends a question (typed, spoken, or from the preset library) and gets the answer. */
    private void send(final String q) {
        if (q.isEmpty()) return;

        messages.add(new Message(q, true));
        final Message typing = new Message(getString(R.string.typing), false);
        messages.add(typing);
        updateMode();
        adapter.notifyItemRangeInserted(messages.size() - 2, 2);
        scrollToEnd();
        sendBtn.setEnabled(false);

        KnowledgeBase.Entry kb = KnowledgeBase.find(q);
        if (kb != null) {
            typing.text = KnowledgeBase.format(kb);
            if (UserPrefs.assistantVoice(this)) tts.speak(typing.text, UserPrefs.speechLangTag(this));
            adapter.notifyItemChanged(messages.indexOf(typing));
            sendBtn.setEnabled(true);
            scrollToEnd();
            ChatStore.saveConversation(this, conversationId, messages);
            return;
        }

        final List<Message> history = new ArrayList<>(messages.subList(0, messages.size() - 1));
        final String url = getSetting("url", DEFAULT_URL);
        final String key = getSetting("key", DEFAULT_KEY);
        final String model = getSetting("model", DEFAULT_MODEL);

        executor.execute(() -> {
            String reply;
            try {
                reply = ApiClient.chat(url, key, model, history, UserPrefs.answerLangHint(this));
            } catch (Exception e) {
                reply = "خطا: " + e.getMessage();
            }
            final String finalReply = reply;
            ui.post(() -> {
                typing.text = finalReply;
                if (UserPrefs.assistantVoice(this)) tts.speak(finalReply, UserPrefs.speechLangTag(this));
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

    /** Shared by the sidebar and the overflow menu. */
    private boolean handleAction(int id) {
        if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_new_chat) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_tasks) {
            startActivity(new Intent(this, TaskActivity.class));
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(this, HistoryActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_logout) {
            UserManager.logout(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            return false;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        speech.destroy();
        tts.shutdown();
    }
}
