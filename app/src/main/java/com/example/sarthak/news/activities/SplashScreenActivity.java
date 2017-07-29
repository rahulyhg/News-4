package com.example.sarthak.news.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.sarthak.news.firebasemanager.FirebaseAuthorisation;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launchActivity();
    }

    /**
     * Check firebase authorisation for current user UID.
     * If it returns null, launch LoginActivity. Else launch HomeScreenActivity.
     */
    private void launchActivity() {

        // get current user UID
        FirebaseAuthorisation firebaseAuthorisation = new FirebaseAuthorisation(SplashScreenActivity.this);
        String currentUser = firebaseAuthorisation.getCurrentUser();

        if (currentUser != null) {
            // launch HomeScreenActivity
            startActivity(new Intent(SplashScreenActivity.this, HomeScreenActivity.class));
        } else {
            // launch LoginActivity
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        }

        // remove SplashScreenActivity from stack
        finish();
    }
}
