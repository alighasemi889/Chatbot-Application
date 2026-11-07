package com.example.aichat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/** Category chips + question list. Tapping a question returns it to the chat, which sends it. */
public class PresetActivity extends AppCompatActivity {
    public static final String EXTRA_QUESTION = "question";

    private final List<String> shown = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<PresetQuestions.Category> categories;
    private String lang;
    private int selected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_presets);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        lang = UserPrefs.language(this);
        categories = PresetQuestions.all();

        findViewById(R.id.back_btn).setOnClickListener(v -> finish());

        ChipGroup chips = findViewById(R.id.chip_group);
        for (int i = 0; i < categories.size(); i++) {
            final int index = i;
            Chip chip = new Chip(this);
            chip.setText(PresetQuestions.categoryName(categories.get(i), lang));
            chip.setCheckable(true);
            chip.setChecked(i == selected);
            chip.setOnClickListener(v -> showCategory(index));
            chips.addView(chip);
        }

        ListView list = findViewById(R.id.list);
        adapter = new ArrayAdapter<>(this, R.layout.item_history, R.id.text1, shown);
        list.setAdapter(adapter);
        list.setOnItemClickListener((AdapterView<?> parent, View view, int pos, long id) -> {
            Intent data = new Intent().putExtra(EXTRA_QUESTION, shown.get(pos));
            setResult(RESULT_OK, data);
            finish();
        });
        showCategory(selected);
    }

    private void showCategory(int index) {
        selected = index;
        shown.clear();
        for (PresetQuestions.Question q : categories.get(index).questions) {
            shown.add(PresetQuestions.questionText(q, lang));
        }
        adapter.notifyDataSetChanged();
    }
}
