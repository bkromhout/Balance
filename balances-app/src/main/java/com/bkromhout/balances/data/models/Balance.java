package com.bkromhout.balances.data.models;

import com.bkromhout.rrvl.UIDModel;
import io.realm.RealmObject;

/**
 * Represents a balance in Realm.
 * <p>
 * A balance is a collection of {@link Transaction}s.
 */
public class Balance extends RealmObject implements UIDModel {


    @Override
    public Object getUID() {
        return null;
    }
}
