package com.example.sarthak.news.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.TextView;

import com.example.sarthak.news.R;
import com.example.sarthak.news.firebasemanager.FirebaseAuthorisation;
import com.example.sarthak.news.fragments.AppPreferencesFragment;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView logOut = (TextView) findViewById(R.id.settings_signOut);

        // call settings preference fragment
        launchSettingsFragment();

        // handle onClick listener
        logOut.setOnClickListener(this);
    }

    @Override
    protected void configureToolbar() {
        super.configureToolbar();

        // set up toolbar UP/Back button
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_settings;
    }

    @Override
    protected int getToolbarID() {
        return R.id.settings_toolbar;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.settings);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // launch animation
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_out_left);
    }

    //------------------------------------------------------------------------
    // button onClick listener
    //------------------------------------------------------------------------
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.settings_signOut :

                logoutUser();
                break;
        }
    }

    /**
     * Launch settings preference fragment
     */
    private void launchSettingsFragment() {

        PreferenceFragment settingsFragment = new AppPreferencesFragment();

        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.settings_frame, settingsFragment);
        fragmentTransaction.commit();
    }

    /**
     * Set up alert dialog to log out user.
     */
    private void logoutUser() {

        // setup alert dialog
        new AlertDialog.Builder(this)
                .setMessage(R.string.settings_alert_dialog_message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // logout current user from firebase authentication
                        new FirebaseAuthorisation(SettingsActivity.this).logoutUser();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
}
