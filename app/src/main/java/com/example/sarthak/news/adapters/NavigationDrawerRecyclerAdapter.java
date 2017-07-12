package com.example.sarthak.news.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sarthak.news.R;
import com.example.sarthak.news.utils.RecyclerViewItemClickListener;

import java.util.ArrayList;

/**
 * NavigationDrawer RecyclerView adapter
 *
 * @author Sarthak Grover
 */

public class NavigationDrawerRecyclerAdapter extends RecyclerView.Adapter<NavigationViewHolder> {

    private LayoutInflater inflater;

    private ArrayList<String> category = new ArrayList<>();

    private RecyclerViewItemClickListener onRecyclerViewItemClickListener;

    public NavigationDrawerRecyclerAdapter(Context mContext, ArrayList<String> category) {

        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.category = category;
    }

    public void setOnRecyclerViewItemClickListener(RecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener;
    }

    @Override
    public NavigationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = inflater.inflate(R.layout.cardview_navigation_drawer, parent, false);
        return new NavigationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final NavigationViewHolder holder, final int position) {

        final String newsCategory = category.get(position);

        // bind data to view holder
        holder.bindData(newsCategory);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onRecyclerViewItemClickListener.onClick(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {

        return category.size();
    }
}
