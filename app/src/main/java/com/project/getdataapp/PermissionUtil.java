package com.project.getdataapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by billy_chi on 2017/1/26.
 */

public class PermissionUtil {
    private static final String TAG = "GetDataPermissionUtil";

    /**
     * Check should show app permission rationale or not
     *
     * @param activity Caller's activity
     * @param requiredPermissions Desired permissions
     * @return Return checking result
     */
    public static boolean shouldShowRequestPermissionRationale(Activity activity, String [] requiredPermissions) {
        Log.i(TAG, "shouldShowRequestPermissionRationale");
        if (requiredPermissions != null) {
            for (int i = 0; i < requiredPermissions.length; i++) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requiredPermissions[i])) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Check desired permission is granted or not
     *
     * @param context Context
     * @param permission Single desired permission
     * @return Return checking result
     */
    public static boolean checkSelfPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check is android sdk M or not
     *
     * @return Return is android M or not
     */
    public static boolean isM60() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check has storage permissions or not
     *
     * @param context Context
     * @return Return has storage permissions or not
     */
    public static boolean hasStoragePermissions(Context context) {
        return checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }


    /**
     * Get read/write storage permission intent
     *
     * @param context Context
     * @return Return read/write storage permission intent
     */
    public static Intent getStoragePermissionsIntent(Context context) {
        Intent intent = new Intent(context, RequestPermissionActivity.class);

        intent.putExtra(Constants.EXTRA_REQUIRED_PERMISSIONS, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        });
        intent.putExtra(Constants.EXTRA_PERMISSIONS_TYPE, Constants.REQUEST_EXTERNALSTORAGE_PERMISSION);
        return intent;
    }
}
