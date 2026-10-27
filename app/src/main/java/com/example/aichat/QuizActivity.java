package com.example.aichat;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class QuizActivity extends AppCompatActivity {
    public static final String EXTRA_COUNT = "count";
    private static final int GREEN = 0xFF2E7D32;
    private static final int RED = 0xFFC62828;
    private static final int NEUTRAL = 0x33FFFFFF;

    private List<QuestionBank.Question> questions;
    private int index = 0;
    private int correct = 0;
    private boolean answered = false;

    private TextView progress;
    private TextView questionText;
    private TextView resultText;
    private final Button[] options = new Button[4];
    private Button nextBtn;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_quiz);
        setTitle("آزمون");

        progress = findViewById(R.id.tv_progress);
        questionText = findViewById(R.id.tv_question);
        resultText = findViewById(R.id.tv_result);
        options[0] = findViewById(R.id.opt0);
        options[1] = findViewById(R.id.opt1);
        options[2] = findViewById(R.id.opt2);
        options[3] = findViewById(R.id.opt3);
        nextBtn = findViewById(R.id.btn_next);

        int count = getIntent().getIntExtra(EXTRA_COUNT, 5);
        questions = QuestionBank.pick(count);

        for (int i = 0; i < options.length; i++) {
            final int choice = i;
            options[i].setOnClickListener(v -> onChoose(choice));
        }
        nextBtn.setOnClickListener(v -> onNext());
        showQuestion();
    }

    private void showQuestion() {
        answered = false;
        QuestionBank.Question q = questions.get(index);
        progress.setText("سؤال " + (index + 1) + " از " + questions.size());
        questionText.setText(q.text);
        for (int i = 0; i < options.length; i++) {
            options[i].setText(q.options[i]);
            options[i].setBackgroundColor(NEUTRAL);
            options[i].setEnabled(true);
            options[i].setVisibility(View.VISIBLE);
        }
        nextBtn.setEnabled(false);
        nextBtn.setText(index == questions.size() - 1 ? "پایان آزمون" : "سؤال بعدی");
        resultText.setVisibility(View.GONE);
    }

    private void onChoose(int choice) {
        if (answered) return;
        answered = true;
        QuestionBank.Question q = questions.get(index);
        for (Button b : options) b.setEnabled(false);
        if (choice == q.correct) {
            correct++;
            options[choice].setBackgroundColor(GREEN);
        } else {
            options[choice].setBackgroundColor(RED);
            options[q.correct].setBackgroundColor(GREEN);
        }
        nextBtn.setEnabled(true);
    }

    private void onNext() {
        if (!answered) return;
        index++;
        if (index < questions.size()) {
            showQuestion();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        int total = questions.size();
        int percent = total > 0 ? correct * 100 / total : 0;
        QuizStore.save(this, correct, total);

        questionText.setText("آزمون تمام شد");
        progress.setText("");
        for (Button b : options) b.setVisibility(View.GONE);
        resultText.setText("نمره‌ی شما: " + correct + " از " + total + " (" + percent + "٪)");
        resultText.setVisibility(View.VISIBLE);
        nextBtn.setEnabled(true);
        nextBtn.setText("بازگشت");
        nextBtn.setOnClickListener(v -> finish());

        if (total == 10) {
            playCheer();
        }
    }

    /** Short fanfare built from system tones, so no audio file is required. */
    private void playCheer() {
        final ToneGenerator tg;
        try {
            tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 90);
        } catch (RuntimeException e) {
            return;
        }
        int[] tones = {
                ToneGenerator.TONE_PROP_BEEP,
                ToneGenerator.TONE_PROP_BEEP2,
                ToneGenerator.TONE_CDMA_HIGH_L,
                ToneGenerator.TONE_PROP_ACK
        };
        long[] delays = {0, 220, 440, 760};
        for (int i = 0; i < tones.length; i++) {
            final int t = tones[i];
            handler.postDelayed(() -> tg.startTone(t, 300), delays[i]);
        }
        handler.postDelayed(tg::release, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
