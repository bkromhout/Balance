package com.bkromhout.balances.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bkromhout.balances.R;
import com.bkromhout.balances.Utils;
import com.bkromhout.balances.adapters.CategoryStartsWithAdapter;
import com.bkromhout.balances.data.CurrencyUtils;
import com.bkromhout.balances.data.DateUtils;
import com.bkromhout.balances.data.models.Category;
import com.bkromhout.balances.data.models.CategoryFields;
import com.bkromhout.balances.data.models.Transaction;
import com.bkromhout.balances.data.models.TransactionFields;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.Calendar;

/**
 * Activity used to enter information used to create (or update) a {@link Transaction}.
 */
public class NewTransactionActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    // Keys.
    private static final String TIME_IN_MILLIS = "TIME_IN_MILLIS";

    // Views.
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.name_layout)
    TextInputLayout etNameLayout;
    @BindView(R.id.name)
    TextInputEditText etName;
    @BindView(R.id.category_layout)
    TextInputLayout etCategoryLayout;
    @BindView(R.id.category)
    AutoCompleteTextView actvCategory;
    @BindView(R.id.type)
    RadioGroup rgType;
    @BindView(R.id.date)
    TextView tvDate;
    @BindView(R.id.amount_layout)
    TextInputLayout etAmountLayout;
    @BindView(R.id.amount)
    TextInputEditText etAmount;
    @BindView(R.id.check_number)
    TextInputEditText etCheckNumber;
    @BindView(R.id.notes)
    TextInputEditText etNotes;

    /**
     * Instance of Realm.
     */
    private Realm realm;
    /**
     * The UID of the {@link Transaction} we're editing, if we're editing one.
     */
    private long editingUid = -1;
    /**
     * The timestamp to use for this transaction. We keep a global instance for the calendar so that we can maintain
     * granularity despite using a short date format for the text displayed to the user.
     */
    private Calendar timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_new_transaction);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();

        // Check to see if we're editing, and get the UID of the Transaction to load data from if we are.
        if (getIntent().hasExtra(TransactionFields.UNIQUE_ID))
            editingUid = getIntent().getLongExtra(TransactionFields.UNIQUE_ID, -1);

        // Restore the previously set timestamp.
        if (savedInstanceState != null && savedInstanceState.containsKey(TIME_IN_MILLIS)) {
            timestamp = Calendar.getInstance();
            timestamp.setTimeInMillis(savedInstanceState.getLong(TIME_IN_MILLIS));
        }

        initUi();
    }

    /**
     * Initialize the UI.
     */
    private void initUi() {
        if (editingUid != -1) {
            // We're editing an existing Transaction, change the title to reflect this.
            setTitle(R.string.action_edit_transaction);

            // Fill in current data.
            Transaction transaction = realm.where(Transaction.class).equalTo(TransactionFields.UNIQUE_ID,
                    editingUid).findFirst();
            etName.setText(transaction.name);
            actvCategory.setText(transaction.category.name);
            rgType.check(transaction.category.isCredit ? R.id.type_credit : R.id.type_debit);
            etAmount.setText(CurrencyUtils.longToCurrencyString(transaction.amount, false).replace("-", ""));
            if (transaction.checkNumber != -1) etCheckNumber.setText(String.valueOf(transaction.checkNumber));
            etNotes.setText(transaction.note);

            // Set our global timestamp to the Transaction's existing one, unless we set it from savedInstanceState.
            if (timestamp == null) {
                timestamp = Calendar.getInstance();
                timestamp.setTime(transaction.timestamp);
            }
        } else {
            // We're making a new Transaction, so we'll the current date and time as our timestamp, unless we already
            // set it from savedInstanceState.
            if (timestamp == null)
                timestamp = Calendar.getInstance();
        }
        // Set date TextView.
        tvDate.setText(DateUtils.parseDateToString(timestamp.getTime()));

        // Set up autocomplete for categories.
        RealmResults<Category> categories = realm.where(Category.class).findAll();
        actvCategory.setAdapter(new CategoryStartsWithAdapter(this, R.layout.category_item, categories));
        actvCategory.setOnItemClickListener((adapterView, view, position, id) -> {
            Category category = (Category) adapterView.getItemAtPosition(position);
            actvCategory.setText(category.name);
            rgType.check(category.isCredit ? R.id.type_credit : R.id.type_debit);
        });

        // Set all focus change listeners.
        View.OnFocusChangeListener formattingFocusChangeListener = Utils.getCurrencyFormattingFocusChangeListener();
        etAmount.setOnFocusChangeListener(formattingFocusChangeListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Utils.forceMenuIcons(menu, getClass().getSimpleName());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_transaction, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close Realm.
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save_transaction:
                saveTransaction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.date)
    void onDateClick() {
        // Open DatePickerDialog.
        DatePickerDialog dpd = DatePickerDialog.newInstance(this, timestamp.get(Calendar.YEAR),
                timestamp.get(Calendar.MONTH), timestamp.get(Calendar.DAY_OF_MONTH));
        dpd.dismissOnPause(true);
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        timestamp.set(year, monthOfYear, dayOfMonth);
        tvDate.setText(DateUtils.parseDateToString(timestamp.getTime()));
    }

    /**
     * Save a transaction's details.
     */
    private void saveTransaction() {
        // Validate data which needs it, and (hopefully) obtain a Bundle containing that data.
        Bundle data = validateInputsAndBundle();
        if (data == null) return;

        // Put other data into the Bundle.
        data.putLong(TransactionFields.TIMESTAMP, timestamp.getTimeInMillis());
        String s = etCheckNumber.getText().toString().trim();
        data.putInt(TransactionFields.CHECK_NUMBER, s.isEmpty() ? -1 : Integer.parseInt(s));
        data.putString(TransactionFields.NOTE, etNotes.getText().toString().trim());

        // Make sure we have a real Category created, and that its UID in the Bundle.
        createCategoryIfNeeded(data);

        // If editing, also put the Transaction UID into the Bundle.
        if (editingUid != -1)
            data.putLong(TransactionFields.UNIQUE_ID, editingUid);

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
        data.putString(TransactionFields.NAME, s);

        // Validate category.
        s = actvCategory.getText().toString().trim();
        boolean isCredit = rgType.getCheckedRadioButtonId() == R.id.type_credit;
        if (s.isEmpty()) {
            etCategoryLayout.setError(getString(R.string.error_required));
            valid = false;
        } else {
            // Try to find existing category.
            Category existingCategory = realm.where(Category.class)
                                             .equalTo(CategoryFields.NAME, s)
                                             .equalTo(CategoryFields.IS_CREDIT, isCredit)
                                             .findFirst();
            // If one exists, add its ID to the bundle, but use the field name through TransactionFields.
            if (existingCategory != null)
                data.putLong(TransactionFields.CATEGORY.UNIQUE_ID, existingCategory.uniqueId);
            // If one doesn't exist, we'll create it after validation.
        }

        // Validate amount.
        s = etAmount.getText().toString().trim();
        if (s.isEmpty()) {
            etAmountLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        long l = CurrencyUtils.currencyStringToLong(s, 0);
        if (l == 0) {
            // Don't allow zero as an amount, that's a useless transaction.
            etAmountLayout.setError(getString(R.string.error_invalid_amount));
            valid = false;
        }
        // If this is a debit, we need to multiply the amount by -1.
        if (!isCredit) l *= -1L;
        // Add amount to bundle.
        data.putLong(TransactionFields.AMOUNT, l);

        // Return our data bundle if everything was valid; null otherwise.
        return valid ? data : null;
    }

    /**
     * Creates a {@link Category} based off of the current inputs if there isn't already a UID for one in the given
     * Bundle.
     * @param data Bundle to check for a {@link Category} UID.
     */
    private void createCategoryIfNeeded(final Bundle data) {
        // If we already have a Category UID, we're good.
        if (data.containsKey(TransactionFields.CATEGORY.UNIQUE_ID))
            return;

        // Otherwise, we need to create a new Category, then add its UID to the Bundle.
        realm.executeTransaction(tRealm -> {
            Category newCategory = tRealm.copyToRealm(new Category(actvCategory.getText().toString().trim(),
                    rgType.getCheckedRadioButtonId() == R.id.type_credit));

            data.putLong(TransactionFields.CATEGORY.UNIQUE_ID, newCategory.uniqueId);
        });
    }
}
