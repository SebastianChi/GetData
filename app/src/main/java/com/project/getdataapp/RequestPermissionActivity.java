package com.project.getdataapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by billy_chi on 2017/1/26.
 */

public class RequestPermissionActivity extends Activity {
    private static final String TAG = "GetDataPermission";

    private Handler mMainHandler;

    private int mRequestCode;
    private String[] mRequiredPermissions;
    private Context mContext;

    private RequestPermissionsCallback mRequestPermissionsCallback;

    private AlertDialog mDeclinedPermissionDialog;
    private AlertDialog mRequestPermissionRationalDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        parseIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        mContext = this;

        RequestPermissionTask requestPermissionTask = new RequestPermissionTask();
        requestPermissionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void parseIntent() {
        Log.i(TAG, "parseIntent");
        Intent intent = getIntent();
        if(intent == null) {
            return;
        }

        if(intent.hasExtra(PermissionUtil.EXTRA_REQUIRED_PERMISSIONS)) {
            mRequiredPermissions = intent.getStringArrayExtra(PermissionUtil.EXTRA_REQUIRED_PERMISSIONS);
        }

        if(intent.hasExtra(PermissionUtil.EXTRA_PERMISSIONS_TYPE)) {
            mRequestCode = intent.getIntExtra(PermissionUtil.EXTRA_PERMISSIONS_TYPE, 0);
        }
    }

