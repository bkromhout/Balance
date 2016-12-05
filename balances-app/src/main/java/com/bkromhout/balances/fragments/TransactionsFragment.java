package com.bkromhout.balances.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bkromhout.balances.C;
import com.bkromhout.balances.R;
import com.bkromhout.balances.Utils;
import com.bkromhout.balances.activities.NewTransactionActivity;
import com.bkromhout.balances.adapters.TransactionAdapter;
import com.bkromhout.balances.data.models.*;
import com.bkromhout.balances.events.ActionEvent;
import com.bkromhout.balances.events.TransactionClickEvent;
import com.bkromhout.balances.ui.Dialogs;
import com.bkromhout.rrvl.RealmRecyclerView;
import com.bkromhout.rrvl.SelectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionsFragment extends Fragment implements ActionMode.Callback, SelectionChangeListener {
    // Request codes.
    private static final int RC_CREATE_TRANSACTION = 3;
    private static final int RC_EDIT_TRANSACTION = 4;

    // Views.
    @BindView(R.id.recycler)
    RealmRecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fabNewTransaction;
    @BindView(R.id.loading_transactions)
    TextView tvLoadingResults;
    @BindView(R.id.no_transactions)
    TextView tvNoResults;

    /**
     * Instance of Realm.
     */
    private Realm realm;
    /**
     * {@link Transaction}s currently shown in the recyclerview.
     */
    private RealmResults<Transaction> transactions;
    /**
     * Adapter for the RealmRecyclerView.
     */
    private TransactionAdapter adapter;
    /**
     * Action mode.
     */
    private static ActionMode actionMode;
    /**
     * Realm change listener which takes care of toggling view visibility when {@link #transactions} changes from empty
     * to non-empty (and vice-versa).
     */
    private final RealmChangeListener<RealmResults<Transaction>> emptyListener = results ->
            toggleEmptyState(results.isLoaded(), results.isEmpty());

    public TransactionsFragment() {
        // Required empty public constructor
    }

    public static TransactionsFragment newInstance(final long balanceUid) {
        TransactionsFragment fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        args.putLong(BalanceFields.UNIQUE_ID, balanceUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment, then bind and set up views.
        View root = inflater.inflate(R.layout.fragment_transactions, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        realm = Realm.getDefaultInstance();
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
        Balance balance = realm.where(Balance.class)
                               .equalTo(BalanceFields.UNIQUE_ID, getArguments().getLong(BalanceFields.UNIQUE_ID))
                               .findFirst();
        transactions = balance.transactions.sort(TransactionFields.TIMESTAMP, Sort.DESCENDING);
        transactions.addChangeListener(emptyListener);
        toggleEmptyState(transactions.isLoaded(), transactions.isEmpty());
        adapter = makeAdapter();
        if (adapter != null) adapter.setSelectionChangeListener(this);
        RecyclerView realRv = recyclerView.getRecyclerView();
        realRv.addItemDecoration(new DividerItemDecoration(realRv.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Utils.forceMenuIcons(menu, getClass().getSimpleName());
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.transactions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save adapter state if we're in action mode.
        if (actionMode != null) {
            adapter.saveInstanceState(outState);
            outState.putBoolean(C.IS_IN_ACTION_MODE, true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        // Finish action mode so it doesn't leak.
        if (actionMode != null) actionMode.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Close adapter.
        if (adapter != null) adapter.close();
        // Remove listener.
        transactions.removeChangeListener(emptyListener);
        // Close Realm.
        if (realm != null) {
            realm.close();
            realm = null;
        }
        if (actionMode != null) actionMode.finish();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.transactions_action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Utils.forceMenuIcons(menu, getClass().getSimpleName());
        // Only show the "Edit" item if only one item is selected.
        menu.findItem(R.id.action_edit_transaction).setVisible(adapter.getSelectedItemCount() <= 1);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Ignore if nothing is selected.
        if (adapter.getSelectedItemCount() == 0) return true;
        switch (item.getItemId()) {
            case R.id.action_edit_transaction:
                List<Transaction> selected = adapter.getSelectedRealmObjects();
                if (selected.size() != 1) return true; // Ignore if we somehow have more than one selected.
                Transaction transaction = selected.get(0);
                // Start NewTransactionActivity in edit mode by passing a Balance's UID.
                startActivityForResult(new Intent(getActivity(), NewTransactionActivity.class)
                        .putExtra(BalanceFields.UNIQUE_ID, transaction.uniqueId), RC_EDIT_TRANSACTION);
                return true;
            case R.id.action_delete_transaction:
                // Get pluralized strings.
                String title = getResources().getQuantityString(R.plurals.title_delete_transaction,
                        adapter.getSelectedItemCount());
                String text = getResources().getQuantityString(R.plurals.prompt_delete_transaction,
                        adapter.getSelectedItemCount());
                // Open confirmation dialog.
                Dialogs.simpleConfirmDialog(getActivity(), title, text, getString(R.string.delete), item.getItemId());
                return true;
            default:
                return false;
        }
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        startActivityForResult(new Intent(getActivity(), NewTransactionActivity.class), RC_CREATE_TRANSACTION);
    }

    /**
     * Called when a transaction item is clicked or long-clicked.
     * @param event {@link TransactionClickEvent}.
     */
    @Subscribe
    public void onTransactionClickEvent(TransactionClickEvent event) {
        // If in action mode, select some things.
        if (actionMode != null) {
            if (event.getType() == TransactionClickEvent.Type.LONG)
                adapter.extendSelectionTo(event.getAdapterPosition());
            else
                adapter.toggleSelected(event.getAdapterPosition());
            return;
        }

        switch (event.getType()) {
            case NORMAL:
                // Open TransactionDetailsActivity for the clicked Transaction.
//                startActivity(new Intent(this, TransactionDetailsActivity.class)
//                        .putExtra(TransactionFields.UNIQUE_ID, event.getUniqueId()));
                break;
            case LONG:
                adapter.toggleSelected(event.getAdapterPosition());
                startActionMode();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_CREATE_TRANSACTION:
                    // Save a new Balance.
                    saveNewTransaction(data.getExtras());
                    break;
                case RC_EDIT_TRANSACTION:
                    // Persist changed data to selected balance.
                    updateTransaction(data.getExtras());
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
            case R.id.action_delete_transaction:
                // Delete selected transaction items.
                final Long[] uidsToDelete = adapter.getSelectedItemUids();
                realm.executeTransactionAsync(bgRealm ->
                        bgRealm.where(Transaction.class)
                               .in(TransactionFields.UNIQUE_ID, uidsToDelete)
                               .findAll()
                               .deleteAllFromRealm());
                break;
        }
        if (actionMode != null) actionMode.finish();
    }

    @Override
    public void itemSelectionChanged() {
        // Refresh the action mode menu.
        if (actionMode != null) actionMode.invalidate();
    }

    /**
     * Create and save a new {@link Transaction}.
     * @param data Data to use to create the new {@link Transaction}.
     */
    private void saveNewTransaction(final Bundle data) {
        realm.executeTransactionAsync(bgRealm -> {
            // Find Category.
            Category category = bgRealm.where(Category.class).equalTo(CategoryFields.UNIQUE_ID,
                    data.getLong(TransactionFields.CATEGORY.UNIQUE_ID)).findFirst();

            // Create new Transaction with required data.
            Transaction newTransaction = new Transaction(data.getString(TransactionFields.NAME), category,
                    data.getLong(TransactionFields.AMOUNT), new Date(data.getLong(TransactionFields.TIMESTAMP)));

            // Add other data.
            newTransaction.checkNumber = data.getInt(TransactionFields.CHECK_NUMBER);
            newTransaction.note = data.getString(TransactionFields.NOTE);

            // Add Transaction to the list for the currently shown Balance. This will also copy it to Realm.
            Balance balance = bgRealm.where(Balance.class)
                                     .equalTo(BalanceFields.UNIQUE_ID, getArguments().getLong(BalanceFields.UNIQUE_ID))
                                     .findFirst();
            balance.transactions.add(newTransaction);
        });
    }

    /**
     * Update an existing {@link Transaction}.
     * @param data Data to use to find and update a {@link Transaction}.
     */
    private void updateTransaction(final Bundle data) {
        realm.executeTransaction(bgRealm -> {
            // Find Transaction.
            Transaction transaction = bgRealm.where(Transaction.class).equalTo(TransactionFields.UNIQUE_ID,
                    data.getLong(TransactionFields.UNIQUE_ID)).findFirst();

            // Find Category.
            Category category = bgRealm.where(Category.class).equalTo(CategoryFields.UNIQUE_ID,
                    data.getLong(TransactionFields.CATEGORY.UNIQUE_ID)).findFirst();

            // Update data.
            transaction.name = data.getString(TransactionFields.NAME);
            transaction.category = category;
            transaction.amount = data.getLong(TransactionFields.AMOUNT);
            transaction.timestamp = new Date(data.getLong(TransactionFields.TIMESTAMP));
            transaction.checkNumber = data.getInt(TransactionFields.CHECK_NUMBER);
            transaction.note = data.getString(TransactionFields.NOTE);

            adapter.notifyDataSetChanged();
        });
        if (actionMode != null) actionMode.finish();
    }

    /**
     * Starts action mode (if it hasn't been already).
     */
    private void startActionMode() {
        if (actionMode == null) actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
    }

    /**
     * Create a new {@link TransactionAdapter} based on the current view options and return it.
     * @return New {@link TransactionAdapter}, or null if {@link #transactions} is null or invalid.
     */
    private TransactionAdapter makeAdapter() {
        if (transactions == null || !transactions.isValid()) return null;
        return new TransactionAdapter(getActivity(), transactions);
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
