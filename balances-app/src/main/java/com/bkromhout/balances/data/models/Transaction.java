package com.bkromhout.balances.data.models;

import com.bkromhout.rrvl.UIDModel;
import io.realm.RealmObject;

/**
 * Represents a transaction in Realm.
 * <p>
 * A transaction is a record of a credit or debit to an {@link Balance}. All transactions are owned by some {@link
 * Balance}.
 */
public class Transaction extends RealmObject implements UIDModel {


    @Override
    public Object getUID() {
        return null;
    }
}