    private void setFinishResult(int resultCode, boolean result) {
        Log.i(TAG, "setFinishResult, resultCode:" + resultCode + ", result:" + result);
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PermissionUtil.KEY_PERMISSION_RESULT, result);
        setResult(resultCode, returnIntent);
        finish();
    }

    private class RequestPermissionTask extends AsyncTask<Void, Void, Void> {
        private String TAG = "RequestPermissionTask";

        public RequestPermissionTask() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            requestPermissionImp();
            return null;
        }

        private void requestPermissionImp() {
            if(mRequiredPermissions != null && mRequiredPermissions.length > 0) {
                Log.i(TAG, "requestPermissionImp length: " + mRequiredPermissions.length);
                if (mRequestPermissionsCallback == null) {
                    mRequestPermissionsCallback = new RequestPermissionsCallback(mRequiredPermissions, mRequestCode);
                }
                mRequestPermissionsCallback.call((Activity) mContext);
            } else {
                Log.i(TAG, "No requiredPermissions need to check.");
                setFinishResult(RESULT_OK, false);
            }
        }
    }

    private class RequestPermissionsCallback implements ActivityCompat.OnRequestPermissionsResultCallback {
        private final String[] mPermissions;
        private final int mRequestCode;
        private final boolean mShouldRequestPermission;//allow consuming code to check silently without prompting user if set false

        public RequestPermissionsCallback(String[] permissions, int requestCode) {
            mPermissions = permissions;
            mRequestCode = requestCode;
            mShouldRequestPermission = true;
        }

        public void call(@NonNull Activity activity){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                onHasPermission();
            }
            else {
                if (hasPermissions(activity)) {
                    onHasPermission();
                } else if (mShouldRequestPermission){
                    if (PermissionUtil.shouldShowRequestPermissionRationale(activity, mPermissions)){
                        onShowRequestPermissionRationale();
                    } else {
                        ActivityCompat.requestPermissions(activity, mPermissions, mRequestCode);
                    }
                }
            }
        }

        private void onHasPermission() {
            Log.i(TAG, "[onHasPermission] permissions all granted.");
            setFinishResult(RESULT_OK, true);
        }

        private void onDeclinedPermission() {
            Log.i(TAG, "[onDeclinedPermission] permissions denied.");

            if (mMainHandler == null) {
                mMainHandler =  new Handler(Looper.getMainLooper());
            }

            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    showUserDeclinedPermissionDialog((Activity) mContext);
                }
            });

        }

        private void onShowRequestPermissionRationale() {
            Log.i(TAG, "[onShowRequestPermissionRationale]");
            if (mMainHandler == null) {
                mMainHandler =  new Handler(Looper.getMainLooper());
            }
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    showRequestPermissionRationaleDialog((Activity) mContext);
                }
            });
        }

        private boolean hasPermissions(@NonNull Context context){
            if (mPermissions == null || mPermissions.length == 0) {
                return false;
            } else {
                for (final String permission : mPermissions){
                    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
                return true;
            }
        }

        private boolean verifyPermissions(int[] grantResults) {
            // At least one result must be checked.
            if(grantResults.length < 1){
                return false;
            }

            // Verify that each required permission has been granted, otherwise return false.
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            Log.i(TAG, "[onRequestPermissionsResult] requestCode:" + requestCode);
            if (this.mRequestCode == requestCode) {

                if(verifyPermissions(grantResults)) {
                    //permissions granted
                    onHasPermission();
                } else {
                    if(!PermissionUtil.shouldShowRequestPermissionRationale((Activity) mContext, mPermissions)) {
                        onDeclinedPermission();
                    } else {
                        setFinishResult(RESULT_OK, false);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mRequestPermissionsCallback != null) {
            mRequestPermissionsCallback.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showUserDeclinedPermissionDialog(final Activity activity){
        Log.i(TAG, "[showUserDeclinedPermissionDialog]");
        if (mDeclinedPermissionDialog == null) {
            MyDialogData data = new MyDialogData();
            String permissionCategory;
            switch (mRequestCode) {
                case PermissionUtil.REQUEST_EXTERNALSTORAGE_PERMISSION:
                default:
                    permissionCategory = activity.getResources().getString(R.string.permission_storage);
                    break;
            }

            //data.setTitle(activity.getResources().getString(R.string.app_name));
            String msg = String.format(activity.getResources().getString(R.string.declined_permission_message), permissionCategory);

            data.setMessage(msg);
            data.setPositiveButtonText(getString(R.string.go_to_settings));
            data.setNegativeButtonText(getString(R.string.not_now));

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            goToSettings(activity);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // do nothing
                            dialog.dismiss();
                            setFinishResult(RESULT_OK, false);
                            break;
                    }
                }
            };

            DialogInterface.OnCancelListener dialogCancelListener = new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    setFinishResult(RESULT_OK, false);
                }
            };
            mDeclinedPermissionDialog = MyDialog.getPermissionDialog(mContext, data, dialogClickListener, dialogCancelListener);
        }

        showDialog(mDeclinedPermissionDialog);
    }

    private void showRequestPermissionRationaleDialog(final Activity activity) {
        Log.i(TAG, "[showRequestPermissionRationaleDialog]");
        if(mRequestPermissionRationalDialog == null) {
            MyDialogData data = new MyDialogData();
            String rationalMessage = activity.getResources().getString(R.string.permission_storage_rationale_message);

            data.setMessage(rationalMessage);
            data.setPositiveButtonText(getString(R.string.va_ok));
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            ActivityCompat.requestPermissions(activity, mRequiredPermissions, mRequestCode);
                            break;
                    }
                }
            };

            data.setCancelable(false);

            DialogInterface.OnCancelListener dialogCancelListener = new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    setFinishResult(RESULT_OK, false);
                }
            };
            mRequestPermissionRationalDialog = MyDialog.getPermissionDialog(mContext, data, dialogClickListener, dialogCancelListener);
        }
        showDialog(mRequestPermissionRationalDialog);
    }

    private void showDialog(AlertDialog dialog) {
        try {
            if(!isFinishing() && dialog != null && !dialog.isShowing()) {
                dialog.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "[showDialog] exception!!");
            Log.e(TAG, "", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PermissionUtil.REQUEST_PERMISSION) {
            Log.i(TAG, "[onActivityResult] REQUEST_PERMISSION");

            if (hasRequestPermissionGranted()) {
                setFinishResult(RESULT_OK, true);
            } else {
                Log.e(TAG, "Permissions weren't granted");
                setFinishResult(RESULT_OK, false);
            }
        }
    }

    private void goToSettings(final Activity activity) {
        try {
            //Open the specific App Info page:
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, PermissionUtil.REQUEST_PERMISSION);

        } catch ( ActivityNotFoundException e ) {

            //Open the generic Apps page:
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            activity.startActivityForResult(intent, PermissionUtil.REQUEST_PERMISSION);
        }
    }

    private boolean hasRequestPermissionGranted() {
        boolean result = false;
        switch (mRequestCode) {
            case PermissionUtil.REQUEST_EXTERNALSTORAGE_PERMISSION:
                result = PermissionUtil.hasStoragePermissions(this);
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        if(!hasRequestPermissionGranted()) {
            setFinishResult(RESULT_OK, false);
        } else {
            super.onBackPressed();
        }
    }
}
