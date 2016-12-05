package com.bkromhout.balances.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bkromhout.balances.Balances;
import com.bkromhout.balances.R;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;
import timber.log.Timber;

/**
 * Activity which shows information about the app and open source libraries.
 */
public class AboutActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.about_app_version)
    TextView version;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initUi();
    }

    /**
     * Initialize UI.
     */
    private void initUi() {
        // Set version text.
        try {
            PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText(
                    Balances.get().getString(R.string.version_string, pkgInfo.versionName, pkgInfo.versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "Couldn't get our package information.");
        }

        // Fill in libraries.
        LibsSupportFragment libsFrag = new LibsBuilder().supportFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.about_libs, libsFrag).commit();
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

    @OnClick(R.id.github)
    void onGitHubLogoClicked() {
        // Open Minerva's GitHub repo in browser.
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bkromhout/Balances")));
    }
}
