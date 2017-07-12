package com.example.sarthak.news.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.sarthak.news.R;

/**
 * View holder class for NavigationDrawer recycler adapter. Represents single row of RecyclerView.
 */

class NavigationViewHolder extends RecyclerView.ViewHolder {

    public TextView title;

    public NavigationViewHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.nav_drawer_card_textView);
    }

    /**
     * Bind data from String to respective position in RecyclerView
     *
     * @param newsCategory is the news category string to set in every row
     */
    void bindData(String newsCategory) {

        title.setText(newsCategory);
    }
}
