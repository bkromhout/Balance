package com.bkromhout.balances.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bkromhout.balances.R;
import com.bkromhout.balances.Utils;
import com.bkromhout.balances.data.models.Balance;
import com.bkromhout.rrvl.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Main activity of the app. Shows a list of balances and provides functionality related to them.
 */
public class MainActivity extends AppCompatActivity {

    // Views.
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    @BindView(R.id.recycler)
    RealmRecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fabNewBalance;
    @BindView(R.id.balances_empty)
    LinearLayout emptyBalances;

    /**
     * Instance of Realm.
     */
    private Realm realm;
    /**
     * {@link Balance}s currently shown in the recycler view.
     */
    private RealmResults<Balance> balances;
    /**
     * Realm change listener which takes care of toggling view visibility when {@link #balances} changes from empty to
     * non-empty (and vice-versa).
     */
    private final RealmChangeListener<RealmResults<Balance>> emptyListener = results -> toggleEmptyState(
            results.isEmpty());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // Init the UI.
        initUi();
    }

    private void initUi() {

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

    /**
     * Changes the visibility of UI elements so that the empty view is shown/hidden.
     * @param showEmptyView If true, show the empty view.
     */
    private void toggleEmptyState(boolean showEmptyView) {
        recyclerView.setVisibility(showEmptyView ? View.VISIBLE : View.GONE);
    }
}
