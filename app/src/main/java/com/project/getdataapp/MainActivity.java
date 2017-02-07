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
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GetActivity";

    String mStartDescription;
    String mResultDescription;
    UrlParameter[] mUrlParameters;
    View mProgressView;
    int mTaskNumber;
    int mCompletedTaskNumber;
    boolean isGettingData;

    TextView mResultView;
    TextView mTemplateView;
    Button mReadButton;
    Button mGetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isGettingData = false;

        mProgressView = findViewById(R.id.progress_overlay);

        mReadButton = (Button) findViewById(R.id.read_button);
        mReadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readSourceFromFile();
            }
        });

        mGetButton = (Button) findViewById(R.id.get_button);
        mGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGettingData == false) {
                    startGet();
                } else {
                    Log.w(TAG, "Need wait for current getting data complete");
                }
            }
        });

        checkPermission();

        mResultView = (TextView) findViewById(R.id.result_view);
        mTemplateView = (TextView) findViewById(R.id.template_view);
        //For testing
        //getFakeData();
        mStartDescription = getString(R.string.read_file);
        mResultDescription = String.format(getString(R.string.get_from), FileHandler.getExternalSavePath(Constants.FOLDER_NAME));

        mResultView.setText(mStartDescription);
        mTemplateView.setText(getString(R.string.file_template));
    }

    private void checkPermission() {
        if(!PermissionUtil.hasStoragePermissions(MainActivity.this)) {
            Intent intent = PermissionUtil.getStoragePermissionsIntent(MainActivity.this);
            startActivityForResult(intent, Constants.REQUEST_PERMISSION_REQUEST_CODE);
        }
    }

    private synchronized void startGet() {
        if(isConnected()) {
            if(mUrlParameters != null) {
                isGettingData = true;
                mCompletedTaskNumber = 0;
                mTaskNumber = mUrlParameters.length;
                showProgress();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < mUrlParameters.length; i++) {
                            if (mUrlParameters[i] != null && mUrlParameters[i].isValid()) {
                                GetAndWriteFileTask task = new GetAndWriteFileTask(mUrlParameters[i], mTaskCompleteCallback);
                                try {
                                    task.execute();
                                } catch (Exception e) {
                                    Log.e(TAG, task.mTaskName + " task exception, " + e);
                                    task.removeCallBack();
                                    TaskCompleted();
                                }
                            } else {
                                Log.e(TAG, "parameter " + i + " is not valid");
                            }
                        }
                    }
                }).start();
            } else {
                Log.e(TAG, "startGet, no parameters found");
            }
        } else {
            Log.e(TAG, "startGet, no network");
            Toast.makeText(this, "Please connect to network!", Toast.LENGTH_LONG).show();
        }
    }

    private GetAndWriteFileTask.Callback mTaskCompleteCallback = new GetAndWriteFileTask.Callback() {
        @Override
        public void onFinished(boolean result, String name) {
            Log.i(TAG, name + " completed, result: " + result);
            TaskCompleted();
        }
    };

    private synchronized void TaskCompleted() {
        mCompletedTaskNumber++;
        verifyForAllTasks();
    }

    private boolean isAllTaskFinished() {
        return mCompletedTaskNumber == mTaskNumber;
    }

    private void verifyForAllTasks() {
        if(isAllTaskFinished()) {
            Log.i(TAG, "all tasks completed");
            isGettingData = false;
            dismissProgress();
        }
    }

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
        startActivityForResult(intent, Constants.READ_FILE_REQUEST_CODE);
    }

    private void parseFileToParameters(InputStream is) {
        try {
            InputStreamReader inputReader = new InputStreamReader(is, "big5");
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            ArrayList<String> list = new ArrayList();
            while ((line = bufReader.readLine()) != null) {
                list.add(line);
            }
            mUrlParameters = new UrlParameter[list.size()];
            for(int i = 0; i < list.size(); i++) {
                String[] splits = list.get(i).trim().split(",");
                if(splits.length == 3) {
                    mUrlParameters[i] = new UrlParameter(splits[0], splits[1], splits[2]);
                } else {
                    Log.e(TAG, "Wrong string input, line " + (i + 1));
                }
            }
            mResultView.setText(getString(R.string.analyze_complete) +
                    "\n\n" + mResultDescription
            );
        } catch (IOException e) {
            Log.e(TAG, "get data failed!!" + e);
            Toast.makeText(this, "檔案有問題", Toast.LENGTH_LONG).show();
        }
    }

    private void getFakeData() {
        Log.i(TAG, "Get default fake data");
        String filePath = "source.txt";
        try {
            InputStream is = getResources().getAssets().open(filePath);
            parseFileToParameters(is);
        } catch (IOException e) {
            Log.e(TAG, "get assets failed!!" + e);
        }
    }

    public void dismissProgress() {
        mProgressView.setVisibility(View.GONE);
        mResultView.setText(getString(R.string.get_complete) +
                "\n\n" +
                mResultDescription +
                "\n\n" +
                getString(R.string.re_read_file) + mStartDescription
        );
        mReadButton.setEnabled(true);
        mGetButton.setEnabled(true);
    }

    public void showProgress() {
        mProgressView.setVisibility(View.VISIBLE);

        String result = getString(R.string.get_start);

        mResultView.setText(result);
        mReadButton.setEnabled(false);
        mGetButton.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if (requestCode == Constants.READ_FILE_REQUEST_CODE) {
                Uri uri;
                if (data != null) {
                    uri = data.getData();
                    Log.i(TAG, "Uri:" + uri.toString());
                    File file = FileHandler.getFileFromUri(this, uri);
                    Log.i(TAG, "File:" + file.toString());
                    if(isTxtFile(file.getName())) {
                        mResultView.setText(getString(R.string.analyze_file));

                        try {
                            InputStream is = new FileInputStream(file);
                            parseFileToParameters(is);
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "FileNotFoundException, " + e);
                            Toast.makeText(this, "檔案有問題", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "不是txt檔案", Toast.LENGTH_LONG).show();
                    }
                }
            } else if(requestCode == Constants.REQUEST_PERMISSION_REQUEST_CODE) {
                if (data != null) {
                    boolean result = data.getBooleanExtra(Constants.EXTRA_PERMISSION_RESULT, false);
                    Log.i(TAG, "request permission result : " + result);
                    if(!result) {
                        this.finish();
                    }
                }
            }
        }
    }

    private boolean isTxtFile(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        if(ext.equalsIgnoreCase("txt")) {
            return true;
        }
        return false;
    }
}
