package com.example.aichat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LearningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_learning);
        setTitle("مسیر یادگیری من");

        Button quiz5 = findViewById(R.id.btn_quiz5);
        Button quiz10 = findViewById(R.id.btn_quiz10);
        quiz5.setOnClickListener(v -> startQuiz(5));
        quiz10.setOnClickListener(v -> startQuiz(10));
    }

    private void startQuiz(int count) {
        Intent i = new Intent(this, QuizActivity.class);
        i.putExtra(QuizActivity.EXTRA_COUNT, count);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        QuizStore.Stats s = QuizStore.stats(this);
        TextView stats = findViewById(R.id.tv_stats);
        stats.setText(
                "آزمون‌های انجام‌شده: " + s.quizzes + "\n" +
                "سؤال‌های پاسخ‌داده‌شده: " + s.answered + "\n" +
                "پاسخ‌های درست: " + s.correct + "\n" +
                "میانگین نمره: " + s.averagePercent + "٪\n" +
                "بهترین نمره: " + s.bestPercent + "٪");

        TextView recent = findViewById(R.id.tv_recent);
        if (s.quizzes == 0) {
            recent.setText("هنوز آزمونی انجام نداده‌اید.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String line : QuizStore.recent(this, 10)) {
                sb.append(line).append("\n");
            }
            recent.setText(sb.toString().trim());
        }
    }
}
