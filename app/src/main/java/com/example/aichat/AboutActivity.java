package com.example.aichat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AboutActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private TextView status;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_about);
        setTitle(R.string.about_app);

        ((TextView) findViewById(R.id.tv_version))
                .setText(getString(R.string.current_version, UpdateManager.currentVersion(this)));
        status = findViewById(R.id.tv_status);
        btn = findViewById(R.id.btn_check);
        btn.setOnClickListener(v -> checkForUpdate());
    }

    private void checkForUpdate() {
        btn.setEnabled(false);
        status.setText(R.string.checking);
        final String current = UpdateManager.currentVersion(this);
        executor.execute(() -> {
            try {
                UpdateManager.Release rel = UpdateManager.fetchLatest();
                ui.post(() -> {
                    btn.setEnabled(true);
                    if (rel == null) {
                        status.setText(R.string.no_release);
                    } else if (!UpdateManager.isNewer(rel.version, current)) {
                        status.setText(R.string.up_to_date);
                    } else {
                        status.setText("");
                        askToUpdate(rel);
                    }
                });
            } catch (Exception e) {
                ui.post(() -> {
                    btn.setEnabled(true);
                    status.setText(getString(R.string.check_failed, e.getMessage()));
                });
            }
        });
    }

    private void askToUpdate(UpdateManager.Release rel) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.update_available, rel.version))
                .setPositiveButton(R.string.download_install, (d, w) -> downloadAndInstall(rel))
                .setNegativeButton(R.string.later, null)
                .show();
    }

    private void downloadAndInstall(UpdateManager.Release rel) {
        btn.setEnabled(false);
        executor.execute(() -> {
            try {
                File apk = UpdateManager.download(this, rel.apkUrl,
                        pct -> ui.post(() -> status.setText(getString(R.string.downloading, pct))));
                ui.post(() -> {
                    btn.setEnabled(true);
                    status.setText("");
                    if (!UpdateManager.install(this, apk)) {
                        status.setText(R.string.allow_install);
                    }
                });
            } catch (Exception e) {
                ui.post(() -> {
                    btn.setEnabled(true);
                    status.setText(getString(R.string.install_failed, e.getMessage()));
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
