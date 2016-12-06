package com.bkromhout.balances.events;

/**
 * Event fired to trigger widget updates.
 */
public class UpdateWidgetsEvent {
    private final long balanceUid;
    private final boolean isDeleted;

    /**
     * Create a new {@link UpdateWidgetsEvent}.
     * @param balanceUid {@link com.bkromhout.balances.data.models.Balance} unique ID.
     * @param isDeleted Whether the {@link com.bkromhout.balances.data.models.Balance} was deleted.
     */
    public UpdateWidgetsEvent(final long balanceUid, final boolean isDeleted) {
        this.balanceUid = balanceUid;
        this.isDeleted = isDeleted;
    }

    /**
     * Get the unique ID of the {@link com.bkromhout.balances.data.models.Balance} which we wish to update widgets for.
     * @return {@link com.bkromhout.balances.data.models.Balance} unique ID.
     */
    public long getBalanceUid() {
        return balanceUid;
    }

    /**
     * Whether or not the provided unique ID is for a {@link com.bkromhout.balances.data.models.Balance} which has been
     * deleted (in which case, we would invalidate any associated widgets).
     * @return True if the {@link com.bkromhout.balances.data.models.Balance} whose unique ID was given has been
     * deleted; false otherwise.
     */
    public boolean isDeleted() {
        return isDeleted;
    }
}
