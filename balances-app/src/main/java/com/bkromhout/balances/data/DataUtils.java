package com.bkromhout.balances.data;

import com.bkromhout.balances.enums.WarnLimitsResult;

/**
 * Utility methods related to data.
 */
public class DataUtils {
    /**
     * Validates a pair of warning limits.
     * @param yellowLimit The yellow (first) warning limit.
     * @param redLimit The red (second) warning limit.
     * @return The validation result.
     */
    public static WarnLimitsResult validateWarnLimits(long yellowLimit, long redLimit) {
        if (yellowLimit < 0 || redLimit < 0)
            return WarnLimitsResult.NEGATIVE;
        else if (yellowLimit <= redLimit)
            return WarnLimitsResult.OVERLAP;
        else
            return WarnLimitsResult.OK;
    }
}
