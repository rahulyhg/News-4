package com.example.sarthak.news.activities;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sarthak.news.models.Item;
import com.example.sarthak.news.R;
import com.example.sarthak.news.utils.Constants;
import com.example.sarthak.news.utils.NetworkChangeReceiver;
import com.example.sarthak.news.utils.NetworkUtil;
import com.squareup.picasso.Picasso;

public class NewsDescriptionActivity extends BaseActivity {

    String date, headline, imageUrl1, imageUrl2, textUrl;
    Item newsItem;

    private CoordinatorLayout coordinatorLayout;
    private ImageView mNewsImage1, mNewsImage2;
    private TextView mHeadline, mDescription1, mDescription2, mDate;
    private ProgressBar progressBar;

    NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNewsImage1 = (ImageView) findViewById(R.id.news_desc_head_image);
        mNewsImage2 = (ImageView)  findViewById(R.id.news_desc_tail_image);
        mHeadline = (TextView) findViewById(R.id.news_desc_headline);
        mDescription1 = (TextView) findViewById(R.id.news_desc_body);
        mDescription2 = (TextView) findViewById(R.id.news_desc_tail_body);
        mDate = (TextView) findViewById(R.id.news_desc_date);
        progressBar = (ProgressBar) findViewById(R.id.news_desc_progressbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.layout_frame);

        // make progress bar visible when loading data
        // it is dismissed when loading is complete
        progressBar.setVisibility(View.VISIBLE);

        // display error snackbar when no internet connection
        showErrorSnackbar();

        // get news data from NavigationDrawerFragment as model 'Item'
        // 'Item' contains data as obtained from firebase database
        getData();

        // set retrieved data into textViews and imageViews and display to user
        displayData();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_news_description;
    }

    @Override
    protected int getToolbarID() {

        // returns 0 since there is no toolbar
        return 0;
    }

    @Override
    protected String getToolbarTitle() {

        // returns null since there is no toolbar
        return null;
    }

    @Override
    protected void onStart() {

        super.onStart();

        // launch animation
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_out_left);

        // register broadcast receiver which listens for change in network state
        registerReceiver(networkChangeReceiver, new IntentFilter(Constants.CONNECTIVITY_CHANGE_ACTION));
    }

    @Override
    protected void onStop() {

        super.onStop();

        // unregister broadcast receiver
        unregisterReceiver(networkChangeReceiver);
    }

    /**
     * Retrieves news data as model 'Item' from NavigationDrawerFragment
     * 'Item' contains data as obtained from firebase database
     */
    protected void getData() {

        newsItem = (Item) getIntent().getSerializableExtra(Constants.INTENT_KEY_NEWS_DATA);

        date = newsItem.getDate();
        headline = newsItem.getHeadline();
        imageUrl1 = newsItem.getImageUrl1();
        imageUrl2 = newsItem.getImageUrl2();
        textUrl = newsItem.getTextUrl();
    }

    /**
     * PURPOSE: Sets data obtained from newsItem to textViews and imageViews.
     *
     * Retrieves date and headline as string values and sets to textViews.
     * Image URLs obtained as strings are processed using Picasso and set to imageViews.
     * Text URL containing news description is processed using Volley.
     */
    private void displayData() {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = textUrl;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressBar.setVisibility(View.INVISIBLE);

                        mHeadline.setText(headline);
                        mDate.setText(getString(R.string.updated_date, date));

                        //-------------------------------------------------------------------------
                        // Divides the text into two strings. A part of text is displayed first.
                        // Then an image of news data is displayed. Rest of the text is displayed
                        // after this image. Done to ENHANCE UI design.
                        //-------------------------------------------------------------------------
                        int length = 2 * response.trim().length() / 3;
                        String halfText = response.substring(length);
                        int count = halfText.indexOf("\n");

                        mDescription1.setText(response.substring(0, length + count + 1));
                        mDescription2.setText(response.substring(length + count + 1));

                        // check for status of boolean downloadImages from base activity
                        // display default images if user has disabled downloading images
                        if (downloadImages) {

                            Picasso.with(getApplicationContext())
                                    .load(imageUrl1)
                                    .resize(480, 320)
                                    .into(mNewsImage1);

                            Picasso.with(getApplicationContext())
                                    .load(imageUrl2)
                                    .resize(480, 480)
                                    .into(mNewsImage2);
                        } else {

                            mNewsImage1.setImageResource(R.drawable.default_image);
                            mNewsImage2.setImageResource(R.drawable.default_image);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(NewsDescriptionActivity.this, R.string.volley_error, Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);
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
     * receiver is called and hence dismissSnackbar dismisses the snackbar.
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
                if (!NetworkUtil.getConnectivityStatus(NewsDescriptionActivity.this)) {

                    // dismiss progress bar
                    progressBar.setVisibility(View.INVISIBLE);
                    // show error message in snackbar
                    snackbar = Snackbar.make(coordinatorLayout, getString(R.string.error_connection), Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                }
            }
        };
    }
}
