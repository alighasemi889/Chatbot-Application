package com.example.aichat;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Shows the version of the installed build, read from PackageManager (never hard-coded). */
public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.apply(this);
        setContentView(R.layout.activity_about);
        setTitle(R.string.about_app);

        TextView version = findViewById(R.id.version_text);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            long code = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    ? info.getLongVersionCode() : info.versionCode;
            version.setText(getString(R.string.version_format, info.versionName, String.valueOf(code)));
        } catch (PackageManager.NameNotFoundException e) {
            version.setText(R.string.version_unknown);
        }
        LoginActivity.animateIn(findViewById(R.id.card));
    }
}
