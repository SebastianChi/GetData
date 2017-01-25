package com.project.getdataapp;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static org.apache.http.protocol.HTTP.USER_AGENT;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GetActivity";
    private static final String URL_TEMPLATE = "http://data.gcis.nat.gov.tw/od/data/api/6BBA2268-1367-4B42-9CCA-BC17499EBE8C?$format=%s" +
            "&$filter=Company_Name like %s" +
            " and Company_Status eq %s";

    /** {format}:資料格式json、xml。
     *  {Company_Name}:公司名稱關鍵字。
     *  {Company_Status}:公司狀態代碼。
     **/
    static String mFormat = "xml";
    static String mCompanyName = "台灣積體電路製造股份有限公司";
    static String mCompanyStatus = "01";
    // http://data.gcis.nat.gov.tw/od/data/api/6BBA2268-1367-4B42-9CCA-BC17499EBE8C?$format=xml&$filter=Company_Name like 台灣積體電路製造股份有限公司 and Company_Status eq 01

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String url = setupUrl();

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGet(url);
            }
        });
    }

    private String setupUrl() {
        String url = String.format(URL_TEMPLATE, mFormat, mCompanyName, mCompanyStatus);
        url = url.replace(" ", "%20");
        return url;
    }

    private void startGet(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(url);

                // add request header
                request.addHeader("User-Agent", USER_AGENT);

                try {
                    HttpResponse response = client.execute(request);

                    System.out.println("Response Code : "
                            + response.getStatusLine().getStatusCode());

                    InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent());
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    StringBuffer resultSb = new StringBuffer();
                    String receiveString = "";

                    while ((receiveString = reader.readLine()) != null) {
                        resultSb.append(receiveString);
                    }
                    String result = resultSb.toString();

                    if(isExternalStorageWritable()) {
                        //Todo: write to external
                        File file = getExternalStorageDir("getData");
                        if(file != null) {
                            writeToFile(file.getPath(), result, getApplicationContext());
                        }
                    } else {
                        //Todo: write to internal
                    }

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found: " + e.toString());
                } catch (IOException e) {
                    Log.e(TAG, "Can not read file: " + e.toString());
                }
            }
        }).start();

    }

    private void readSourceFromExternalFile() {
        //Todo:
    }

    private void writeToFile(String path, String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(path, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private File getExternalStorageDir(String dirName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), dirName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

}
