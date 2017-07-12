package com.example.sarthak.news.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.sarthak.news.R;

public class AppPreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // load preferences from XML resource file
        addPreferencesFromResource(R.xml.preference_screen);
    }
}
