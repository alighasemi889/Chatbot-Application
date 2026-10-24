package com.example.aichat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private final List<String> ids = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_history);
        setTitle(R.string.history);

        ListView list = findViewById(R.id.list);
        TextView empty = findViewById(R.id.empty);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        list.setAdapter(adapter);
        list.setEmptyView(empty);

        list.setOnItemClickListener((parent, view, position, rowId) -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra(MainActivity.EXTRA_CONVERSATION_ID, ids.get(position));
            startActivity(i);
            finish();
        });

        list.setOnItemLongClickListener((parent, view, position, rowId) -> {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_delete)
                    .setPositiveButton(R.string.delete_chat, (d, w) -> {
                        ChatStore.deleteConversation(this, ids.get(position));
                        reload();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return true;
        });

        reload();
    }

    private void reload() {
        ids.clear();
        titles.clear();
        for (ChatStore.Summary s : ChatStore.listConversations(this)) {
            ids.add(s.id);
            titles.add(s.title.isEmpty() ? getString(R.string.untitled) : s.title);
        }
        adapter.notifyDataSetChanged();
    }
}
