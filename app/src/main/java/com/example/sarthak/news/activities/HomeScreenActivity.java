package com.example.sarthak.news.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuInflater;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.sarthak.news.firebasemanager.FirebaseAuthorisation;
import com.example.sarthak.news.fragments.NavigationDrawerFragment;
import com.example.sarthak.news.utils.Constants;
import com.example.sarthak.news.utils.NavigationDrawerItemClickListener;
import com.example.sarthak.news.R;
import com.example.sarthak.news.fragments.NewsListFragment;
import com.example.sarthak.news.utils.NetworkChangeReceiver;
import com.example.sarthak.news.utils.NetworkUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeScreenActivity extends BaseActivity implements NavigationDrawerItemClickListener {

    Bundle newsListFragmentBundle = new Bundle();
    Bundle navigationDrawerFragmentBundle = new Bundle();

    boolean changeActionBarColor;

    FrameLayout frameLayout;

    ArrayList<String> newsCategory = new ArrayList<>();

    NetworkChangeReceiver networkChangeReceiver;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        frameLayout = (FrameLayout) findViewById(R.id.content_frame);

        // display error snackbar when no internet connection
        showErrorSnackbar();

        // returns true if theme is 'Light'
        // change action bar colour for each news category when theme is 'Light'
        // show black action bar when theme is 'Dark'
        // 'changeActionBarColor' is passed to NewsListFragment to decide action bar color
        changeActionBarColor = theme.equals(getString(R.string.theme_light));

        // launch default fragment as 'Home' category
        launchDefaultFragment();

        // retrieve an instance of Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // read list of news category from firebase database
        readCategoryData();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home_screen;
    }

    @Override
    protected int getToolbarID() {
        return R.id.toolbar;
    }

    @Override
    protected String getToolbarTitle() {
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // launch animation
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_out_right);

        // check for firebase user login
        // redirect to start activity if no user login
        FirebaseAuthorisation firebaseAuthorisation = new FirebaseAuthorisation(HomeScreenActivity.this);
        firebaseAuthorisation.checkFirebaseLogin();

        // register broadcast receiver which listens for change in network state
        registerReceiver(networkChangeReceiver, new IntentFilter(Constants.CONNECTIVITY_CHANGE_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();

        // unregister broadcast receiver
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // check the state of DrawerLayout
        // close DrawerLayout if open
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * NavigationDrawerItemClickListener callback
     *
     * itemClicked is called when an item is clicked from the navigation drawer recycler view.
     * Called on NavigationDrawerItemClickListener from NavigationDrawerFragment.
     *
     * Used to close the drawer layout.
     */
    @Override
    public void itemCLicked() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // inflate settings option menu in toolbar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings :

                // launch settings activity
                Intent settingsIntent = new Intent(HomeScreenActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * PURPOSE: Launch default fragment as 'Home' from firebase database
     *
     * Set up default news category as 'Home' in NewsListFragment.
     * Send changeActionBar value to fragment to set up action bar colour.
     * Send downloadImages status to decide whether to display images or not.
     */
    private void launchDefaultFragment() {

        Fragment newsListFragment = new NewsListFragment();
        newsListFragmentBundle.putString(Constants.INTENT_KEY_NEWS_CATEGORY, getString(R.string.KEY_HOME));
        newsListFragmentBundle.putBoolean(Constants.INTENT_KEY_ACTION_BAR, changeActionBarColor);
        newsListFragmentBundle.putBoolean(Constants.INTENT_KEY_DOWNLOAD_IMAGES, downloadImages);
        newsListFragment.setArguments(newsListFragmentBundle);

        FragmentTransaction newsListFragmentTransaction = getSupportFragmentManager().beginTransaction();
        newsListFragmentTransaction.replace(R.id.content_frame, newsListFragment);
        newsListFragmentTransaction.commit();
    }

    /**
     * PURPOSE: Read news category from firebase database
     *
     * Clears newsCategory arraylist to remove any redundant data
     * and then adds values from firebase database to array list.
     *
     * These values are then passed to NavigationDrawerFragment
     * to display data in the recycler view.
     */
    private void readCategoryData() {

        mDatabase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // clear newsCategory to remove any redundant data in the array list.
                newsCategory.clear();
                newsCategory.add(getString(R.string.KEY_BOOKMARKS));

                // retrieve all keys from Firebase database and add them to arraylist.
                for (final DataSnapshot child : dataSnapshot.getChildren()) {

                    // do not add 'Users' key to the newsCategory
                    // only add news category keys from the database to array list
                    if (!child.getKey().equals(getString(R.string.KEY_USERS))) {

                        newsCategory.add(child.getKey());
                    }
                }

                // launch NavigationDrawerFragment
                launchNavigationDrawerFragment();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * PURPOSE: Launch NavigationDrawerFragment in navigation drawer with list of
     * news category obtained from firebase database
     *
     * newsCategory arraylist is sent to bundle and which is to be passed to NavigationDrawerFragment later.
     * changeActionBarColor is sent to set action bar color based on app theme.
     * downloadImages is sent to decide whether to display images or not.
     */
    public void launchNavigationDrawerFragment() {

        Fragment navigationDrawerFragment = new NavigationDrawerFragment();

        navigationDrawerFragmentBundle.putStringArrayList(Constants.INTENT_KEY_NEWS_CATEGORY, newsCategory);
        navigationDrawerFragmentBundle.putBoolean(Constants.INTENT_KEY_ACTION_BAR, changeActionBarColor);
        navigationDrawerFragmentBundle.putBoolean(Constants.INTENT_KEY_DOWNLOAD_IMAGES, downloadImages);
        navigationDrawerFragment.setArguments(navigationDrawerFragmentBundle);

        FragmentTransaction navigationDrawerFragmentTransaction = getSupportFragmentManager().beginTransaction();
        navigationDrawerFragmentTransaction.replace(R.id.home_frame, navigationDrawerFragment);
        navigationDrawerFragmentTransaction.commit();
    }

    /**
     * PURPOSE: Shows error snackbar when there is no network state
     *
     * Listens to the broadcast receiver for any change in network state
     * and shows an error snackbar when there is no network state.
     *
     * WORKING:
     * When the broadcast receiver detects any change in network state,
     * onReceive method in broadcast receiver is called.
     *
     * Initially, the snackbar is initialised as null. If network connection
     * is available, snackbar remains null and hence dismissSnackbar and setUpLayout
     * do not do anything.
     *
     * However, when network is not available, snackbar is initialised and
     * displayed to the user. Now when the network becomes available again, broadcast
     * receiver is called and hence the method 'dismissSnackbar' dismisses the snackbar.
     */
    private void showErrorSnackbar() {

        networkChangeReceiver = new NetworkChangeReceiver() {

            Snackbar snackbar = null;

            @Override
            protected void dismissSnackbar() {

                if (snackbar != null) {

                    // dismiss snackbar
                    snackbar.dismiss();
                }
            }

            @Override
            protected void setUpLayout() {

                // check if no internet connection
                if (!NetworkUtil.getConnectivityStatus(HomeScreenActivity.this)) {

                    snackbar = Snackbar.make(frameLayout, R.string.error_connection, Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                }
            }
        };
    }
}
