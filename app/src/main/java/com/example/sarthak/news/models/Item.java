package com.example.sarthak.news.models;

import java.io.Serializable;

/**
 * POJO class to display data in RecyclerView
 *
 * @author Sarthak Grover
 */

public class Item implements Serializable {

    private String date;
    private String headline;
    private String imageUrl1;
    private String imageUrl2;
    private String textUrl;

    public Item() {
        // Default constructor required for calls to DataSnapshot.getValue(Item.class)
    }

    public Item(String headline, String imageUrl1, String date) {
        this.headline = headline;
        this.imageUrl1 = imageUrl1;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getHeadline() {
        return headline;
    }

    public String getImageUrl1() {
        return imageUrl1;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }

    public String getTextUrl() {
        return textUrl;
    }
}
