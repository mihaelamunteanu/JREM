package com.juxtarem.android.juxtarem;

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

import com.juxtarem.android.juxtarem.utilities.JsonUtils;
import com.juxtarem.android.juxtarem.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

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
            taskTextView.setText((String)JsonUtils.getTaskValuesFromJson(data).get("taskDetail"));
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
}
