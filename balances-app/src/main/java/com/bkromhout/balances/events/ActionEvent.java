package com.bkromhout.balances.events;

import android.support.annotation.IdRes;

/**
 * General-purpose event fired from many places to trigger something.
 */
public class ActionEvent {
    /**
     * The action to take.
     */
    private final int actionId;
    /**
     * Extra data to help take the action.
     */
    private final Object data;

    /**
     * Create a new {@link ActionEvent}.
     * @param actionId Action to take.
     * @param data     Extra data.
     */
    public ActionEvent(@IdRes int actionId, Object data) {
        this.actionId = actionId;
        this.data = data;
    }

    @IdRes
    public int getActionId() {
        return this.actionId;
    }

    public Object getData() {
        return this.data;
    }
}
