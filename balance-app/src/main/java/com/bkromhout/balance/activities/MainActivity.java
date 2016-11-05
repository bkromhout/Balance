package com.bkromhout.balance.activities;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bkromhout.balance.R;
import com.bkromhout.rrvl.RealmRecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }
}
