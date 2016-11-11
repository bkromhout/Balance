package com.bkromhout.balances.data;

import timber.log.Timber;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Class used to help with Date to/from String conversions.
 */
public class DateUtils {
    private static final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

    /**
     * Takes a string and returns a date object.
     * @param date A short date string.
     * @return Date object.
     */
    public static Date parseStringToDate(String date) {
        Date d = null;
        try {
            d = df.parse(date);
        } catch (ParseException e) {
            Timber.e(e);
        }
        return d;
    }

    /**
     * Takes a date and returns a short date string.
     * @param date Date object to parse.
     * @return short date string.
     */
    public static String parseDateToString(Date date) {
        return df.format(date);
    }

    /**
     * Takes a long date (AKA time in milliseconds) and returns a short date string.
     * @param date Time in milliseconds (long).
     * @return short date string.
     */
    public static String parseLongDateToString(Long date) {
        return df.format(new Date(date));
    }
}
