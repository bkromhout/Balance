package com.bkromhout.balances.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bkromhout.balances.R;
import com.bkromhout.balances.Utils;
import com.bkromhout.balances.data.models.Balance;
import com.bkromhout.balances.data.models.BalanceFields;
import com.bkromhout.balances.enums.BalanceDetailsFrag;
import com.bkromhout.balances.fragments.TransactionsFragment;
import io.realm.Realm;
import io.realm.RealmChangeListener;

import static com.bkromhout.balances.enums.BalanceDetailsFrag.TRANSACTIONS;

/**
 * Activity which hosts a few fragments and implements the bottom navigation pattern.
 */
public class BalanceDetailsActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener {
    // Keys.
    private static final String CURR_FRAG = "CURR_FRAG";

    // Views.
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.content)
    RelativeLayout content;
    @BindView(R.id.balance_amount)
    TextView tvBalanceAmt;
    @BindView(R.id.frag_cont)
    FrameLayout fragCont;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNav;

    /**
     * Instance of Realm.
     */
    private Realm realm;
    /**
     * {@link Balance} currently shown in this activity.
     */
    private Balance balance;
    /**
     * What fragment is currently being shown.
     */
    private BalanceDetailsFrag currFrag = TRANSACTIONS;
    /**
     * Realm change listener which takes care of updating certain content whenever there is a change to {@link
     * #balance}.
     */
    private final RealmChangeListener<Balance> balanceListener = this::updateBalanceDetails;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();
        balance = realm.where(Balance.class)
                       .equalTo(BalanceFields.UNIQUE_ID, getIntent().getLongExtra(BalanceFields.UNIQUE_ID, -1))
                       .findFirst();

        // Restore the current fragment state if needed.
        if (savedInstanceState != null && savedInstanceState.containsKey(CURR_FRAG))
            currFrag = BalanceDetailsFrag.fromIndex(savedInstanceState.getInt(CURR_FRAG));

        // Init the UI.
        initUi();
    }

    /**
     * Initialize the UI.
     */
    private void initUi() {
        balance.addChangeListener(balanceListener);
        updateBalanceDetails(balance);

        bottomNav.setOnNavigationItemSelectedListener(this);
        bottomNav.findViewById(currFrag.getNavId()).performClick();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Utils.forceMenuIcons(menu, getClass().getSimpleName());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.balance_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURR_FRAG, currFrag.getIndex());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove listener.
        balance.removeChangeListener(balanceListener);
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
            case R.id.action_settings:
                // Open settings activity.
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                // TODO Open about activity.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        currFrag = BalanceDetailsFrag.fromNavId(item.getItemId());

        Fragment fragment = null;
        switch (currFrag) {
            case TRANSACTIONS:
                // Show TransactionsFragment.
                fragment = TransactionsFragment.newInstance(balance.uniqueId);
                break;
            case OVERVIEW:
                // TODO Show OverviewFragment.
                break;
            case SCHEDULED:
                // TODO Show ScheduledFragment.
                break;
        }

        if (fragment != null) {
            FragmentManager fragMan = getSupportFragmentManager();
            fragMan.beginTransaction().replace(R.id.frag_cont, fragment).commit();
        }

        return true;
    }

    /**
     * Update the title and the balance amount TextView.
     * @param balance {@link Balance}.
     */
    private void updateBalanceDetails(Balance balance) {
        setTitle(balance.name);
        Utils.setAmountTextAndColorWithLimits(tvBalanceAmt, balance.getTotalBalance(), balance.yellowLimit,
                balance.redLimit);
    }
}
