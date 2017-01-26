package com.project.getdataapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GetActivity";

    private static final int READ_FILE_REQUEST_CODE = 1101;

    UrlParameter[] mUrlParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String url = "https://dl.dropboxusercontent.com/u/15616335/HHP-2012.xlsx";

        Button getBtn = (Button) findViewById(R.id.get_button);
        getBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGet();
            }
        });

        Button readBtn = (Button) findViewById(R.id.read_button);
        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readSourceFromFile();
            }
        });

        checkPermission();
    }

    private void checkPermission() {
        if(!PermissionUtil.hasStoragePermissions(MainActivity.this)) {
            Intent intent = PermissionUtil.getStoragePermissionsIntent(MainActivity.this);
            startActivityForResult(intent, PermissionUtil.REQUEST_PERMISSION);
        }
    }

    private void startGet() {
        if(isConnected()) {
            mUrlParameters = new UrlParameter[5];
            UrlParameter par = new UrlParameter("xml", "台灣積體電路製造股份有限公司", "01");
            mUrlParameters[0] = par;
            UrlParameter par2 = new UrlParameter("xml", "椅王興業有限公司", "01");
            mUrlParameters[1] = par2;

            for(int i=0; i < mUrlParameters.length; i++) {
                if(mUrlParameters[i] != null && mUrlParameters[i].isValid()) {
                    GetAndWriteFileTask task = new GetAndWriteFileTask(mUrlParameters[i], mTaskCompleteCallback);
                    task.execute();
                } else {
                    Log.i(TAG, "parameter " + i + " is not valid");
                }
            }
        } else {
            Toast.makeText(this, "Please connect to network!", Toast.LENGTH_LONG);
        }
    }

    private GetAndWriteFileTask.Callback mTaskCompleteCallback = new GetAndWriteFileTask.Callback() {
        @Override
        public void onFinished(boolean result) {
            Log.i(TAG, "task completed, result: " + result);
        }
    };

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void readSourceFromFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_FILE_REQUEST_CODE);
    }

    private void parseFileToParameters(File file) {
        //Todo:
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if (requestCode == READ_FILE_REQUEST_CODE) {
                Uri uri;
                if (data != null) {
                    uri = data.getData();
                    Log.i(TAG, "Uri:" + uri.toString());
                    File file = FileHandler.getFileFromUri(this, uri);
                    Log.i(TAG, "File:" + file.toString());
                }
            } else if(requestCode == PermissionUtil.REQUEST_PERMISSION) {
                if (data != null) {
                    boolean result = data.getBooleanExtra(PermissionUtil.KEY_PERMISSION_RESULT, false);
                    Log.i(TAG, "request permission result : " + result);
                    if(!result) {
                        this.finish();
                    }
                }
            }
        }
    }
}
