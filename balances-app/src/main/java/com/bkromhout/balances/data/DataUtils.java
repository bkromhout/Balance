package com.bkromhout.balances.data;

import com.bkromhout.balances.enums.WarnLimitsResult;

/**
 * Utility methods related to data.
 */
public class DataUtils {
    /**
     * Checks to see if a string is {@code null} or empty.
     * @param s String to check.
     * @return True if string is {@code null} or empty, otherwise false.
     */
    public static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Validates a pair of warning limits.
     * @param yellowLimit The yellow (first) warning limit.
     * @param redLimit The red (second) warning limit.
     * @return The validation result.
     */
    public static WarnLimitsResult validateWarnLimits(long yellowLimit, long redLimit) {
        if (yellowLimit < 0 || redLimit < 0)
            return WarnLimitsResult.NEGATIVE;
        else if (yellowLimit >= redLimit)
            return WarnLimitsResult.OVERLAP;
        else
            return WarnLimitsResult.OK;
    }
}
