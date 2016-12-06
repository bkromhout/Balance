package com.bkromhout.balances.activities;

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
import com.bkromhout.balances.data.models.*;
import com.bkromhout.balances.events.UpdateWidgetsEvent;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import io.realm.Realm;
import io.realm.RealmResults;
import org.greenrobot.eventbus.EventBus;

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
     * {@link Balance} which will (or does, if we're editing) own this transaction.
     */
    private Balance owningBalance;
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

        // Get the Balance which will (or does) own this transaction.
        owningBalance = realm.where(Balance.class)
                             .equalTo(BalanceFields.UNIQUE_ID, getIntent().getLongExtra(BalanceFields.UNIQUE_ID, -1))
                             .findFirst();
        if (owningBalance == null) finish();

        // Check to see if we're editing, and get the UID of the Transaction to load data from if we are.
        if (getIntent().hasExtra(BalanceFields.TRANSACTIONS.UNIQUE_ID))
            editingUid = getIntent().getLongExtra(BalanceFields.TRANSACTIONS.UNIQUE_ID, -1);

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
                persistTransaction();
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
     * Save or update a transaction's details, as long as user inputs are valid.
     */
    private void persistTransaction() {
        // Validate data which needs it. If any aren't valid, stop now.
        if (!validateInputs()) return;

        // Everything was valid, so either save a new Transaction, or update an existing one.
        saveOrUpdateTransaction();

        // Set result and finish.
        setResult(RESULT_OK);
        finish();
    }

    /**
     * Validates all inputs which need it, and returns the aggregate result.
     * @return True if everything was valid, false if anything was invalid.
     */
    private boolean validateInputs() {
        boolean valid = true;

        // Validate name.
        String s = etName.getText().toString().trim();
        if (s.isEmpty()) {
            etNameLayout.setError(getString(R.string.error_required));
            valid = false;
        }

        // Validate category.
        s = actvCategory.getText().toString().trim();
        if (s.isEmpty()) {
            etCategoryLayout.setError(getString(R.string.error_required));
            valid = false;
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

        return valid;
    }

    /**
     * Persist our data to either an existing or new {@link Transaction}. If we have to create a new {@link
     * Transaction}, it will be added to {@link #owningBalance}.
     */
    private void saveOrUpdateTransaction() {
        // Prepare required data.
        String name = etName.getText().toString().trim();
        Category category = getOrMakeCategory();
        long amount = CurrencyUtils.currencyStringToLong(etAmount.getText().toString().trim(), 0);
        if (!category.isCredit) amount *= -1L;

        realm.beginTransaction();
        // Try to get an existing transaction. If it ends up being null, we'll create a new one.
        Transaction transaction = realm.where(Transaction.class)
                                       .equalTo(TransactionFields.UNIQUE_ID, editingUid)
                                       .findFirst();
        if (transaction == null) {
            // We're creating; make using required data, copy to Realm, then add to owning Balance.
            transaction = realm.copyToRealm(new Transaction(name, category, amount, timestamp.getTime()));
            owningBalance.transactions.add(transaction);
        } else {
            // We're editing; update required data.
            transaction.name = name;
            transaction.category = category;
            transaction.amount = amount;
            transaction.timestamp = timestamp.getTime();
        }

        // Update optional data.
        String s = etCheckNumber.getText().toString().trim();
        transaction.checkNumber = s.isEmpty() ? -1 : Integer.parseInt(s);
        transaction.note = etNotes.getText().toString().trim();

        // Persist changes.
        realm.commitTransaction();
        // Trigger widget updates.
        EventBus.getDefault().post(new UpdateWidgetsEvent(owningBalance.uniqueId, false));
    }

    /**
     * Tries to get an existing {@link Category} based on the current category string and transaction type. If
     * successful, returns that, otherwise creates a new {@link Category} using that information and returns it.
     * @return {@link Category} for this transaction.
     */
    private Category getOrMakeCategory() {
        String categoryName = actvCategory.getText().toString().trim();
        boolean isCredit = rgType.getCheckedRadioButtonId() == R.id.type_credit;

        // Try to find an existing category.
        Category category = realm.where(Category.class)
                                 .equalTo(CategoryFields.NAME, categoryName)
                                 .equalTo(CategoryFields.IS_CREDIT, isCredit)
                                 .findFirst();
        // If we couldn't find one, we'll create one instead.
        if (category == null) {
            realm.beginTransaction();
            category = realm.copyToRealm(new Category(categoryName, isCredit));
            realm.commitTransaction();
        }

        return category;
    }
}
