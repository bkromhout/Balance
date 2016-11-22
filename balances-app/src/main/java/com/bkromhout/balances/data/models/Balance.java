package com.bkromhout.balances.data.models;

import com.bkromhout.balances.Utils;
import com.bkromhout.balances.data.DataUtils;
import com.bkromhout.balances.data.UniqueIdFactory;
import com.bkromhout.balances.enums.WarnLimitsResult;
import com.bkromhout.rrvl.UIDModel;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Represents a balance in Realm.
 * <p>
 * A balance is a collection of {@link Transaction}s.
 */
public class Balance extends RealmObject implements UIDModel {
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
     * Base amount in the balance. This will be added to the sum of the transactions' amounts when getting this {@link
     * Balance}'s total balance.
     */
    public long baseBalance;
    /**
     * The first warning limit for the balance.
     */
    public long yellowLimit;
    /**
     * The second warning limit for the balance.
     */
    public long redLimit;
    /**
     * List of transactions associated with this balance.
     */
    public RealmList<Transaction> transactions;

    // Empty constructor for Realm.
    public Balance() {
    }

    /**
     * Create a new {@link Balance}.
     * @param name Name of the balance.
     * @param baseBalance Base amount in the balance.
     * @param yellowLimit First warning limit for the balance.
     * @param redLimit Second warning limit for the balance.
     */
    public Balance(String name, long baseBalance, long yellowLimit, long redLimit) {
        if (Utils.nullOrEmpty(name))
            throw new IllegalArgumentException("Name must not be null or empty.");
        if (DataUtils.validateWarnLimits(yellowLimit, redLimit) != WarnLimitsResult.OK)
            throw new IllegalArgumentException("Invalid limits.");

        this.name = name;
        this.baseBalance = baseBalance;
        this.yellowLimit = yellowLimit;
        this.redLimit = redLimit;
        this.uniqueId = UniqueIdFactory.getInstance().nextId(Balance.class);
    }

    /**
     * Get the total balance.
     * @return Total balance.
     */
    public long getTotalBalance() {
        return baseBalance + transactions.sum(TransactionFields.AMOUNT).longValue();
    }

    @Override
    public Object getUID() {
        return uniqueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Balance balance = (Balance) o;
        return uniqueId == balance.uniqueId;
    }

    @Override
    public int hashCode() {
        return (int) (uniqueId ^ (uniqueId >>> 32));
    }
}
