package com.bkromhout.balance;

import timber.log.Timber;

/**
 * Debug version of custom Application class.
 * @see Balance
 */
public class BalanceDebug extends Balance {
    @Override
    public void onCreate() {
        Timber.plant(new Timber.DebugTree());
        super.onCreate();
    }
}
