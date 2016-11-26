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
import io.realm.Realm;

/**
 * Activity used to enter information used to create (or update) a {@link Balance}.
 */
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
     * The UID of the {@link Balance} we're editing, if we're editing one.
     */
    private long editingUid = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_new_balance);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Check to see if we're editing, and get the UID of the Balance to load data from if we are.
        if (getIntent().hasExtra(BalanceFields.UNIQUE_ID))
            editingUid = getIntent().getLongExtra(BalanceFields.UNIQUE_ID, -1);

        initUi();
    }

    /**
     * Initialize the UI.
     */
    private void initUi() {
        if (editingUid != -1) {
            // We're editing an existing Balance, change the title to reflect this.
            setTitle(R.string.action_edit_balance);

            // Fill in current data.
            try (Realm realm = Realm.getDefaultInstance()) {
                Balance balance = realm.where(Balance.class).equalTo(BalanceFields.UNIQUE_ID, editingUid).findFirst();
                etName.setText(balance.name);
                etBaseAmount.setText(CurrencyUtils.longToCurrencyString(balance.baseBalance));
                etYellowLimit.setText(CurrencyUtils.longToCurrencyString(balance.yellowLimit));
                etRedLimit.setText(CurrencyUtils.longToCurrencyString(balance.redLimit));
            }
            // Don't allow editing the base amount.
            etBaseAmountLayout.setEnabled(false);
            etBaseAmount.setEnabled(false);
        } else {
            // We're making a new Balance.
            etBaseAmount.setText(Balances.getD().ZERO_AMOUNT_NO_SYMBOL);
            etYellowLimit.setText(CurrencyUtils.getStringFromEnUSString("50.00"));
            etRedLimit.setText(CurrencyUtils.getStringFromEnUSString("25.00"));
        }

        // Set all focus change listeners.
        View.OnFocusChangeListener formattingFocusChangeListener = Utils.getCurrencyFormattingFocusChangeListener();
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
        // Validate data which needs it, and (hopefully) obtain a Bundle containing that data.
        Bundle data = validateInputsAndBundle();
        if (data == null) return;

        // In this case, all data does have validation, so we only need to potentially add the UID if we're editing.
        if (editingUid != -1)
            data.putLong(BalanceFields.UNIQUE_ID, editingUid);

        // Set result and finish.
        setResult(RESULT_OK, new Intent().putExtras(data));
        finish();
    }

    /**
     * Validates any inputs which need it, and returns the data for those inputs in a Bundle.
     * @return A Bundle containing all data from validated inputs if all validations complete; null if any validations
     * fail. Note that any data which comes from inputs which lack validation checks will not be contained in the
     * returned Bundle.
     */
    private Bundle validateInputsAndBundle() {
        boolean valid = true;
        Bundle data = new Bundle();

        // Validate name.
        String s = etName.getText().toString().trim();
        if (s.isEmpty()) {
            etNameLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        // Add name to bundle.
        data.putString(BalanceFields.NAME, s);

        // Validate base amount.
        s = etBaseAmount.getText().toString().trim();
        if (s.isEmpty()) {
            etBaseAmountLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        // Add base amount to bundle.
        data.putLong(BalanceFields.BASE_BALANCE, CurrencyUtils.currencyStringToLong(s, 0));

        // Validate both limits for entry presence first.
        s = etYellowLimit.getText().toString().trim();
        String s2 = etRedLimit.getText().toString().trim();
        if (s.isEmpty()) {
            etYellowLimitLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (s2.isEmpty()) {
            etRedLimitLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        // Then validate them for correctness.
        if (!s.isEmpty() && !s2.isEmpty()) {
            long yLimit = CurrencyUtils.currencyStringToLong(etYellowLimit.getText().toString(), 0);
            long rLimit = CurrencyUtils.currencyStringToLong(etRedLimit.getText().toString(), 0);
            if (yLimit <= rLimit) {
                etYellowLimitLayout.setError(getString(R.string.error_yellow_limit));
                valid = false;
            }
            // Add both limits to bundle.
            data.putLong(BalanceFields.YELLOW_LIMIT, yLimit);
            data.putLong(BalanceFields.RED_LIMIT, rLimit);
        }

        // Return our data bundle if everything was valid; null otherwise.
        return valid ? data : null;
    }
}
