package com.example.sarthak.news.fragments;

import android.annotation.TargetApi;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.sarthak.news.R;
import com.example.sarthak.news.activities.HomeScreenActivity;
import com.example.sarthak.news.activities.NewsDescriptionActivity;
import com.example.sarthak.news.adapters.NewsListRecyclerAdapter;
import com.example.sarthak.news.firebasemanager.FirebaseAuthorisation;
import com.example.sarthak.news.models.Item;
import com.example.sarthak.news.utils.Constants;
import com.example.sarthak.news.utils.RecyclerViewItemClickListener;
import com.example.sarthak.news.utils.NetworkChangeReceiver;
import com.example.sarthak.news.utils.NetworkUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class NewsListFragment extends Fragment implements RecyclerViewItemClickListener {

    RecyclerView recyclerView;

    private NewsListRecyclerAdapter adapter;
    private List<Item> newsListData = new ArrayList<>();

    private ProgressDialog progressDialog;

    ArrayList<Item> newsItem = new ArrayList<>();
    String category;
    boolean changeActionBarColor;
    boolean downloadImages;

    Bundle bundle;

    int flag = 0;

    NetworkChangeReceiver networkChangeReceiver;

    private DatabaseReference mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_news_list, container, false);

        // retrieve a set of values from NavigationDrawerFragment/HomeScreenActivity
        // category is used to display news articles of corresponding news category.
        // changeActionBarColor is used to set action bar color based on app theme.
        // displayImages is used to decide whether to display images or not.
        category = getArguments().getString(Constants.INTENT_KEY_NEWS_CATEGORY, getString(R.string.KEY_TOP_STORIES));
        changeActionBarColor = getArguments().getBoolean(Constants.INTENT_KEY_ACTION_BAR, true);
        downloadImages = getArguments().getBoolean(Constants.INTENT_KEY_DOWNLOAD_IMAGES, true);

        // display an error fragment when no internet connection
        showErrorFragment();

        // register network change broadcast receiver
        getActivity().registerReceiver(networkChangeReceiver, new IntentFilter(Constants.CONNECTIVITY_CHANGE_ACTION));

        // set up progress dialog
        setUpProgressDialog();

        // read news data from firebase database
        readNewsData();

        // set action bar color if theme is 'Light'
        // if theme is 'Dark' black action bar is set
        if (changeActionBarColor) {

            setActionBarColor(category);
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.news_data);
        adapter = new NewsListRecyclerAdapter(getActivity(), newsListData, newsItem, category, changeActionBarColor, downloadImages);
        // recycler view on click listener
        adapter.setOnRecyclerViewItemClickListener(this);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set activity title as app name if news category is 'Home'
        // Else set title as news category
        if (category.equals(getString(R.string.KEY_HOME))) {
            ((HomeScreenActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));
        } else {
            ((HomeScreenActivity) getActivity()).getSupportActionBar().setTitle(category);
        }

        // update recycler adapter if any changes
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // unregister network change broadcast receiver
        getActivity().unregisterReceiver(networkChangeReceiver);
    }

    /**
     * Recycler view on click listener
     *
     * Launches NewsDescriptionActivity.
     */
    @Override
    public void onClick(View view, int position) {

        // get current user UID
        FirebaseAuthorisation firebaseAuthorisation = new FirebaseAuthorisation(getActivity());
        String currentUser = firebaseAuthorisation.getCurrentUser();

        // update boolean value for selected article in recycler view as 'true'
        // this is done to change background for read articles in recycler view
        // background of article is changed if value for the article in shared preferences is true
        SharedPreferences.Editor editor = getActivity()
                .getSharedPreferences(Constants.READ_ARTICLES_STATUS_SHARED_PREFERENCES, MODE_PRIVATE).edit();
        editor.putBoolean(currentUser + category + String.valueOf(position), true);
        editor.apply();

        // launch NewsDescriptionActivity with data of the selected news item
        Intent intent = new Intent(getActivity(), NewsDescriptionActivity.class);
        intent.putExtra(Constants.INTENT_KEY_NEWS_DATA, newsItem.get(position));
        startActivity(intent);
    }

    /**
     * Set up progress dialog
     */
    private void setUpProgressDialog() {

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.news_fetch_message));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    /**
     * PURPOSE: Retrieve firebase data reference to access news articles
     *
     * Since all the news categories are stored at the root of firebase database, hence,
     * their database reference is the the root as well.
     * However, 'Bookmarks' is something which will be specific to a particular user.
     * Hence, the data for 'Bookmarks' is stored in firebase database in keys which are same
     * as current user UID. Hence, firebase data reference for 'Bookmarks' is set accordingly.
     */
    private void readNewsData() {

        if (category.equals(getString(R.string.KEY_BOOKMARKS))) {

            checkForBookmarks();
        } else {

            mDatabase = FirebaseDatabase.getInstance().getReference().child(category);
            readData();
        }
    }

    /**
     * Set firebase database reference to current user UID.
     *
     * Checks if there exists any values in 'Bookmarks' key of firebase database.
     * If value exists, firebase database reference is set to current user UID and data is read.
     * Else no values are read and hence no item is displayed in recycler view.
     */
    private void checkForBookmarks() {

        FirebaseAuthorisation firebaseAuthorisation = new FirebaseAuthorisation(getActivity());

        final String currentUser = firebaseAuthorisation.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.KEY_USERS)).child(currentUser).child(getString(R.string.KEY_BOOKMARKS));

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    readData();
                } else {

                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Read firebase database and store values of news articles as model 'Item'
     * to newsItem
     *
     * news headline, date and imageUrl are stored in newsListData to be displayed in
     * news list recycler view.
     */
    public void readData() {

        if (mDatabase != null) {

            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // clear newsItem and newsListData to remove any redundant data
                    newsItem.clear();
                    newsListData.clear();

                    int position = 0;
                    for ( DataSnapshot userDataSnapshot : dataSnapshot.getChildren() ) {
                        if (userDataSnapshot != null) {
                            // add values fetched from firebase database to 'Item' newsItem
                            newsItem.add(userDataSnapshot.getValue(Item.class));

                            // add news headline, date and imageUrl which are to be displayed in
                            // the recycler view to 'Item' newsListData
                            newsListData.add(new Item(newsItem.get(position).getHeadline(),
                                    newsItem.get(position).getImageUrl1(), newsItem.get(position).getDate()));

                            // update recycler view adapter
                            adapter.notifyDataSetChanged();
                            // dismiss progress dialog
                            progressDialog.dismiss();
                        } position++;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    adapter.notifyDataSetChanged();
                }
            });
        } else {

            // dismiss progress dialog
            progressDialog.dismiss();
        }
    }

    /**
     * PURPOSE: Shows ErrorFragment when there is no network state
     *
     * Listens to the broadcast receiver for any change in network state
     * and shows an ErrorFragment when there is no network state.
     *
     * WORKING:
     * When the broadcast receiver detects any change in network state,
     * onReceive method in broadcast receiver is called.
     *
     * Initially, if network is not available, display error fragment and flag is set set as 0.
     *
     * However, when network connection is available, flag becomes 1 and error fragment is not
     * called thereafter. The loaded news articles are displayed as it is.
     * Hence, this method displays an error fragment when network is not available when fragment
     * is called. However, when network is not available after the activity is launched, loaded
     * data is displayed to the user.
     */
    private void showErrorFragment() {

        bundle = new Bundle();

        networkChangeReceiver = new NetworkChangeReceiver() {

            @Override
            protected void dismissSnackbar() {}

            @Override
            protected void setUpLayout() {

                if (!NetworkUtil.getConnectivityStatus(getActivity())) {

                    if (flag == 0) {

                        progressDialog.dismiss();

                        Fragment errorFragment = new ErrorFragment();
                        bundle.putString(Constants.INTENT_KEY_NEWS_CATEGORY, category);
                        errorFragment.setArguments(bundle);

                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, errorFragment);
                        fragmentTransaction.commit();
                    }
                } else {

                    flag = 1;
                }
            }
        };
    }

    /**
     * Change action bar colour for each news category
     *
     * @param category is the news category
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setActionBarColor(String category) {

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        Window window = getActivity().getWindow();

        if (category.equals(getString(R.string.KEY_HOME))) {

            window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.pinkPrimaryDark));
            if (actionBar != null)
                actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.pinkPrimary)));
        } else if (category.equals(getString(R.string.KEY_TOP_STORIES))) {

            window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.purplePrimaryDark));
            if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.purplePrimary)));
        } else if (category.equals(getString(R.string.KEY_NATIONAL))) {

            window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.orangePrimaryDark));
            if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.orangePrimary)));
        } else if (category.equals(getString(R.string.KEY_INTERNATIONAL))) {

            window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.greenPrimaryDark));
            if (actionBar != null)
                actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.greenPrimary)));
        } else if (category.equals(getString(R.string.KEY_SPORTS))) {

            window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.yellowPrimaryDark));
            if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.yellowPrimary)));
        } else {

            window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
            if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.colorPrimary)));
        }
    }
}
