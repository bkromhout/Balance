package com.bkromhout.balances;

import android.app.Application;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;

/**
 * Oft-used constants which must be loaded from resources at run-time.
 */
public class D {
    /**
     * Common green text color.
     */
    @ColorInt
    public final int TEXT_COLOR_GREEN;
    /**
     * Common yellow text color.
     */
    @ColorInt
    public final int TEXT_COLOR_YELLOW;
    /**
     * Common red text color.
     */
    @ColorInt
    public final int TEXT_COLOR_RED;

    // Only Balances should create an instance of this.
    D(Application application) {
        TEXT_COLOR_GREEN = ContextCompat.getColor(application, R.color.textColorGreen);
        TEXT_COLOR_YELLOW = ContextCompat.getColor(application, R.color.textColorYellow);
        TEXT_COLOR_RED = ContextCompat.getColor(application, R.color.textColorRed);
    }
}
