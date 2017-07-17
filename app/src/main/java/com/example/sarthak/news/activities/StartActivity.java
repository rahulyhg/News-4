package com.example.sarthak.news.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sarthak.news.R;
import com.example.sarthak.news.firebasemanager.FirebaseAuthorisation;

public class StartActivity extends BaseActivity implements View.OnClickListener {

    private EditText mEmailInput, mPassInput;
    private Button mLoginButton, mRegButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialise all view components
        setUpView();

        //-------------------------------------------------------------------
        // button onClick listeners
        //-------------------------------------------------------------------
        mRegButton.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_start;
    }

    @Override
    protected int getToolbarID() {

        // returns 0 since there is no toolbar
        return 0;
    }

    @Override
    protected String getToolbarTitle() {

        // returns null since there is no toolbar
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // launch animation
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_out_right);
    }

    //---------------------------------------------------------------------------------------
    // button onClick listener
    //---------------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.start_register_btn:

                launchRegisterActivity();
                break;

            case R.id.start_login_btn:

                loginUser();
                break;
        }
    }

    /**
     * Initialise all view components
     */
    private void setUpView() {

        mRegButton = (Button) findViewById(R.id.start_register_btn);
        mLoginButton = (Button) findViewById(R.id.start_login_btn);

        mEmailInput = (EditText) findViewById(R.id.start_email);
        mPassInput = (EditText) findViewById(R.id.start_password);
    }

    /**
     * Launch register activity
     */
    private void launchRegisterActivity() {

        Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    /**
     * Login registered user using firebase authentication
     */
    private void loginUser() {

        // setup progress dialog
        ProgressDialog mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setTitle(getString(R.string.start_progress_dialog_title));
        mProgressDialog.setMessage(getString(R.string.start_progress_dialog_message));
        mProgressDialog.setCanceledOnTouchOutside(false);

        String email = mEmailInput.getText().toString();
        String password = mPassInput.getText().toString();

        if (!(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {

            // show progress dialog
            mProgressDialog.show();
            // login registered user
            FirebaseAuthorisation firebaseAuthorisation = new FirebaseAuthorisation(StartActivity.this);
            firebaseAuthorisation.loginUser(email, password, mProgressDialog);
        } else {

            Toast.makeText(StartActivity.this, R.string.start_error_message, Toast.LENGTH_LONG).show();
        }
    }
}
