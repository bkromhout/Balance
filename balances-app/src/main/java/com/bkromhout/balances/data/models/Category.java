package com.bkromhout.balances.data.models;

import com.bkromhout.balances.data.UniqueIdFactory;
import com.bkromhout.rrvl.UIDModel;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * A category for {@link Transaction}s.
 */
public class Category extends RealmObject implements UIDModel {
    /**
     * A unique long value.
     */
    @PrimaryKey
    public long uniqueId;
    /**
     * The name of the category.
     */
    @Index
    public String name;
    /**
     * If true, transactions in this category add to a balance. If false, they subtract from it.
     */
    public boolean isCredit;

    // Empty constructor for Realm.
    public Category(){
    }

    /**
     * Create a new {@link Category}.
     * @param name Name of the category.
     * @param isCredit Whether transactions in this category add to a balance or not.
     */
    public Category(String name, boolean isCredit) {
        this.name = name;
        this.isCredit = isCredit;
        this.uniqueId = UniqueIdFactory.getInstance().nextId(Category.class);
    }

    @Override
    public Object getUID() {
        return uniqueId;
    }
}
