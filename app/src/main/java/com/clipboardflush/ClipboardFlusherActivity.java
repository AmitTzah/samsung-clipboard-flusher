package com.clipboardflush;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * Invisible one-shot activity that floods the clipboard with 60 distinct
 * whitespace entries. Samsung Keyboard keeps only the most recent ~20-24
 * history items (some newer One UI builds report up to ~50), so this pushes
 * older items out of the visible history with margin.
 *
 * An activity is required because Android 10+ blocks background apps from
 * writing to the clipboard; the foreground activity is the only way without
 * accessibility permissions.
 *
 * Flash is minimized via windowDisablePreview, no animation, 1x1 window and
 * short total duration (~300ms).
 */
public class ClipboardFlusherActivity extends Activity {

    private static final int FLUSH_COPY_COUNT = 60;
    // 5ms lets Samsung register each distinct entry (different lengths);
    // 60*5=300ms total vs old 40*3=120ms - slightly longer flash, but every
    // write lands so the flush actually evicts the older item.
    private static final long DELAY_BETWEEN_COPIES_MS = 5L;

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
