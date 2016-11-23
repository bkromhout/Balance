package com.bkromhout.balances.enums;

/**
 * Used by {@link com.bkromhout.balances.data.DataUtils#validateWarnLimits(long, long)} to inform the caller of the
 * validity of a pair of warning limits.
 */
public enum WarnLimitsResult {
    /**
     * Limits are valid.
     */
    OK,
    /**
     * Limits overlap (the yellow limit is less than or equal to the red limit).
     */
    OVERLAP,
    /**
     * One or both of the limits are less than 0.
     */
    NEGATIVE;
}
