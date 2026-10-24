package com.example.aichat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskActivity extends AppCompatActivity {
    private final List<TaskStore.Task> tasks = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_tasks);
        setTitle(R.string.tasks);

        Button addBtn = findViewById(R.id.add_task);
        ListView list = findViewById(R.id.list);
        TextView empty = findViewById(R.id.empty);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
        list.setAdapter(adapter);
        list.setEmptyView(empty);

        addBtn.setOnClickListener(v -> askTitle());

        list.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_delete_task)
                    .setPositiveButton(R.string.delete_chat, (d, w) -> {
                        TaskStore.Task t = tasks.get(position);
                        ReminderReceiver.cancel(this, t);
                        TaskStore.remove(this, t);
                        reload();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return true;
        });

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        reload();
    }

    private void askTitle() {
        EditText input = new EditText(this);
        input.setHint(R.string.task_title_hint);
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_task)
                .setView(input)
                .setPositiveButton(R.string.next, (d, w) -> {
                    String title = input.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, R.string.empty_field, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pickDateTime(title);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void pickDateTime(String title) {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                new TimePickerDialog(this, (tv, hour, minute) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, day, hour, minute, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    if (c.getTimeInMillis() <= System.currentTimeMillis()) {
                        Toast.makeText(this, R.string.task_past, Toast.LENGTH_LONG).show();
                        return;
                    }
                    TaskStore.Task t = TaskStore.add(this, title, c.getTimeInMillis());
                    ReminderReceiver.schedule(this, t);
                    reload();
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)).show(),
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void reload() {
        tasks.clear();
        labels.clear();
        tasks.addAll(TaskStore.load(this));
        for (TaskStore.Task t : tasks) {
            labels.add(t.title + "  —  " + fmt.format(t.time));
        }
        adapter.notifyDataSetChanged();
    }
}
