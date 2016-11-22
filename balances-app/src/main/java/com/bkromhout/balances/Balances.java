package com.bkromhout.balances;

import android.app.Application;
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
     * Static instance of {@link D}.
     */
    private static D D;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        D = new D(this);

        Realm.init(this);
        // TODO Get the index working.
        EventBus.builder()/*.addIndex(new EventBusIndex())*/.installDefaultEventBus();

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
     * Get static {@link D} instance.
     */
    public static D getD() {
        return D;
    }
}
