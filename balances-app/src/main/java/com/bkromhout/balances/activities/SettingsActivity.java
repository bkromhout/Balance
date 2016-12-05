package com.bkromhout.balances.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bkromhout.balances.Balances;
import com.bkromhout.balances.Prefs;
import com.bkromhout.balances.R;

/**
 * Settings activity.
 */
public class SettingsActivity extends AppCompatActivity {
    // Views.
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        // Set up toolbar.
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Show preferences fragment.
        getFragmentManager().beginTransaction().replace(R.id.content, new SettingsFragment()).commit();
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
     * Custom preferences fragment.
     */
    public static class SettingsFragment extends PreferenceFragment {
        /**
         * Preferences.
         */
        private final Prefs prefs = Balances.getPrefs();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            initUi();
        }

        /**
         * Init the UI.
         */
        private void initUi() {
            // Set up Edit Categories item.
            Preference editCategories = getPreferenceScreen().findPreference(Prefs.EDIT_CATEGORIES);
            editCategories.setOnPreferenceClickListener(this::onEditCategoriesClick);
        }

        /**
         * Opens the {@link CategoriesActivity}.
         */
        private boolean onEditCategoriesClick(Preference preference) {
            startActivity(new Intent(getActivity(), CategoriesActivity.class));
            return true;
        }
    }
}
