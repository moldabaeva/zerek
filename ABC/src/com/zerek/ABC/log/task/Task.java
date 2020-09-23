package com.zerek.ABC.log.task;


import android.app.Activity;
import com.zerek.ABC.MainActivity;
import com.zerek.ABC.R;
import com.zerek.ABC.log.WebLoader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// Main class
public abstract class Task implements Runnable {
    public final String sUrl;
    public WebLoader parent;
    private final boolean bShowErr;

    // For messages
    protected Activity act;


    public Task(String sUrl) {
        this(sUrl, true);
    }

    public Task(String sUrl, boolean bShowErr) {
        this.sUrl = sUrl;
        this.bShowErr = bShowErr;
        MainActivity.debug(sUrl);
    }

    public void doHttpLoad(Activity activity) {
        this.act = activity;
        HttpURLConnection connection = null;
        try {
            // URL
            connection = (HttpURLConnection) new URL(sUrl).openConnection();

            //Unnecessary
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.connect();

            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK ) {
                // Convert to data
                parse(connection.getInputStream());

                // Result
                activity.runOnUiThread(this);
            } else
                showErrInfo(activity, activity.getResources().getString(R.string.Error_Connect));

        } catch (Exception localException) {
            String sErr = localException.toString() + " " + localException.getMessage();
            showErrInfo(activity, sErr);
        }

        // Clear data
        if (connection != null)
            connection.disconnect();
    }

    // Show error in Toast
    public void showErrInfo(final Activity act, final String sMes) {
        if (bShowErr)
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.showToast(act, sMes);
                }
            });
    }

    // Convert to necessary data
    protected abstract void parse(InputStream inputStream);
}