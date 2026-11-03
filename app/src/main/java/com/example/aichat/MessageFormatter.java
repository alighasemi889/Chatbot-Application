package com.example.aichat;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.TypefaceSpan;

/** Renders ```fenced``` code blocks as monospace text on a tinted background. */
public final class MessageFormatter {
    private MessageFormatter() {}

    public static CharSequence format(String raw, int codeBackground) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        String[] parts = raw.split("```", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 0) {
                sb.append(parts[i]);
                continue;
            }
            String code = parts[i];
            int nl = code.indexOf('\n');
            if (nl >= 0 && code.substring(0, nl).trim().matches("[A-Za-z0-9+#._-]*")) {
                code = code.substring(nl + 1); // drop language hint line
            }
            code = code.replaceAll("\\n+$", "");
            int start = sb.length();
            sb.append(code);
            int end = sb.length();
            if (end > start) {
                sb.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new BackgroundColorSpan(codeBackground), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return sb;
    }

    /** Plain text without fence markers, for copying. */
    public static String plain(String raw) {
        return format(raw, 0).toString();
    }
}
