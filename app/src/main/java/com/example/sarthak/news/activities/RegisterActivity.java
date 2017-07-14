package com.example.sarthak.news.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.example.sarthak.news.R;
import com.example.sarthak.news.firebasemanager.FirebaseAuthorisation;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private TextInputLayout mName, mEmail, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button mSignUp = (Button) findViewById(R.id.register_sign_up_btn);

        mName = (TextInputLayout) findViewById(R.id.register_name);
        mEmail = (TextInputLayout) findViewById(R.id.register_email);
        mPassword = (TextInputLayout) findViewById(R.id.register_password);

        // sign up button onClick listener
        mSignUp.setOnClickListener(this);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_register;
    }

    @Override
    protected int getToolbarID() {
        return R.id.main_page_toolbar;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.register_activity_title);
    }

    @Override
    protected void configureToolbar() {
        super.configureToolbar();

        // set up toolbar UP/Back button
        // handle NullPointerException
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // launch animation
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_out_left);
    }

    //------------------------------------------------------------------------
    // button onClick listener
    //------------------------------------------------------------------------
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.register_sign_up_btn :

                registerUser();
                break;
        }
    }

    /**
     * Register new user to firebase authentication
     */
    private void registerUser() {

        // setup progress dialog
        ProgressDialog mProgressDialog = new ProgressDialog(RegisterActivity.this);

        mProgressDialog.setTitle(getString(R.string.register_progress_dialog_title));
        mProgressDialog.setMessage(getString(R.string.register_progress_dialog_message));
        mProgressDialog.setCanceledOnTouchOutside(false);

        String name = mName.getEditText().getText().toString();
        String email = mEmail.getEditText().getText().toString();
        String password = mPassword.getEditText().getText().toString();

        if (!(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {

            mProgressDialog.show();

            // register user to firebase authentication
            // store name and email to firebase database
            FirebaseAuthorisation firebaseAuthorisation = new FirebaseAuthorisation(RegisterActivity.this);
            firebaseAuthorisation.registerUser(name, email, password, mProgressDialog);
        }
    }
}
