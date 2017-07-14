package com.example.sarthak.news.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sarthak.news.R;
import com.example.sarthak.news.adapters.NavigationDrawerRecyclerAdapter;
import com.example.sarthak.news.utils.Constants;
import com.example.sarthak.news.utils.NavigationDrawerItemClickListener;
import com.example.sarthak.news.utils.RecyclerViewItemClickListener;

import java.util.ArrayList;

public class NavigationDrawerFragment extends Fragment implements RecyclerViewItemClickListener {

    RecyclerView recyclerView;
    NavigationDrawerRecyclerAdapter adapter;

    Bundle newsListBundle = new Bundle();
    ArrayList<String> newsCategory;
    boolean changeActionBarColor;
    boolean downloadImages;

    NavigationDrawerItemClickListener navigationDrawerItemClickListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        // retrieve a set of values from HomeScreenActivity
        // newsCategory arraylist is displayed as items in recycler view.
        // changeActionBarColor is sent to NewsListFragment to set action bar color based on app theme.
        // downloadImages is sent to NewsListFragment to decide whether to display images or not.
        newsCategory = getArguments().getStringArrayList(Constants.INTENT_KEY_NEWS_CATEGORY);
        changeActionBarColor = getArguments().getBoolean(Constants.INTENT_KEY_ACTION_BAR, true);
        downloadImages = getArguments().getBoolean(Constants.INTENT_KEY_DOWNLOAD_IMAGES, true);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        adapter = new NavigationDrawerRecyclerAdapter(getActivity(), newsCategory);
        adapter.setOnRecyclerViewItemClickListener(this);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        // initialise interface click listener
        navigationDrawerItemClickListener = (NavigationDrawerItemClickListener) getActivity();

        return view;
    }

    /**
     * Launches NewsListFragment with a list of news item as per the category of news
     * selected by the user.
     *
     * Arguments sent to the the NewsListFragment:
     * newsCategory which decides the category of news to be displayed to the user.
     * changeActionBarColor which changes action bar colour only when 'theme' is 'Light'.
     * Else it shows a black action bar.
     * downloadImages which is retrieved from shared preferences to specify whether images
     * should be displayed or not.
     *
     * It also contains an itemClickListener which calls onClick in HomeScreenActivity to
     * close the drawer layout.
     */
    @Override
    public void onClick (View view,int position) {

        Fragment newsListFragment = new NewsListFragment();

        newsListBundle.putString(Constants.INTENT_KEY_NEWS_CATEGORY, newsCategory.get(position));
        newsListBundle.putBoolean(Constants.INTENT_KEY_ACTION_BAR, changeActionBarColor);
        newsListBundle.putBoolean(Constants.INTENT_KEY_DOWNLOAD_IMAGES, downloadImages);
        newsListFragment.setArguments(newsListBundle);

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, newsListFragment);
        fragmentTransaction.commit();

        // callback to NavigationDrawerItemClickListener
        // used to close drawer layout
        navigationDrawerItemClickListener.itemCLicked();
    }
}
