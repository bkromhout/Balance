package com.bkromhout.balances.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bkromhout.balances.R;
import com.bkromhout.balances.Utils;
import com.bkromhout.balances.adapters.BalanceAdapter;
import com.bkromhout.balances.data.models.Balance;
import com.bkromhout.balances.data.models.BalanceFields;
import com.bkromhout.rrvl.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Main activity of the app. Shows a list of balances and provides functionality related to them.
 */
public class MainActivity extends AppCompatActivity {
    // Request codes.
    private static final int RC_CREATE_BALANCE = 1;
    private static final int RC_EDIT_BALANCE = 2;

    // Views.
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    @BindView(R.id.recycler)
    RealmRecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fabNewBalance;
    @BindView(R.id.alt_view)
    FrameLayout altView;
    @BindView(R.id.loading_balances)
    TextView tvLoadingResults;
    @BindView(R.id.no_balances)
    TextView tvNoResults;

    /**
     * Instance of Realm.
     */
    private Realm realm;
    /**
     * {@link Balance}s currently shown in the recycler view.
     */
    private RealmResults<Balance> balances;
    /**
     * Adapter for the RealmRecyclerView.
     */
    private BalanceAdapter adapter;
    /**
     * Realm change listener which takes care of toggling view visibility when {@link #balances} changes from empty to
     * non-empty (and vice-versa).
     */
    private final RealmChangeListener<RealmResults<Balance>> emptyListener = results ->
            toggleEmptyState(results.isLoaded(), results.isEmpty());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // Init the UI.
        initUi();
    }

    /**
     * Initialize the UI.
     */
    private void initUi() {
        balances = realm.where(Balance.class)
                        .findAllSortedAsync(BalanceFields.NAME);
        balances.addChangeListener(emptyListener);
        toggleEmptyState(balances.isLoaded(), balances.isEmpty());
        adapter = makeAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Utils.forceMenuIcons(menu, getClass().getSimpleName());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close adapter.
        if (adapter != null) adapter.close();
        // Remove listener.
        balances.removeChangeListener(emptyListener);
        // Close Realm.
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        startActivityForResult(new Intent(this, NewBalanceActivity.class), RC_CREATE_BALANCE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_CREATE_BALANCE:
                    // Save a new Balance.
                    saveNewBalance(data.getExtras());
                    break;
                case RC_EDIT_BALANCE:
                    // TODO
                    break;
            }
        }
    }

    /**
     * Create and save a new {@link Balance}.
     * @param data Data to use to create the new {@link Balance}.
     */
    private void saveNewBalance(final Bundle data) {
        realm.executeTransactionAsync(bgRealm -> {
            Balance newBalance = new Balance(data.getString(BalanceFields.NAME),
                    data.getLong(BalanceFields.BASE_BALANCE, 0L), data.getLong(BalanceFields.YELLOW_LIMIT, 5000L),
                    data.getLong(BalanceFields.RED_LIMIT, 2500L));
            realm.copyToRealm(newBalance);
        });
    }

    /**
     * Update an existing {@link Balance}.
     * @param data Data to use to find and update a {@link Balance}.
     */
    private void updateBalance(final Bundle data) {
        realm.executeTransaction(bgRealm -> {
            Balance balance = bgRealm.where(Balance.class).equalTo(BalanceFields.UNIQUE_ID,
                    data.getLong(BalanceFields.UNIQUE_ID)).findFirst();
            if (balance == null) throw new IllegalArgumentException("Invalid Balance ID");

            balance.name = data.getString(BalanceFields.NAME);
            balance.yellowLimit = data.getLong(BalanceFields.YELLOW_LIMIT, 5000L);
            balance.redLimit = data.getLong(BalanceFields.RED_LIMIT, 2500L);
        });
    }

    /**
     * Create a new {@link BalanceAdapter} based on the current view options and return it.
     * @return New {@link BalanceAdapter}, or null if {@link #balances} is null or invalid.
     */
    private BalanceAdapter makeAdapter() {
        if (balances == null || !balances.isValid()) return null;
        return new BalanceAdapter(this, balances);
    }

    /**
     * Changes the visibility of UI elements so that the empty view is shown/hidden.
     * @param isLoaded Whether the results have finished loading or not.
     * @param isEmpty  Whether there are any results.
     */
    private void toggleEmptyState(boolean isLoaded, boolean isEmpty) {
        if (!isLoaded) {
            // Not loaded yet.
            recyclerView.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.GONE);
            tvLoadingResults.setVisibility(View.VISIBLE);
        } else if (isEmpty) {
            // Loaded, but empty.
            recyclerView.setVisibility(View.GONE);
            tvLoadingResults.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
        } else {
            // Loaded with results.
            tvNoResults.setVisibility(View.GONE);
            tvLoadingResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
