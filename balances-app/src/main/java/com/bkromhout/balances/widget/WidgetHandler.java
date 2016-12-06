package com.bkromhout.balances.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;
import com.bkromhout.balances.Balances;
import com.bkromhout.balances.R;
import com.bkromhout.balances.activities.BalanceDetailsActivity;
import com.bkromhout.balances.activities.NewTransactionActivity;
import com.bkromhout.balances.data.CurrencyUtils;
import com.bkromhout.balances.data.models.Balance;
import com.bkromhout.balances.data.models.BalanceFields;
import io.realm.Realm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Since there are many places in the app which need to be able to trigger widget content updates, as much of the logic
 * as possible is contained here in order to minimize code duplication.
 */
public class WidgetHandler {
    private static final String WIDGET_ENTRIES = "Balance_Widget_Entries";
    private static final String KEY_PREFIX = "widget_";
    private static final long INVALID_BALANCE = -1L;

    /**
     * Saves a new widget entry to the widget shared preferences. This entry uses the widget ID as the key (prepended
     * with {@link #KEY_PREFIX}), and the {@link Balance} UID as the value.
     * @param context    Context to use.
     * @param widgetId   Widget ID.
     * @param balanceUid {@link Balance} unique ID.
     */
    static void saveWidgetEntry(Context context, int widgetId, long balanceUid) {
        SharedPreferences.Editor editor = context.getSharedPreferences(WIDGET_ENTRIES, Context.MODE_PRIVATE).edit();
        editor.putLong(KEY_PREFIX + widgetId, balanceUid).apply();
    }

