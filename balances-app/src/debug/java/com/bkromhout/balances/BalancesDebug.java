package com.bkromhout.balances;

import timber.log.Timber;

/**
 * Debug version of custom Application class.
 * @see Balances
 */
public class BalancesDebug extends Balances {
    @Override
    public void onCreate() {
        Timber.plant(new Timber.DebugTree());
        super.onCreate();
    }
}
