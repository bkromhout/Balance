package com.bkromhout.balances.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bkromhout.balances.R;
import com.bkromhout.balances.adapters.BalanceAdapter;
import com.bkromhout.balances.data.models.Balance;
import com.bkromhout.balances.data.models.BalanceFields;
import com.bkromhout.balances.events.BalanceClickEvent;
import com.bkromhout.rrvl.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * The configuration screen for the {@link BalanceWidgetProvider BalanceWidgetProvider}. This activity is not in the
 * "activities" package because it requires the use of package-private methods in {@link WidgetHandler}.
 */
public class ConfigureWidgetActivity extends AppCompatActivity {
    // Views.
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler)
    RealmRecyclerView recyclerView;
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
     * The widget ID.
     */
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    /**
     * Realm change listener which takes care of toggling view visibility when {@link #balances} changes from empty to
     * non-empty (and vice-versa).
     */
    private final RealmChangeListener<RealmResults<Balance>> emptyListener = results ->
            toggleEmptyState(results.isLoaded(), results.isEmpty());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_configure_widget);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find the widget id from the intent.
        if (getIntent().hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
            appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        // Make sure that we actually have a widget ID. If we don't, finish now.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        } else {
            realm = Realm.getDefaultInstance();
            // Init the UI.
            initUi();
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
        RecyclerView realRv = recyclerView.getRecyclerView();
        realRv.addItemDecoration(new DividerItemDecoration(realRv.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when a balance item is clicked.
     * @param event {@link BalanceClickEvent}.
     */
    @Subscribe
    public void onBalanceClickEvent(BalanceClickEvent event) {
        // No matter what type it is, we consider it as the user picking that Balance for the widget. The first thing
        // we do is create an entry for the widget in the widget SharedPreferences file.
        WidgetHandler.saveWidgetEntry(this, appWidgetId, event.getUniqueId());

        // Then we trigger a content update for the widget.
        WidgetHandler.updateWidget(this, realm, appWidgetId);

        // And we finally finish with RESULT_OK, being sure to pass back the original widget ID.
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId));
        finish();
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

