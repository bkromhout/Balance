package com.bkromhout.balances;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.bkromhout.balances.data.CurrencyUtils;
import timber.log.Timber;

import java.lang.reflect.Method;

/**
 * Utility methods.
 */
public class Utils {
    /**
     * Uses some clever trickery to make it so that menu items in the popup menu still show their icons. (Very hacky)
     * @param menu            Menu to force icons for.
     * @param classSimpleName Class name, used for potential logging.
     */
    public static void forceMenuIcons(Menu menu, String classSimpleName) {
        if (menu != null) {
            // Make sure all icons are tinted the correct color, including those in the overflow menu.
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).getIcon().setColorFilter(
                        ContextCompat.getColor(Balances.get(), R.color.textColorPrimary), PorterDuff.Mode.SRC_IN);
            // And use a bit of reflection to ensure we show icons even in the overflow menu.
            if (menu.getClass().equals(MenuBuilder.class)) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Timber.tag(classSimpleName);
                    Timber.e(e, "onMenuOpened...unable to set icons for overflow menu");
                }
            }
        }
    }

    /**
     * Get a tinted drawable.
     * @param drawableRes The drawable resource to use.
     * @param colorRes    The color resource to use.
     * @return Tinted drawable.
     */
    public static Drawable getTintedDrawable(@DrawableRes int drawableRes, @ColorRes int colorRes) {
        Drawable drawable = ContextCompat.getDrawable(Balances.get(), drawableRes);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(Balances.get(), colorRes));
        return drawable;
    }

    /**
     * Convenience method to take a long amount, create an appropriate string from it using the methods in {@link
     * com.bkromhout.balances.data.CurrencyUtils}, and then assign it to the given TextView and change the text color to
     * green or red depending on if the amount is positive or negative.
     * @param textView The TextView to set the resulting text and color to.
     * @param amount   The amount value to use.
     */
    public static void setAmountTextAndColor(final TextView textView, final long amount, final boolean includeSymbol) {
        String amountStr = CurrencyUtils.longToCurrencyString(amount, includeSymbol);
        textView.setText(amountStr);
        textView.setTextColor(amount < 0L ? Balances.getD().TEXT_COLOR_RED : Balances.getD().TEXT_COLOR_GREEN);
    }

    /**
     * Similar to {@link #setAmountTextAndColor(TextView, long, boolean)}, sets color based on yellow and red limits
     * instead of just the sign of the number. Also always includes the currency symbol.
     * @param textView    The TextView to set the resulting text and color to.
     * @param amount      The amount value to use.
     * @param yellowLimit The yellow limit value to compare against.
     * @param redLimit    The red limit value to compare against.
     */
    public static void setAmountTextAndColorWithLimits(final TextView textView, final long amount,
                                                       final long yellowLimit, final long redLimit) {
        String amountStr = CurrencyUtils.longToCurrencyString(amount, true);
        textView.setText(amountStr);
        textView.setTextColor(amount > yellowLimit ? Balances.getD().TEXT_COLOR_GREEN
                : (amount > redLimit ? Balances.getD().TEXT_COLOR_YELLOW
                : Balances.getD().TEXT_COLOR_RED));
    }

    /**
     * Create and return a new View.OnFocusChangeListener which is meant to be applied to an EditText which is used to
     * input currency amounts. The listener will auto-format the EditText's contents whenever it loses focus.
     * @return A new View.OnFocusChangeListener which formats the contents of a currency EditText whenever it loses
     * focus.
     */
    public static View.OnFocusChangeListener getCurrencyFormattingFocusChangeListener() {
        return (view, hasFocus) -> {
            EditText et = (EditText) view;
            if (!hasFocus && et.length() != 0) {
                String parsedAmount = CurrencyUtils.roundCurrency(et.getText().toString());
                et.setText(parsedAmount);
            }
        };
    }

    /**
     * Checks to see if a string is {@code null} or empty.
     * @param s String to check.
     * @return True if string is {@code null} or empty, otherwise false.
     */
    public static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
