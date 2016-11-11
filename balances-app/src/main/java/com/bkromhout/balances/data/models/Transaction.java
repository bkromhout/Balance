package com.bkromhout.balances.data.models;

import com.bkromhout.balances.data.UniqueIdFactory;
import com.bkromhout.rrvl.UIDModel;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

import java.util.Date;

/**
 * Represents a transaction in Realm.
 * <p>
 * A transaction is a record of a credit or debit to an {@link Balance}. All transactions are owned by some {@link
 * Balance}.
 */
public class Transaction extends RealmObject implements UIDModel {
    /**
     * A unique long value.
     */
    @PrimaryKey
    public long uniqueId;
    /**
     * The name of the balance.
     */
    public String name;
    /**
     * The amount of the transaction.
     */
    public long amount;
    /**
     * The timestamp of the transaction.
     */
    @Index
    public Date timestamp;
    /**
     * The check number of this transaction.
     */
    public int checkNumber;
    /**
     * Note for this transaction.
     */
    public String note;

    // Empty constructor for Realm.
    public Transaction() {
    }

    /**
     * Create a new {@link Transaction}.
     * @param name Name of the transaction.
     * @param amount Amount of the transaction.
     * @param timestamp Timestamp of the transaction.
     */
    public Transaction(String name, long amount, Date timestamp) {
        this.name = name;
        this.amount = amount;
        this.timestamp = timestamp;
        this.checkNumber = -1;
        this.uniqueId = UniqueIdFactory.getInstance().nextId(Transaction.class);
    }

    @Override
    public Object getUID() {
        return uniqueId;
    }
}
