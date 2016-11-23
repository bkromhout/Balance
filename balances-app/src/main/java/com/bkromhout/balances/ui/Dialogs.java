package com.bkromhout.balances.ui;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bkromhout.balances.R;
import com.bkromhout.balances.events.ActionEvent;
import org.greenrobot.eventbus.EventBus;

/**
 * Utility class for constructing dialogs.
 */
public class Dialogs {
    /**
     * Shows a simple confirmation dialog using the given {@code title}, {@code text}, and {@code posText} strings. Upon
     * the positive button being clicked, fires an {@link ActionEvent} using the given {@code actionId}.
     * @param ctx      Context to use.
     * @param title    String resource to use for title.
     * @param text     String resource to use for text.
     * @param posText  String resource to use for positive button text.
     * @param actionId Action ID to send if Yes is clicked.
     */
    public static void simpleConfirmDialog(final Context ctx, @StringRes final int title, @StringRes final int text,
                                           @StringRes final int posText, @IdRes final int actionId) {
        new MaterialDialog.Builder(ctx)
                .title(title)
                .content(text)
                .positiveText(posText)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> EventBus.getDefault().post(new ActionEvent(actionId, null)))
                .show();
    }
}
