package com.bkromhout.balances;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;
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

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                      .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                      .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).withMetaTables().build())
                      .build());
    }
}
