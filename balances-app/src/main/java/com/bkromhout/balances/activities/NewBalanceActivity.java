package com.bkromhout.balances.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bkromhout.balances.Balances;
import com.bkromhout.balances.R;
import com.bkromhout.balances.Utils;
import com.bkromhout.balances.data.CurrencyUtils;
import com.bkromhout.balances.data.models.Balance;
import com.bkromhout.balances.data.models.BalanceFields;

public class NewBalanceActivity extends AppCompatActivity {

    // Views.
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.content)
    ConstraintLayout content;
    @BindView(R.id.balance_name_layout)
    TextInputLayout etNameLayout;
    @BindView(R.id.balance_name)
    TextInputEditText etName;
    @BindView(R.id.base_amount_layout)
    TextInputLayout etBaseAmountLayout;
    @BindView(R.id.base_amount)
    TextInputEditText etBaseAmount;
    @BindView(R.id.yellow_limit_layout)
    TextInputLayout etYellowLimitLayout;
    @BindView(R.id.yellow_limit)
    TextInputEditText etYellowLimit;
    @BindView(R.id.red_limit_layout)
    TextInputLayout etRedLimitLayout;
    @BindView(R.id.red_limit)
    TextInputEditText etRedLimit;

    /**
     * Whether we're editing an existing balance or not.
     */
    private boolean isEditing = false;
    /**
     * The UID of the {@link Balance} we're editing, if we're editing one.
     */
    private long editingUid = -1;
    /**
     * Focus change listener applied to all currency input fields to auto-format their contents whenever they lose
     * focus.
     */
    private View.OnFocusChangeListener formattingFocusChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_new_balance);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Check to see if we're editing, and get the UID of the Balance to load data from if we are.
        if (getIntent().hasExtra(BalanceFields.UNIQUE_ID)) {
            isEditing = true;
            editingUid = getIntent().getLongExtra(BalanceFields.UNIQUE_ID, -1);
        }
    }

    /**
     * Initialize the UI.
     */
    private void initUi() {
        if (isEditing) {
            // TODO
            // Don't allow editing the base amount.
            etBaseAmount.setEnabled(false);
        } else {
            etBaseAmount.setText(Balances.getD().ZERO_AMOUNT_NO_SYMBOL);
            etYellowLimit.setText(CurrencyUtils.getStringFromEnUSString("50.00"));
            etRedLimit.setText(CurrencyUtils.getStringFromEnUSString("25.00"));
        }

        // Set all focus change listeners.
        formattingFocusChangeListener = Utils.getCurrencyFormattingFocusChangeListener();
        etBaseAmount.setOnFocusChangeListener(formattingFocusChangeListener);
        etYellowLimit.setOnFocusChangeListener(formattingFocusChangeListener);
        etRedLimit.setOnFocusChangeListener(formattingFocusChangeListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Utils.forceMenuIcons(menu, getClass().getSimpleName());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_balance, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save_balance:
                saveBalance();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Save a balance's details.
     */
    private void saveBalance() {
        if (!verifyInputs()) return;

        // Put result data into a bundle.
        Bundle b = new Bundle();
        b.putString(BalanceFields.NAME, etName.getText().toString());
        b.putLong(BalanceFields.BASE_BALANCE, CurrencyUtils.currencyStringToLong(etBaseAmount.getText().toString(), 0));
        b.putLong(BalanceFields.YELLOW_LIMIT,
                CurrencyUtils.currencyStringToLong(etYellowLimit.getText().toString(), 0));
        b.putLong(BalanceFields.RED_LIMIT, CurrencyUtils.currencyStringToLong(etRedLimit.getText().toString(), 0));

        // Set result and finish.
        setResult(RESULT_OK, new Intent().putExtras(b));
        finish();
    }

    /**
     * Verify the input values.
     * @return True if all inputs are valid, otherwise false.
     */
    private boolean verifyInputs() {
        boolean valid = true;

        // Validate name.
        if (etName.length() == 0) {
            etNameLayout.setError(getString(R.string.error_required));
            valid = false;
        }

        // Validate amount.
        if (etBaseAmount.length() == 0) {
            etBaseAmountLayout.setError(getString(R.string.error_required));
            valid = false;
        }

        // Validate both limits for entry presence first.
        if (etYellowLimit.length() == 0) {
            etYellowLimitLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (etRedLimit.length() == 0) {
            etRedLimitLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        // Then validate them for correctness.
        if (etYellowLimit.length() != 0 && etRedLimit.length() != 0) {
            long yLimit = CurrencyUtils.currencyStringToLong(etYellowLimit.getText().toString(), 0);
            long rLimit = CurrencyUtils.currencyStringToLong(etRedLimit.getText().toString(), 0);
            if (yLimit <= rLimit) {
                etYellowLimitLayout.setError(getString(R.string.error_yellow_limit));
                valid = false;
            }
        }

        return valid;
    }
}
