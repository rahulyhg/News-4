package com.example.sarthak.news.fragments;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sarthak.news.R;
import com.example.sarthak.news.utils.Constants;
import com.example.sarthak.news.utils.NetworkChangeReceiver;
import com.example.sarthak.news.utils.NetworkUtil;

public class ErrorFragment extends Fragment {

    String category;
    Bundle newsListFragmentBundle;

    NetworkChangeReceiver networkChangeReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_error, container, false);

        // get news category from NewsListFragment
        // though news category is not used in ErrorFragment, it is stored to send back value
        // to NewsListFragment after connection becomes available
        category = getArguments().getString(Constants.INTENT_KEY_NEWS_CATEGORY, getString(R.string.KEY_TOP_STORIES));

        // show NewsListFragment as soon as connection becomes available
        showNewsListFragment();

        // register broadcast receiver which listens for change in network state
        getActivity().registerReceiver(networkChangeReceiver, new IntentFilter(Constants.CONNECTIVITY_CHANGE_ACTION));

        return view;
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        // un-register broadcast receiver
        getActivity().unregisterReceiver(networkChangeReceiver);
    }

    /**
     * Launches NewsListFragment When connection becomes available.
     *
     * Since ErrorFragment is launched only when there is no network access,
     * as soon as network is available, it launches NewListFragment to display news item.
     */
    private void showNewsListFragment() {

        newsListFragmentBundle = new Bundle();

        networkChangeReceiver = new NetworkChangeReceiver() {

            @Override
            protected void dismissSnackbar() {}

            @Override
            protected void setUpLayout() {

                if (NetworkUtil.getConnectivityStatus(getActivity())) {

                    //pass news category to fragment as bundle
                    Fragment newsListFragment = new NewsListFragment();
                    newsListFragmentBundle.putString(Constants.INTENT_KEY_NEWS_CATEGORY, category);
                    newsListFragment.setArguments(newsListFragmentBundle);

                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, newsListFragment);
                    fragmentTransaction.commit();
                }

            }
        };
    }
}
