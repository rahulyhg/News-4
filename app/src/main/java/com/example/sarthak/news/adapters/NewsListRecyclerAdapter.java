package com.example.sarthak.news.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sarthak.news.firebasemanager.FirebaseAuthorisation;
import com.example.sarthak.news.models.Item;
import com.example.sarthak.news.R;
import com.example.sarthak.news.utils.Constants;
import com.example.sarthak.news.utils.RecyclerViewItemClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * NewsList RecyclerView adapter
 *
 * @author Sarthak Grover
 */

public class NewsListRecyclerAdapter extends RecyclerView.Adapter<NewsViewHolder> {

    private Context mContext;

    private List<Item> newsCard;
    private ArrayList<Item> newsDetails;

    private String category;

    private boolean changeReadNewsColor;
    private boolean downloadImages;

    private PopupMenu popup;

    private DatabaseReference mDatabase;

    private int count = 0;

    private RecyclerViewItemClickListener onRecyclerViewItemClickListener;

    public NewsListRecyclerAdapter(Context mContext, List<Item> newsListData
            , ArrayList<Item> newsListDetails, String category, boolean changeReadNewsColor, boolean downloadImages) {

        this.mContext = mContext;
        this.newsCard = newsListData;
        this.newsDetails = newsListDetails;
        this.category = category;
        this.changeReadNewsColor = changeReadNewsColor;
        this.downloadImages = downloadImages;

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void setOnRecyclerViewItemClickListener(RecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_news_list, parent, false);

        return new NewsViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(final NewsViewHolder holder, final int position) {
        Item item = newsCard.get(holder.getAdapterPosition());

        // bind data to view holder
        holder.bindData(mContext, item, downloadImages);

        // change background of read articles
        changeReadNewsColor(holder, position);

        // get total number of articles inside 'Bookmarks' from firebase database
        // since it is async task, call beforehand to get total count
        // used when adding articles to 'Bookmarks' where bookmark count is needed
        getBookmarkCount();

        // recycler view onClick listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onRecyclerViewItemClickListener.onClick(view, position);
            }
        });

        // pop menu button onClick listener
        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // shows popup to bookmark/un-bookmark articles
                showPopup(view, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsCard.size();
    }

    /**
     * PURPOSE: Changes background for read articles.
     *
     * Checks for boolean 'status' in shared preferences for each article.
     * Whenever an article is clicked, boolean 'status' for that article returns true.
     * So, if 'status' returns true, then it checks for theme of the app and changes
     * the background of the cardview to desired color since color is different
     * for light and dark theme.
     *
     * @param holder binds an item from arraylist to view holder
     * @param position gives the position of each item in recycler view
     */
    private void changeReadNewsColor(final NewsViewHolder holder, final int position) {

        String currentUser = new FirebaseAuthorisation(mContext).getCurrentUser();

        SharedPreferences prefs = mContext.getSharedPreferences(Constants.READ_ARTICLES_STATUS_SHARED_PREFERENCES, MODE_PRIVATE);
        boolean status = prefs.getBoolean(currentUser + category + String.valueOf(position), false);

        if (status) {
            if (changeReadNewsColor)
                holder.linearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lightBookmarkBackgroundColor));
            else
                holder.linearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.darkBookmarkBackgroundColor));
        }
    }

    /**
     * Gets number of children inside 'Bookmarks' of current user from firebase database
     */
    private void getBookmarkCount() {

        FirebaseAuthorisation firebaseAuthorisation = new FirebaseAuthorisation(mContext);

        String currentUser = firebaseAuthorisation.getCurrentUser();

        mDatabase.child(mContext.getString(R.string.KEY_USERS)).child(currentUser)
                .child(mContext.getString(R.string.KEY_BOOKMARKS)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // gives total number of children in the database reference
                count = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * PURPOSE: Shows a popup menu to add a bookmark or remove a bookmark.
     *
     * It checks the category which calls the NewsListFragment.
     * If the category is 'Bookmarks' it shows remove bookmark popup menu.
     * Else it shows add bookmark popup menu.
     *
     * @param view is the view of the image button that is clicked.
     * @param position is the index of the news item that is to be bookmarked
     */
    private void showPopup(View view, final int position) {

        popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();

        if (category.equals(mContext.getString(R.string.KEY_BOOKMARKS))) {

            inflater.inflate(R.menu.remove_bookmark, popup.getMenu());
            onClickRemoveFromList(position);
        } else {

            inflater.inflate(R.menu.bookmark, popup.getMenu());
            onClickBookmark(position);
        }
        popup.show();
    }

    /**
     * PURPOSE: Adds selected news item to 'Bookmarks' key of current user in firebase database.
     *
     * It puts the data of selected news item to a hash map and then adds it the
     * 'Bookmarks' key of the current user.
     *
     * @param position is the index of the news item that is to be bookmarked
     */
    private void onClickBookmark(final int position) {

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int bookmarkCount = count;

                if (item.getItemId() == R.id.bookmark_btn) {

                    // get firebase current user
                    FirebaseAuthorisation firebaseAuthorisation = new FirebaseAuthorisation(mContext);
                    String currentUser = firebaseAuthorisation.getCurrentUser();

                    // firebase database reference
                    DatabaseReference bookmarkReference = mDatabase.child(mContext.getString(R.string.KEY_USERS))
                            .child(currentUser).child(mContext.getString(R.string.KEY_BOOKMARKS)).child("0" + String.valueOf(bookmarkCount + 1));

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("date", newsDetails.get(position).getDate());
                    userMap.put("headline", newsDetails.get(position).getHeadline());
                    userMap.put("imageUrl1", newsDetails.get(position).getImageUrl1());
                    userMap.put("imageUrl2", newsDetails.get(position).getImageUrl2());
                    userMap.put("textUrl", newsDetails.get(position).getTextUrl());

                    // firebase database onComplete listener
                    bookmarkReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                Toast.makeText(mContext, R.string.bookmark_added, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                return false;
            }
        });
    }

    /**
     * PURPOSE: Removes selected news data from 'Bookmarks' of current user in firebase database.
     *
     * It removes the selected news item from the 'Bookmarks' key of the current user.
     * Then it runs a loop to decrease the index of the following news items by 1.
     * It also removes the boolean 'status' of the news item from the shared preferences.
     *
     * @param position is the index of the news item that is to be removed from bookmarks
     */
    private void onClickRemoveFromList(final int position) {

        final int newsPosition = position + 1;

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.remove_bookmark_btn) {

                    // get firebase current user
                    String currentUser = new FirebaseAuthorisation(mContext).getCurrentUser();

                    // remove shared preferences 'Read' status
                    SharedPreferences.Editor editor = mContext
                            .getSharedPreferences(Constants.READ_ARTICLES_STATUS_SHARED_PREFERENCES, MODE_PRIVATE).edit();
                    editor.remove(currentUser + category + String.valueOf(position));
                    editor.apply();

                    // firebase database reference
                    DatabaseReference removeDataReference = mDatabase.child(mContext.getString(R.string.KEY_USERS))
                            .child(currentUser).child(mContext.getString(R.string.KEY_BOOKMARKS)).child("0" + String.valueOf(newsPosition));
                    removeDataReference.removeValue();

                    // decrease index of following news items by 1.
                    for (int i = newsPosition + 1 ; i <= newsDetails.size() ; i++) {

                        DatabaseReference bookmarkReference = mDatabase.child(mContext.getString(R.string.KEY_USERS))
                                .child(currentUser).child(mContext.getString(R.string.KEY_BOOKMARKS)).child("0" + String.valueOf(i - 1));

                        Item newsItem = newsDetails.get(i - 1);

                        bookmarkReference.setValue(newsItem);
                    }

                    // remove last item in firebase database as it has been copied to its previous location
                    DatabaseReference removeDatabaseFinalValueReference = mDatabase.child(mContext.getString(R.string.KEY_USERS))
                            .child(currentUser).child(mContext.getString(R.string.KEY_BOOKMARKS)).child("0" + String.valueOf(newsDetails.size()));
                    removeDatabaseFinalValueReference.removeValue();
                }
                return false;
            }
        });
    }
}
