package com.bkromhout.balances.enums;

import android.support.annotation.IdRes;
import com.bkromhout.balances.R;

/**
 * Represents the various possible fragments which can be hosted by the
 * {@link com.bkromhout.balances.activities.BalanceDetailsActivity}.
 */
public enum BalanceDetailsFrag {
    TRANSACTIONS(R.id.nav_transactions, 0),
    OVERVIEW(R.id.nav_overview, 1),
    SCHEDULED(R.id.nav_scheduled, 2);

    @IdRes
    private final int navId;
    private final int index;

    BalanceDetailsFrag(@IdRes final int navId, final int index) {
        this.navId = navId;
        this.index = index;
    }

    /**
     * Get the navigation ID for this fragment.
     * @return Navigation ID.
     */
    @IdRes
    public int getNavId() {
        return navId;
    }

    /**
     * Get the index of this fragment's item in the menu.
     * @return Menu item index.
     */
    public int getIndex() {
        return index;
    }

    public static BalanceDetailsFrag fromIndex(final int index) {
        for (BalanceDetailsFrag bdf : BalanceDetailsFrag.values())
            if (bdf.getIndex() == index) return bdf;
        throw new IllegalArgumentException("Invalid index.");
    }

    public static BalanceDetailsFrag fromNavId(@IdRes final int navId) {
        for (BalanceDetailsFrag bdf : BalanceDetailsFrag.values())
            if (bdf.getNavId() == navId) return bdf;
        throw new IllegalArgumentException("Invalid nav ID.");
    }
}
