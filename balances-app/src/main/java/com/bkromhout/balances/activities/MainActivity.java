package com.bkromhout.balances.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bkromhout.balances.C;
import com.bkromhout.balances.R;
import com.bkromhout.balances.Utils;
import com.bkromhout.balances.adapters.BalanceAdapter;
import com.bkromhout.balances.data.models.Balance;
import com.bkromhout.balances.data.models.BalanceFields;
import com.bkromhout.balances.events.ActionEvent;
import com.bkromhout.balances.events.BalanceClickEvent;
import com.bkromhout.balances.ui.Dialogs;
import com.bkromhout.rrvl.RealmRecyclerView;
import com.bkromhout.rrvl.SelectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * Main activity of the app. Shows a list of balances and provides functionality related to them.
 */
public class MainActivity extends AppCompatActivity implements ActionMode.Callback, SelectionChangeListener {
    // Request codes.
    private static final int RC_CREATE_BALANCE = 1;
    private static final int RC_EDIT_BALANCE = 2;

    // Views.
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler)
    RealmRecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fabNewBalance;
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
     * Action mode.
     */
    private static ActionMode actionMode;
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

        realm = Realm.getDefaultInstance();
        // Init the UI.
        initUi();

        if (savedInstanceState != null && savedInstanceState.getBoolean(C.IS_IN_ACTION_MODE)) {
            adapter.restoreInstanceState(savedInstanceState);
            startActionMode();
        }
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
        if (adapter != null) adapter.setSelectionChangeListener(this);
        RecyclerView realRv = recyclerView.getRecyclerView();
        realRv.addItemDecoration(new DividerItemDecoration(realRv.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Utils.forceMenuIcons(menu, getClass().getSimpleName());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save adapter state if we're in action mode.
        if (actionMode != null) {
            adapter.saveInstanceState(outState);
            outState.putBoolean(C.IS_IN_ACTION_MODE, true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        // Finish action mode so it doesn't leak.
        if (actionMode != null) actionMode.finish();
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
        if (actionMode != null) actionMode.finish();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.main_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Utils.forceMenuIcons(menu, getClass().getSimpleName());
        // Only show the "Edit" item if only one item is selected.
        menu.findItem(R.id.action_edit_balance).setVisible(adapter.getSelectedItemCount() <= 1);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.clearSelections();
        actionMode = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Open settings activity.
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                // Open about activity.
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Ignore if nothing is selected.
        if (adapter.getSelectedItemCount() == 0) return true;
        switch (item.getItemId()) {
            case R.id.action_edit_balance:
                List<Balance> selected = adapter.getSelectedRealmObjects();
                if (selected.size() != 1) return true; // Ignore if we somehow have more than one selected.
                Balance balance = selected.get(0);
                // Start NewBalanceActivity in edit mode by passing a Balance's UID.
                startActivityForResult(new Intent(this, NewBalanceActivity.class)
                        .putExtra(BalanceFields.UNIQUE_ID, balance.uniqueId), RC_EDIT_BALANCE);
                return true;
            case R.id.action_delete_balance:
                // Get pluralized strings.
                String title = getResources().getQuantityString(R.plurals.title_delete_balance,
                        adapter.getSelectedItemCount());
                String text = getResources().getQuantityString(R.plurals.prompt_delete_balance,
                        adapter.getSelectedItemCount());
                // Open confirmation dialog.
                Dialogs.simpleConfirmDialog(this, title, text, getString(R.string.delete), item.getItemId());
                return true;
            default:
                return false;
        }
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        startActivityForResult(new Intent(this, NewBalanceActivity.class), RC_CREATE_BALANCE);
    }

    /**
     * Called when a balance item is clicked or long-clicked.
     * @param event {@link BalanceClickEvent}.
     */
    @Subscribe
    public void onBalanceClickEvent(BalanceClickEvent event) {
        // If in action mode, select some things.
        if (actionMode != null) {
            if (event.getType() == BalanceClickEvent.Type.LONG)
                adapter.extendSelectionTo(event.getAdapterPosition());
            else
                adapter.toggleSelected(event.getAdapterPosition());
            return;
        }

        switch (event.getType()) {
            case NORMAL:
                // Open BalanceDetailsActivity for the clicked Balance.
                startActivity(new Intent(this, BalanceDetailsActivity.class)
                        .putExtra(BalanceFields.UNIQUE_ID, event.getUniqueId()));
                break;
            case LONG:
                adapter.toggleSelected(event.getAdapterPosition());
                startActionMode();
                break;
        }
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
                    // Persist changed data to selected balance.
                    updateBalance(data.getExtras());
                    break;
            }
        }
    }

    /**
     * Called to take some action.
     * @param event {@link ActionEvent}.
     */
    @Subscribe
    public void onActionEvent(ActionEvent event) {
        switch (event.getActionId()) {
            case R.id.action_delete_balance:
                // Delete selected balance items.
                final Long[] uidsToDelete = adapter.getSelectedItemUids();
                realm.executeTransactionAsync(bgRealm ->
                        bgRealm.where(Balance.class)
                               .in(BalanceFields.UNIQUE_ID, uidsToDelete)
                               .findAll()
                               .deleteAllFromRealm());
                break;
        }
        if (actionMode != null) actionMode.finish();
    }

    /**
     * Called when the item selection changes so that we can update the action mode menu items if needed.
     */
    @Override
    public void itemSelectionChanged() {
        // Refresh the action mode menu.
        if (actionMode != null) actionMode.invalidate();
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
            bgRealm.copyToRealm(newBalance);
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

            adapter.notifyDataSetChanged();
        });
        if (actionMode != null) actionMode.finish();
    }

    /**
     * Starts the action mode (if it hasn't been already).
     */
    private void startActionMode() {
        if (actionMode == null) actionMode = startSupportActionMode(this);
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
            adapter.notifyDataSetChanged();
        }
    }
}
