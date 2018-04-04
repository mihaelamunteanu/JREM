package com.juxtarem.android.juxtarem.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

/**
 * Created by mihaela on 3/1/2018.
 *
 */

public class NetworkUtils {
    final static String JUXTAREM_BASE_URL = "http://10.0.2.2:8080/juxtarem-1.0.0/rest/juxtaremservice";

    final static String TASKS = "tasks";
    final static String TASK = "task";
    final static String USERS = "users";
    final static String USER = "user";

    final static String CREATE_USER = "createuser";
    /**
     * Builds the URL used to get a task.
     *
     * @param userId The user to get a task for.
     * @return The URL to use to query the server.
     */
    public static URL buildGetTaskForUserUrl(long userId) {
        Uri builtUri = Uri.parse(JUXTAREM_BASE_URL).buildUpon()
                .appendPath(TASKS).appendPath(String.valueOf(userId))
                .build();

        return buildUrlFromUri(builtUri, 0);
    }

    /**
     * Builds the URL used to get a task.
     *
     * @return The URL to use to query the server.
     */
    public static URL buildPostRequestCreateUserUrl() {
        Uri builtUri = Uri.parse(JUXTAREM_BASE_URL).buildUpon()
                .appendPath(CREATE_USER)
                .build();

        return buildUrlFromUri(builtUri, 0);
    }

    /**
     * Builds the URL used to create a new account.
     *
     * @param name The user to get a task for.
     * @param mail The user to get a task for.
     * @param password The user to get a task for.
     * @return The URL to use to query the server.
     */
    public static URL buildCreateAccountUrl(String name, String mail, String password) {
        byte[] passBase64 = android.util.Base64.encode(password.getBytes(), 1);
        Uri builtUri = Uri.parse(JUXTAREM_BASE_URL).buildUpon()
                .appendPath(CREATE_USER)
                .appendPath(String.valueOf(name))
                .appendPath(String.valueOf(mail))
                .appendPath(String.valueOf(passBase64))
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

    private static HttpURLConnection createPostRequestConnection(URL url) throws IOException {
        //TODO refactor to generalize it better and use OkHttp
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //TODO see what to replace the user agent and make sure it does not open new threads
        String USER_AGENT = System.getProperty("http.agent");
        //add reuqest header

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("http.protocol.expect-continue", "false");
        con.setRequestProperty("charset", "utf-8");

        // Send post request
        con.setDoOutput(true);

        return con;
    }

    public static String getResponseForPostSignUpRequest(URL url, String urlParameters) {
        try {
            HttpURLConnection con = createPostRequestConnection(url);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            Log.i(NetworkUtils.class.getCanonicalName(), "\nSending 'POST' request to URL : " + url +
                    " and parameters " + urlParameters + " and response code " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.i(NetworkUtils.class.getCanonicalName(), response.toString());
            return response.toString();

            //TODO handle exceptions
            //TODO close the in also when there is an error received, in the exceptions
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
