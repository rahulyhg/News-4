package com.example.sarthak.news.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.sarthak.news.firebasemanager.FirebaseAuthorisation;
import com.example.sarthak.news.utils.Constants;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        allowInternetAccess();

        launchActivity();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void allowInternetAccess() {

        int hasInternetAccessPermission = checkSelfPermission(Manifest.permission.INTERNET);

        if (hasInternetAccessPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.INTERNET},
                    Constants.REQUEST_PERMISSIONS);
            return;
        }
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
