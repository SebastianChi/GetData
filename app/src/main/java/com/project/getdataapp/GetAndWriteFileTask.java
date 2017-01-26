package com.project.getdataapp;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

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

public class GetAndWriteFileTask extends AsyncTask<Void, String, Boolean> {
    private static final String TAG = "GetAndWriteTask";

    private static final String URL_TEMPLATE =
            "http://data.gcis.nat.gov.tw/od/data/api/6BBA2268-1367-4B42-9CCA-BC17499EBE8C?$format=%s" +
            "&$filter=Company_Name like %s" +
            " and Company_Status eq %s";
    private static final String FOLDER_NAME = "/gcis_data";
    private UrlParameter mParameter;

    private String mTargetUrl;

    // Callback when task finished
    public interface Callback {
        void onFinished(boolean result);
    }
    public Callback mCallback;

    public GetAndWriteFileTask(UrlParameter parameter, Callback callback) {
        mParameter = parameter;
        mCallback = callback;
        setupUrl();
    }

    private void setupUrl() {
        try {
            String url = String.format(URL_TEMPLATE, mParameter.mFormat, mParameter.mCompanyName, mParameter.mStatus);
            mTargetUrl = url.replace(" ", "%20");
        } catch (Exception e) {
            Log.e(TAG, "Exception, setupUrl failed: " + e.getStackTrace());
        }
    }

    @Override
    protected void onPreExecute() {
        Log.i(TAG, "[onPreExecute]");
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = false;
        byte[] bytes = getDataFromUrl();
        publishProgress("getData");
        if (isExternalStorageWritable()) {
            publishProgress("saveData");
            result = writeToExternalPublicFile(bytes);
        } else {
            //Todo: write to internal
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Log.i(TAG, "[onProgressUpdate] " + values);
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.i(TAG, "[onPostExecute]");
        if(mCallback != null) {
            mCallback.onFinished(result);
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
            Log.e(TAG, "Can not read file: " + e.toString());
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

                File file = new File(savePath, mParameter.mCompanyName + ".txt");
                FileOutputStream fos = new FileOutputStream(file);
                Log.i(TAG, "write file");
                fos.write(bytes);
                fos.close();
                Log.i(TAG, "write file complete");
                writeResult = true;
            } catch (IOException e) {
                Log.e(TAG, "Exception, file write failed: " + e.getStackTrace());
            }
        } else {
            Log.e(TAG, "data is null");
        }
        return writeResult;
    }
}
