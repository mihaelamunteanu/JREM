package com.juxtarem.android.juxtarem.utilities.json;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by mihaela on 3/1/2018.
 */

public class JSONUtils {
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

    /**
     * Method that builds a JSON string with a given code an already built values.
     * e.g. code = Exception
     * values:
     *        value[0] = "code" : "001"
     *        value[1] = "text" : "The ids are expected to be long values"
     *
     * The returned String will be:
     * "Exception" : {"code" : "001","text" : "The ids are expected to be long values"}
     *
     * if the code is null then no root element is added
     * e.g. code = null
     *      values[0] = "type" : "user"
     *      values[1] = "mail" : "john@gmail.com"
     * Result {"type" : "user", "mail": "john@gmail.com"}
     *
     * @param code - the value from the left side of the JSON
     * @param values - the complex values from the right side
     * @return the String containing the JSON built from the given input
     */
    public static String buildJSONParent(String code, String...values) {

        StringBuffer jsonStringBuffer = new StringBuffer();
        //add something like "Exception" :
        if (code != null) {
            jsonStringBuffer.append("\"" + code + "\"");
            jsonStringBuffer.append(" : " );
        }

        if (values != null && values.length > 0) {
            //add something like { "code" : "001", "text" : "Unexpected id" }
            jsonStringBuffer.append("{");

            int numberOfValues = values.length;
            for (int i = 0; i < numberOfValues; i++) {
                jsonStringBuffer.append(values[i]);
                if (i < numberOfValues - 1) {
                    //this is not the last value in the list
                    jsonStringBuffer.append(",");
                }
            }
            jsonStringBuffer.append("}");
        } else {
            //result would be "Exception" : null
            jsonStringBuffer.append("null");
        }

        return jsonStringBuffer.toString();
    }

    /**
     * Method to build JSON leaves:
     * e.g. code = text
     *      pairValues[1] = Id unexpected
     * Result: "code" : "Id unexpected"
     *
     * or
     *
     *      code = books
     *      pairValues[1] = JungleBook
     *      pairValues[2] = Leila
     * Result: "books" : ["JungleBook", "Leila"]
     *
     * if no value is in pairValues then the reuslt is "value_of_code" : null.
     *
     * @param code
     * @param pairValues
     * @return
     */
    public static String buildJSONPair(String code, String...pairValues) {
        StringBuffer jsonStringBuffer = new StringBuffer();
        //add something like "code" :
        if (code != null) {
            jsonStringBuffer.append("\"" + code + "\"");
            jsonStringBuffer.append(" : " );
        }

        if (pairValues != null && pairValues.length > 0) {
            int numberOfValues = pairValues.length;
            if (numberOfValues == 1) {
                jsonStringBuffer.append("\"" + pairValues[0] + "\"");
            } else {
                jsonStringBuffer.append("[");
                for (int i = 0; i < numberOfValues; i++) {
                    jsonStringBuffer.append("\"" + pairValues[i] + "\"");
                    if (i < numberOfValues - 1) {
                        //this is not the last value in the list
                        jsonStringBuffer.append(",");
                    }
                }
                jsonStringBuffer.append("]");
            }
        } else {
            //result would be "code" : null
            jsonStringBuffer.append("null");
        }

        return jsonStringBuffer.toString();
    }

}