    /**
     * Removes a widget entry from the widget shared preferences.
     * @param context  Context to use.
     * @param widgetId ID of the widget whose entry should be removed.
     */
    static void removeWidgetEntry(Context context, int widgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WIDGET_ENTRIES, Context.MODE_PRIVATE).edit();
        prefs.remove(KEY_PREFIX + widgetId).apply();
    }

    /**
     * Update a widget. This method will use the given instance of Realm, along with the entry for the given widget ID,
     * to determine what {@link Balance} object to get data from. Either {@link #updateWidgetContent(Context, Balance,
     * int)} or {@link #invalidateWidgetContent(Context, int)} will then be called, depending on if the associated
     * {@link Balance} object is present and valid.
     * <p>
     * It is assumed that an entry using the given widget ID already exists before this method is called. Other than
     * that, this method is very forgiving.
     * <p>
     * The main difference between this method and the {@link #updateWidgetsForBalance(Context, Realm, long)} and {@link
     * #invalidateWidgetsForBalance(Context, long)} methods is that this one is intended to be called when we only know
     * the widget ID, and updates/invalidates a single widget; whereas those methods are called when we know a balance
     * UID, and wish to update/invalidate any and all widgets tied to it.
     * @param context  Context to use.
     * @param realm    Instance of Realm to use.
     * @param widgetId ID of the widget whose content should be updated.
     */
    static void updateWidget(Context context, Realm realm, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(WIDGET_ENTRIES, Context.MODE_PRIVATE);
        long balanceUid = prefs.getLong(KEY_PREFIX + widgetId, INVALID_BALANCE);
        Balance balance = realm.where(Balance.class).equalTo(BalanceFields.UNIQUE_ID, balanceUid).findFirst();

        if (balance != null && balance.isValid()) {
            // Valid Balance object, update content of widget as usual.
            updateWidgetContent(context, balance, widgetId);
        } else {
            // Invalid Balance object, need to invalidate the widget and its entry.
            prefs.edit().putLong(KEY_PREFIX + widgetId, INVALID_BALANCE).apply();
            invalidateWidgetContent(context, widgetId);
        }
    }

    /**
     * Updates any widgets which are tied to the {@link Balance} with the given UID.
     * <p>
     * It is assumed that the {@link Balance} whose UID is given is present and valid.
     * @param context    Context to use.
     * @param realm      Instance of Realm to use.
     * @param balanceUid {@link Balance} unique ID.
     */
    public static void updateWidgetsForBalance(Context context, Realm realm, long balanceUid) {
        SharedPreferences prefs = context.getSharedPreferences(WIDGET_ENTRIES, Context.MODE_PRIVATE);
        List<Integer> widgetIds = getWidgetIdsForBalance(prefs, balanceUid);
        if (widgetIds.isEmpty())
            return;

        // Update all widgets we found IDs for.
        Balance balance = realm.where(Balance.class).equalTo(BalanceFields.UNIQUE_ID, balanceUid).findFirst();
        for (int widgetId : widgetIds)
            updateWidgetContent(context, balance, widgetId);
    }

    /**
     * Invalidates any widgets which are tied to the {@link Balance} with the given UID.
     * <p>
     * An invalidated widget entry will still be present in the shared preferences, but its value will be {@link
     * #INVALID_BALANCE}. The widget itself will be reconfigured by calling {@link #invalidateWidgetContent(Context,
     * int)}.
     * @param context    Context to use.
     * @param balanceUid {@link Balance} unique ID.
     */
    public static void invalidateWidgetsForBalance(Context context, long balanceUid) {
        SharedPreferences prefs = context.getSharedPreferences(WIDGET_ENTRIES, Context.MODE_PRIVATE);
        List<Integer> invalidWidgetIds = getWidgetIdsForBalance(prefs, balanceUid);
        if (invalidWidgetIds.isEmpty())
            return;

        // For all invalid widget IDs...
        SharedPreferences.Editor editor = prefs.edit();
        for (int invalidWidgetId : invalidWidgetIds) {
            // Set value for entry to INVALID_BALANCE.
            editor.putLong(KEY_PREFIX + invalidWidgetId, INVALID_BALANCE);
            // Invalidate widget content.
            invalidateWidgetContent(context, invalidWidgetId);
        }
        editor.apply();
    }

    /**
     * Get a list of widget IDs which are tied to the given balance UID.
     * @param prefs      SharedPreferences.
     * @param balanceUid {@link Balance} unique ID.
     * @return List of widget IDs.
     */
    private static List<Integer> getWidgetIdsForBalance(SharedPreferences prefs, long balanceUid) {
        List<Integer> widgetIds = new ArrayList<>();
        // Loop over all entries.
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            // Check to see if this entry's value is equal to the given balanceUid.
            long value = (Long) entry.getValue();
            if (balanceUid == value)
                // If so, add the widget ID to the list.
                widgetIds.add(Integer.valueOf(entry.getKey().replace(KEY_PREFIX, "")));
        }
        return widgetIds;
    }

    /**
     * Updates the contents of the widget with the given ID so that it reflects the latest data from the given {@link
     * Balance}.
     * @param context  Context to use.
     * @param balance  {@link Balance} whose data will be used to update the widget.
     * @param widgetId ID of the widget whose content should be updated.
     */
    private static void updateWidgetContent(Context context, Balance balance, int widgetId) {
        // Create RemoteViews.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.balance_widget);

        // Update views' data.
        views.setTextViewText(R.id.widget_balance_name, balance.name);
        long amount = balance.getTotalBalance();
        views.setTextViewText(R.id.widget_balance_amount, CurrencyUtils.longToCurrencyString(amount));
        views.setTextColor(R.id.widget_balance_amount, amount > balance.yellowLimit ? Balances.getD().TEXT_COLOR_GREEN
                : (amount > balance.redLimit ? Balances.getD().TEXT_COLOR_YELLOW
                : Balances.getD().TEXT_COLOR_RED));

        // Make sure add Transaction button is showing.
        views.setViewVisibility(R.id.widget_add_trans, View.VISIBLE);

        // Update views' click handlers.
        Intent balanceDetailsIntent = new Intent(context, BalanceDetailsActivity.class);
        balanceDetailsIntent.putExtra(BalanceFields.UNIQUE_ID, balance.uniqueId);
        views.setOnClickPendingIntent(R.id.widget_content,
                PendingIntent.getActivity(context, widgetId, balanceDetailsIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        Intent addTransIntent = new Intent(context, NewTransactionActivity.class);
        addTransIntent.putExtra(BalanceFields.UNIQUE_ID, balance.uniqueId);
        addTransIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        views.setOnClickPendingIntent(R.id.widget_add_trans,
                PendingIntent.getActivity(context, widgetId, addTransIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        // Instruct the widget manager to update the widget.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(widgetId, views);
    }

    /**
     * Set the contents of the widget with the given ID so that clicking it will open {@link ConfigureWidgetActivity},
     * as if it had just been added to the home screen.
     * @param context  Context to use.
     * @param widgetId ID of the widget whose content should be invalidated.
     */
    private static void invalidateWidgetContent(Context context, int widgetId) {
        // Create RemoteViews.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.balance_widget);

        // Change to default text content and color.
        views.setTextViewText(R.id.widget_balance_name, context.getString(R.string.widget_no_balance));
        views.setTextViewText(R.id.widget_balance_amount, context.getString(R.string.widget_no_balance_sub));
        views.setTextColor(R.id.widget_balance_amount,
                ContextCompat.getColor(context, R.color.textColorPrimaryInverse));

        // Hide add Transaction button.
        views.setViewVisibility(R.id.widget_add_trans, View.GONE);

        // Create intent for launching config activity.
        Intent intent = new Intent(context, ConfigureWidgetActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        views.setOnClickPendingIntent(R.id.widget_content,
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

        // Instruct the widget manager to update the widget.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(widgetId, views);
    }
}
