package com.clipboardflush;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * Invisible one-shot activity that floods the clipboard with 40 distinct
 * whitespace entries. Samsung Keyboard keeps only the most recent ~20-24
 * history items, so this pushes older items out of the visible history.
 *
 * An activity is required because Android 10+ blocks background apps from
 * writing to the clipboard; the foreground activity is the only way without
 * accessibility permissions.
 *
 * Flash is minimized via windowDisablePreview, no animation, 1x1 window and
 * short total duration (~120ms vs old ~600ms).
 */
public class ClipboardFlusherActivity extends Activity {

    private static final int FLUSH_COPY_COUNT = 40;
    // 3ms is enough for Samsung to register distinct entries (different lengths)
    // 40*3=120ms total vs old 40*15=600ms - much less visible flash
    private static final long DELAY_BETWEEN_COPIES_MS = 3L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        if (getWindow() != null) {
            getWindow().setLayout(1, 1);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        }

        final ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        Thread worker = new Thread(() -> {
            for (int i = 1; i <= FLUSH_COPY_COUNT; i++) {
                clipboard.setPrimaryClip(ClipData.newPlainText("", spaces(i)));
                if (DELAY_BETWEEN_COPIES_MS > 0) {
                    try {
                        Thread.sleep(DELAY_BETWEEN_COPIES_MS);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            runOnUiThread(() -> {
                finishAndRemoveTask();
                overridePendingTransition(0, 0);
            });
        }, "clipboard-flush-worker");
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private static String spaces(int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append('\u0020');
        }
        return builder.toString();
    }
}
