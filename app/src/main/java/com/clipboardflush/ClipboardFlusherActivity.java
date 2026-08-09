package com.clipboardflush;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;

/**
 * Invisible one-shot activity that floods the clipboard with 40 distinct
 * whitespace entries. Samsung Keyboard keeps only the most recent ~20-24
 * history items, so this pushes older items (including any sensitive copy)
 * out of the visible history.
 *
 * An activity is required because Android 10+ blocks background apps from
 * writing to the clipboard; the foreground activity is the only way to do
 * this without accessibility permissions or a foreground service.
 */
public class ClipboardFlusherActivity extends Activity {

    private static final int FLUSH_COPY_COUNT = 40;
    private static final long DELAY_BETWEEN_COPIES_MS = 15L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        Thread worker = new Thread(() -> {
            for (int i = 1; i <= FLUSH_COPY_COUNT; i++) {
                clipboard.setPrimaryClip(ClipData.newPlainText("", spaces(i)));
                try {
                    Thread.sleep(DELAY_BETWEEN_COPIES_MS);
                } catch (InterruptedException e) {
                    break;
                }
            }
            runOnUiThread(() -> {
                finish();
            });
        }, "clipboard-flush-worker");
        worker.setDaemon(true);
        worker.start();
    }

    /** Returns a string of {@code count} spaces. */
    private static String spaces(int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }
}
