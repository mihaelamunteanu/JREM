package com.juxtarem.android.juxtarem.utilities;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by mihaela on 3/1/2018.
 */

public class JsonUtils {
    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     * <p/>
     * Later on, we'll be parsing the JSON into structured data within the
     * getFullWeatherDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, we just convert the JSON into human-readable strings.
     *
     * @param taskJsonStr JSON response from server
     *
     * @return Array of Strings describing weather data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues getTaskValuesFromJson(String taskJsonStr)
            throws JSONException {

        JSONObject taskJson = new JSONObject(taskJsonStr);
        ContentValues taskValues = new ContentValues();

        /* Is there an error? */
        if (taskJson.has("task")) {
            String taskDetail = taskJson.getString("task");
            taskValues.put("taskDetail", taskDetail);
        }

        if (taskJson.has("point")) {
            String points = taskJson.getString("point");
            taskValues.put("points", points);
        }

        //TODO refactor static final, use points as int

        return taskValues;
    }
}
