package com.juxtarem.android.juxtarem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.juxtarem.android.juxtarem.utilities.NetworkUtils;
import com.juxtarem.android.juxtarem.utilities.json.JsonBuilder;

import java.io.IOException;
import java.net.URL;

public class SignupActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>  {
    private static final String TAG = "SignupActivity";
    private static final int SIGN_UP_TASK_LOADER = 101;
    private static final String NAME = "name";
    private static final String MAIL = "mail";
    private static final String PASS = "pass";

    EditText _nameText;
    EditText _emailText;
    EditText _passwordText;
    Button _signupButton;
    TextView _loginLink;

    private ProgressBar _progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        _nameText = findViewById(R.id.input_name);
        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _loginLink = findViewById(R.id.link_login);
        _signupButton = findViewById(R.id.btn_signup);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

        _progressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed("Validation failed");
            return;
        }

        _signupButton.setEnabled(false);

        _progressBar.setVisibility(View.VISIBLE);
        _progressBar.setIndeterminate(true);

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        //signup logic here.
        URL newAccountCreationURL = NetworkUtils.buildCreateAccountUrl(name, email, password);
        Bundle taskBundle =  new Bundle();
        taskBundle.putString(NAME, name);
        taskBundle.putString(MAIL, email);
        taskBundle.putString(PASS, password);


        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> taskLoader = loaderManager.getLoader(SIGN_UP_TASK_LOADER);
        if (taskLoader == null) {
            loaderManager.initLoader(SIGN_UP_TASK_LOADER, taskBundle, this);
        } else {
            loaderManager.restartLoader(SIGN_UP_TASK_LOADER, taskBundle, this);
        }
    }


    public void onSignupSuccess(String data) {
        _signupButton.setEnabled(true);

        //Won't use the result as this means it worked if it got to this step
//        Intent resultIntent = new Intent();
//        resultIntent.putExtra("data", data);
//        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void onSignupFailed(String message) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        _passwordText.setError(null);
        if (password.isEmpty() || password.length() < 8) {
            _passwordText.setError("at least 8 characters long");
            valid = false;
        } else {
            int nonAlphabeticCharactersNo = 0;
            for (int i = 0; i < password.length(); i++) {
              if (!Character.isLetter(password.indexOf(i))) {
                    nonAlphabeticCharactersNo++;
              }
            if (nonAlphabeticCharactersNo == 0) {
                _passwordText.setError("at least one non alphabetic character");
                valid = false;
            }
          }
        }

        return valid;
    }

    //methods for LoaderCallback Interface
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            String singupResult;

            @Override
            protected void onStartLoading() {
                /* If no arguments were passed, we don't have a query to perform. Simply return. */
                if (args == null) {
                    return;
                }

                /*
                 * If taskResult is not null, deliver that result. Otherwise, force a load
                 * If we already have cached results, just deliver them now. If we don't have any
                 * cached results, force a load.
                 */
                if (singupResult != null) {
                    deliverResult(singupResult);
                } else {
                    //this else needs to stay here to force the load if it is not there
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {
                /* Extract the url from the args using our constant */
                String name = args.getString(NAME);
                String mail = args.getString(MAIL);
                String pass = args.getString(PASS);

                /* If the user didn't enter anything, there's nothing to search for */
                if (mail == null || TextUtils.isEmpty(mail)) {
                    return null;
                }

                /* Parse the URL from the passed in String and perform the search */
                String singupResult = NetworkUtils.getResponseForPostSignUpRequest(NetworkUtils.buildPostRequestCreateUserUrl(),
                       JsonBuilder.buildGetUserPointsResponse(name, mail, pass));
                return singupResult;
            }


            @Override
            protected void onReset() {
                super.onReset();
                /* If no arguments were passed, we don't have a query to perform. Simply return. */
                if (args == null) {
                    return;
                }

                /*
                 * If taskResult is not null, deliver that result. Otherwise, force a load
                 * If we already have cached results, just deliver them now. If we don't have any
                 * cached results, force a load.
                 */
                if (singupResult != null) {
                    deliverResult(singupResult);
                } else {
                    forceLoad();
                }
            }

            @Override
            public void deliverResult(String cretedUserJson) {
                singupResult = cretedUserJson;
                super.deliverResult(singupResult);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader,final String data) {

        Toast.makeText(getBaseContext(), "Signup result " +  data, Toast.LENGTH_LONG).show();
        Log.d(SignupActivity.class.getCanonicalName(), "SignUpResult: " + data);

        _progressBar.setVisibility(View.INVISIBLE);
        // On complete call either onSignupSuccess or onSignupFailed

        if (data != null) {
            onSignupSuccess(data);
        } else {
            onSignupFailed(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        /*
         * Not used now but we are required to Override it to implement the LoaderCallbacks<String> interface
         */
    }
}