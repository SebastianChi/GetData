package com.project.getdataapp;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.http.protocol.HTTP.USER_AGENT;

/**
 * Created by billy_chi on 2017/1/26.
 */

public class GetAndWriteFileTask extends AsyncTask<String, String, Boolean> {
    private static final String TAG = "GetAndWriteTask";

    private static final String URL_TEMPLATE =
            "http://data.gcis.nat.gov.tw/od/data/api/6BBA2268-1367-4B42-9CCA-BC17499EBE8C?$format=%s" +
            "&$filter=Company_Name like %s" +
            " and Company_Status eq %s";
    private static final String FOLDER_NAME = "/gcis_data";
    private UrlParameter mParameter;

    private String mTargetUrl;
    public String mTaskName;

    // Callback when task finished
    public interface Callback {
        void onFinished(boolean result, String name);
    }
    public Callback mCallback;

    public GetAndWriteFileTask(UrlParameter parameter, Callback callback) {
        mParameter = parameter;
        mCallback = callback;
        if(mParameter != null) {
            setupUrl();
        }
    }

    private void setupUrl() {
        try {
            String url = String.format(URL_TEMPLATE, mParameter.mFormat, mParameter.mCompanyName, mParameter.mStatus);
            mTargetUrl = url.replace(" ", "%20");
            mTaskName = mParameter.mCompanyName;
        } catch (Exception e) {
            Log.e(TAG, "Exception, setupUrl failed: " + e.getStackTrace());
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = false;
        Log.i(TAG, "start getData for " + mTaskName);
        byte[] bytes = getDataFromUrl();
        Log.i(TAG, "getData for " + mTaskName + " completed");
        if (isExternalStorageWritable()) {
            result = writeToExternalPublicFile(bytes);
        } else {
            Log.i(TAG, "[doInBackground] unable to get external storage");
            //Todo: write to internal
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if(values != null && values.length > 0) {
            Log.i(TAG, "[onProgressUpdate] " + values[0]);
            super.onProgressUpdate(values);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(mCallback != null) {
            Log.i(TAG, "[onPostExecute] " + mTaskName);
            mCallback.onFinished(result, mTaskName);
        } else {
            Log.w(TAG, "[onPostExecute] " + mTaskName + " callback is null");
        }
    }

    private byte[] getDataFromUrl() {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(mTargetUrl);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);

        try {
            HttpResponse response = client.execute(request);

            System.out.println("Response Code : "
                    + response.getStatusLine().getStatusCode());

            InputStream inputStream = response.getEntity().getContent();
            return IOUtils.toByteArray(inputStream);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
        }
        return null;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private boolean writeToExternalPublicFile(byte[] bytes) {
        boolean writeResult = false;
        if(bytes != null && bytes.length > 0) {
            try {
                File savePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + FOLDER_NAME);
                if (!savePath.exists()) {
                    Log.i(TAG, "create path : " + savePath.mkdirs());
                }

                File file = new File(savePath, mTaskName + ".txt");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.close();
                Log.i(TAG, mTaskName + "write file complete");
                writeResult = true;
            } catch (IOException e) {
                Log.e(TAG, "Exception, file write failed: " + e.getStackTrace());
            }
        } else {
            Log.e(TAG, "data is null");
        }
        return writeResult;
    }

    public void removeCallBack() {
        mCallback = null;
    }
}
