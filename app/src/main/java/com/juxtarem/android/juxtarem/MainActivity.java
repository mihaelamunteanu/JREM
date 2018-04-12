package com.juxtarem.android.juxtarem;

import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.juxtarem.android.juxtarem.utilities.Commons;
import com.juxtarem.android.juxtarem.utilities.Constants;
import com.juxtarem.android.juxtarem.utilities.json.JSONUtils;
import com.juxtarem.android.juxtarem.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private static final String TASK_URL = "TASK_URL";
    private static final String TASK_TEXT = "TASK_TEXT";
    private static final int TASK_LOADER = 100;
    private TextView taskTextView;
    private Button taskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO check if user is connected; initially we consider he is not
        if (AccessToken.getCurrentAccessToken() == null) {
//            Intent intent = new Intent(this,  LoginActivity.class);
            Intent intent = new Intent(this,  FacebookLoginActivity.class);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        }

        taskButton = (Button) findViewById(R.id.task_button);
        taskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNewTask(1L);
            }
        });

        taskTextView = (TextView) findViewById(R.id.task_tv);
        if (savedInstanceState != null) {
            String taskText = savedInstanceState.getString(TASK_TEXT);
            taskTextView.setText(taskText);
        }
    }

    private void getNewTask(long userId) {
        URL taskQueryURL = NetworkUtils.buildGetTaskForUserUrl(userId);
        Bundle taskBundle =  new Bundle();
        taskBundle.putString(TASK_URL, taskQueryURL.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> taskLoader = loaderManager.getLoader(TASK_LOADER);
        if (taskLoader == null) {
            loaderManager.initLoader(TASK_LOADER, taskBundle, this);
        } else {
            loaderManager.restartLoader(TASK_LOADER, taskBundle, this);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            String taskResult;

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
                if (taskResult != null) {
                    deliverResult(taskResult);
                } else {
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {

                /* Extract the search query from the args using our constant */
                String searchQueryUrlString = args.getString(TASK_URL);

                /* If the user didn't enter anything, there's nothing to search for */
                if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                    return null;
                }

                /* Parse the URL from the passed in String and perform the search */
                try {
                    URL taskUrl = new URL(searchQueryUrlString);
                    String taskSearchResults = NetworkUtils.getResponseFromHttpUrl(taskUrl);
                    return taskSearchResults;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
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
                if (taskResult != null) {
                    deliverResult(taskResult);
                } else {
                    forceLoad();
                }
            }

            @Override
            public void deliverResult(String taskJson) {
                taskResult = taskJson;
                super.deliverResult(taskResult);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        //TODO add indicator and error message
        try {
            taskTextView.setText((String)JSONUtils.getTaskValuesFromJson(data).get("task"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        /*
         * Not used now but we are required to Override it to implement the LoaderCallbacks<String> interface
         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //String lost when a rotation is done
        String currentTaskDetails = taskTextView.getText().toString();
        outState.putString(TASK_TEXT, currentTaskDetails);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_share:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            case R.id.action_signout_tasks:
                Commons.facebookLogOut(MainActivity.this);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
