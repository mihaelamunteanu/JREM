package com.juxtarem.android.juxtarem.utilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by mihaela on 3/1/2018.
 */

public class NetworkUtils {
    final static String JUXTAREM_BASE_URL = "http://10.0.2.2:8080/juxtarem-1.0.0/rest/juxtaremservice";

    final static String TASKS = "tasks";
    final static String TASK = "task";
    final static String USERS = "users";
    final static String USER = "user";

    /**
     * Builds the URL used to get a task.
     *
     * @param userId The user to get a task for.
     * @return The URL to use to query the weather server.
     */
    public static URL buildGetTaskForUserUrl(long userId) {
        Uri builtUri = Uri.parse(JUXTAREM_BASE_URL).buildUpon()
                .appendPath(TASK).appendPath(String.valueOf(userId))
                .build();

        return buildUrlFromUri(builtUri, 0);
    }

    private static URL buildUrlFromUri(Uri uri, int lastTaskId) {
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            //TODO handle Response code
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
