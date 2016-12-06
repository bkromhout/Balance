package com.bkromhout.balances.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import io.realm.Realm;

/**
 * Simple AppWidgetProvider implementation which delegates all major functionality to {@link WidgetHandler}.
 */
public class BalanceWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them.
        try (Realm realm = Realm.getDefaultInstance()) {
            for (int appWidgetId : appWidgetIds)
                WidgetHandler.updateWidget(context, realm, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds)
            WidgetHandler.removeWidgetEntry(context, appWidgetId);
    }
}

