package com.example.sarthak.news.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sarthak.news.R;
import com.example.sarthak.news.models.Item;
import com.example.sarthak.news.utils.SquareImageView;
import com.squareup.picasso.Picasso;

/**
 * View holder class for NewsList recycler adapter. Represents single row of RecyclerView.
 */

class NewsViewHolder extends RecyclerView.ViewHolder {

    View itemView;

    private TextView title, date;
    private SquareImageView thumbnail;
    ImageButton overflow;
    LinearLayout linearLayout;

    public NewsViewHolder(View itemView) {
    
        super(itemView);

        this.itemView = itemView;
        title = (TextView) itemView.findViewById(R.id.news_list_card_label);
        date = (TextView) itemView.findViewById(R.id.news_list_card_date);
        thumbnail = (SquareImageView) itemView.findViewById(R.id.news_list_card_image);
        overflow = (ImageButton) itemView.findViewById(R.id.news_list_card_overflow_btn);
        linearLayout = (LinearLayout) itemView.findViewById(R.id.news_list_card_layout);
    }


    /**
     * Bind data from array list to respective position in RecyclerView
     *
     * @param mContext is the context of NewsListFragment
     * @param item is the news item data to set in each row
     * @param downloadImages is a boolean value storing user selected option of displaying images or not
     */
    void bindData(Context mContext, Item item, boolean downloadImages) {

        title.setText(item.getHeadline());
        date.setText(mContext.getString(R.string.updated_date, item.getDate()));

        // check for status of boolean downloadImages
        // display default image if user has disabled downloading images
        if (downloadImages) {

            Picasso.with(mContext)
                    .load(item.getImageUrl1())
                    .fit()
                    .into(thumbnail);
        } else {

            thumbnail.setImageResource(R.drawable.default_image);
        }
    }
}
