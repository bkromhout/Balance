package com.bkromhout.balances;

import android.app.Application;
import android.preference.PreferenceManager;
import com.bkromhout.balance.EventBusIndex;
import com.bkromhout.balances.data.UniqueIdFactory;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import org.greenrobot.eventbus.EventBus;

/**
 * Custom Application class.
 */
public class Balances extends Application {
    /**
     * Realm file name.
     */
    private static final String REALM_FILE_NAME = "balances.realm";
    /**
     * Realm schema version.
     */
    private static final long REALM_SCHEMA_VERSION = 0;

    /**
     * Static instance of application context.
     */
    private static Balances INSTANCE;
    /**
     * Instance of {@link D}.
     */
    private D D;
    /**
     * Instance of {@link Prefs}.
     */
    private Prefs prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        D = new D(this);
        prefs = new Prefs(PreferenceManager.getDefaultSharedPreferences(this));

        EventBus.builder().addIndex(new EventBusIndex()).installDefaultEventBus();

        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .name(REALM_FILE_NAME)
                .schemaVersion(REALM_SCHEMA_VERSION)
                .build());
        try (Realm realm = Realm.getDefaultInstance()) {
            UniqueIdFactory.getInstance().initializeDefault(realm);
        }
    }

    /**
     * Get static Application instance.
     */
    public static Balances get() {
        return INSTANCE;
    }

    /**
     * Get {@link D} instance.
     */
    public static D getD() {
        return INSTANCE.D;
    }

    /**
     * Get {@link Prefs} instance.
     */
    public static Prefs getPrefs() {
        return INSTANCE.prefs;
    }
}
