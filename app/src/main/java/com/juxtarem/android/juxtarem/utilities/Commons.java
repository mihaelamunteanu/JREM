package com.juxtarem.android.juxtarem.utilities;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.juxtarem.android.juxtarem.FacebookLoginActivity;
import com.juxtarem.android.juxtarem.R;

import org.json.JSONException;

/**
 * Created by mihae on 4/10/2018.
 */

public class Commons {
    public static final String GRAPH_PATH = "me/permissions";
    public static final String SUCCESS = "success";
    public static final String EMAIL = "email";

    public static void facebookLogOut(final Context facebookLoginContext) {
        GraphRequest.Callback callback = new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                try {
                    if(response.getError() != null) {
                        Toast.makeText(
                                facebookLoginContext,
                                facebookLoginContext.getResources().getString(
                                        R.string.failed_to_deauth,
                                        response.toString()),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    else if (response.getJSONObject().getBoolean(SUCCESS)) {
                        LoginManager.getInstance().logOut();
                        // updateUI();?
                    }
                } catch (JSONException ex) { /* no op */ }
            }
        };
        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(),
                GRAPH_PATH, new Bundle(), HttpMethod.DELETE, callback);
        request.executeAsync();
    }
}
