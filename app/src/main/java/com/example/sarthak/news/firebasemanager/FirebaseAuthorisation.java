package com.example.sarthak.news.firebasemanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.sarthak.news.R;
import com.example.sarthak.news.activities.HomeScreenActivity;
import com.example.sarthak.news.activities.LoginActivity;
import com.example.sarthak.news.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseAuthorisation {

    private Context mContext;

    private FirebaseAuth mAuth;

    // set up firebase authentication
    public FirebaseAuthorisation(Context context) {

        this.mContext = context;

        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Registers new user to firebase authentication and adds details to firebase database
     *
     * @param name is the name of the user
     * @param email is the email ID of the user
     * @param password is the password of the user
     * @param mProgressDialog is a progress dialog which is dismissed when the user registration is complete
     */
    public void registerUser(final String name, final String email, String password, final ProgressDialog mProgressDialog) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {

                    mProgressDialog.hide();
                    Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                } else {

                    // get current user UID
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    String UID = currentUser.getUid();

                    DatabaseReference mDatabase;

                    // create a database reference for the user
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(UID);

                    User user = new User(name, email);

                    // store user details to firebase database
                    mDatabase.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                // dismiss progress dialog
                                mProgressDialog.dismiss();

                                // launch HomeScreenActivity when user registration is complete
                                Intent mainActivity = new Intent(mContext, HomeScreenActivity.class);
                                mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                mContext.startActivity(mainActivity);

                                // finish current activity
                                ((Activity) mContext).finish();
                            } else {

                                // display error message
                                mProgressDialog.dismiss();
                                Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Logs in registered user with firebase authentication
     *
     * @param email is the email ID of the user
     * @param password is the password of the user
     * @param mProgressDialog is a progress dialog which is dismissed when the user registration is complete
     */
    public void loginUser(String email, String password, final ProgressDialog mProgressDialog) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    // dismiss progress dialog
                    mProgressDialog.dismiss();

                    // launch HomeScreenActivity when user registration is complete
                    Intent mainIntent = new Intent(mContext, HomeScreenActivity.class);
                    mContext.startActivity(mainIntent);

                    // finish current activity
                    ((Activity) mContext).finish();
                } else {

                    // display error message
                    mProgressDialog.dismiss();
                    Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // get current user UID
    public String getCurrentUser() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }
    }

    // log out current user
    public void logoutUser() {

        // sign out from firebase authentication
        mAuth.signOut();

        // launch start activity
        Intent startIntent = new Intent(mContext, LoginActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(startIntent);

        // finish current activity
        ((Activity) mContext).finish();
    }
}
