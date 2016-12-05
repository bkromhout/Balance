package com.bkromhout.balances.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bkromhout.balances.R;
import com.bkromhout.balances.adapters.CategoryAdapter;
import com.bkromhout.balances.data.models.Category;
import com.bkromhout.balances.data.models.CategoryFields;
import com.bkromhout.balances.data.models.Transaction;
import com.bkromhout.balances.data.models.TransactionFields;
import com.bkromhout.balances.events.ActionEvent;
import com.bkromhout.balances.events.CategoryClickEvent;
import com.bkromhout.balances.ui.Dialogs;
import com.bkromhout.rrvl.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Activity used to view, create, edit, and delete Categories.
 */
public class CategoriesActivity extends AppCompatActivity {
    // Views.
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler)
    RealmRecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fabNewCategory;
    @BindView(R.id.loading_categories)
    TextView tvLoadingResults;
    @BindView(R.id.no_categories)
    TextView tvNoResults;

    /**
     * Instance of Realm.
     */
    private Realm realm;
    /**
     * {@link Category}s currently shown in the recycler view.
     */
    private RealmResults<Category> categories;
    /**
     * Adapter for the RealmRecyclerView.
     */
    private CategoryAdapter adapter;
    /**
     * Realm change listener which takes care of toggling view visibility when {@link #categories} changes from empty to
     * non-empty (and vice-versa).
     */
    private final RealmChangeListener<RealmResults<Category>> emptyListener = results ->
            toggleEmptyState(results.isLoaded(), results.isEmpty());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();
        initUi();
    }

    /**
     * Initialize the UI.
     */
    private void initUi() {
        categories = realm.where(Category.class)
                          .findAllSortedAsync(CategoryFields.NAME);
        categories.addChangeListener(emptyListener);
        toggleEmptyState(categories.isLoaded(), categories.isEmpty());
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
        categories.removeChangeListener(emptyListener);
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

    @OnClick(R.id.fab)
    void onFabClick() {
        // Open Edit Category dialog to create new Category.
        Dialogs.categoryDialog(this, null, false, -1);
    }

    /**
     * Called for a category item click event.
     * @param event {@link CategoryClickEvent}.
     */
    @Subscribe
    public void onCategoryClickEvent(CategoryClickEvent event) {
        switch (event.getType()) {
            case ACTIONS:
                onCategoryActionClick(event);
                break;
        }
    }

    /**
     * Called when one of the actions for a category is clicked.
     * @param event {@link CategoryClickEvent}.
     */
    private void onCategoryActionClick(CategoryClickEvent event) {
        Category category = categories.where().equalTo(CategoryFields.UNIQUE_ID, event.getUniqueId()).findFirst();

        switch (event.getActionId()) {
            case R.id.action_edit_category:
                // Open Edit Category dialog to edit Category.
                Dialogs.categoryDialog(this, category.name, category.isCredit, category.uniqueId);
                break;
            case R.id.action_delete_category:
                // Figure out how many transactions are linked to this category.
                long transCount = realm.where(Transaction.class)
                                       .equalTo(TransactionFields.CATEGORY.UNIQUE_ID, event.getUniqueId())
                                       .count();

                if (transCount > 0)
                    // Open info dialog, we cannot delete a category with linked transactions.
                    Dialogs.simpleInfoDialog(this, getString(R.string.title_cannot_delete_category),
                            getResources().getQuantityString(R.plurals.info_cannot_delete_category, (int) transCount,
                                    transCount), getString(R.string.ok));
                else
                    // Open delete confirmation dialog.
                    Dialogs.simpleConfirmDialog(this, R.string.title_delete_category, R.string.prompt_delete_category,
                            R.string.delete, event.getActionId(), event.getUniqueId());
                break;
        }
    }

    /**
     * Called to take some action.
     * @param event {@link ActionEvent}.
     */
    @Subscribe
    public void onActionEvent(ActionEvent event) {
        switch (event.getActionId()) {
            case R.id.action_new_category:
                // Save new Category.
                saveNewCategory((Bundle) event.getData());
                break;
            case R.id.action_edit_category:
                // Update Category.
                updateCategory((Bundle) event.getData());
                break;
            case R.id.action_delete_category:
                // Delete clicked category.
                realm.executeTransactionAsync(bgRealm ->
                        bgRealm.where(Category.class)
                               .equalTo(CategoryFields.UNIQUE_ID, (long) event.getData())
                               .findFirst()
                               .deleteFromRealm());
                break;
        }
    }

    /**
     * Create and save a new {@link Category}.
     * @param data Date to use to create the new {@link Category}.
     */
    private void saveNewCategory(final Bundle data) {
        realm.executeTransactionAsync(bgRealm -> {
            Category newCategory = new Category(data.getString(CategoryFields.NAME), data.getBoolean(
                    CategoryFields.IS_CREDIT));
            bgRealm.copyToRealm(newCategory);
        });
    }

    /**
     * Update an existing {@link Category}.
     * @param data Data to use to find and update a {@link Category}.
     */
    private void updateCategory(final Bundle data) {
        realm.executeTransaction(bgRealm -> {
            Category category = bgRealm.where(Category.class).equalTo(CategoryFields.UNIQUE_ID, data.getLong(
                    CategoryFields.UNIQUE_ID)).findFirst();
            if (category == null) throw new IllegalArgumentException("Invalid Category ID");

            category.name = data.getString(CategoryFields.NAME);
            category.isCredit = data.getBoolean(CategoryFields.IS_CREDIT);

            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Create a new {@link CategoryAdapter} based on the current view options and return it.
     * @return New {@link CategoryAdapter}, or null if {@link #categories} is null or invalid.
     */
    private CategoryAdapter makeAdapter() {
        if (categories == null || !categories.isValid()) return null;
        return new CategoryAdapter(this, categories);
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
