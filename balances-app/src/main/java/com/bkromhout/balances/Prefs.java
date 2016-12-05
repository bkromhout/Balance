package com.bkromhout.balances;

import android.content.SharedPreferences;

/**
 * Wrapper class for SharedPreferences.
 */
public class Prefs {
    /*
     * Key Strings.
     */
    public static final String EDIT_CATEGORIES = Balances.get().getString(R.string.key_edit_categories);

    /**
     * Shared Preferences.
     */
    private final SharedPreferences prefs;

    // Only Balances should create an instance of this.
    Prefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }
}
