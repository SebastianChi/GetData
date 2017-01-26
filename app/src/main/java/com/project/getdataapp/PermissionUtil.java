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

    // For RequestPermissionActivity
    public static final int REQUEST_EXTERNALSTORAGE_PERMISSION = 1;
    public static String EXTRA_REQUIRED_PERMISSIONS = "required_permissions";
    public static String EXTRA_PERMISSIONS_TYPE = "permissions_type";

    public static int REQUEST_PERMISSION = 1002;
    public static String KEY_PERMISSION_RESULT = "key_permission_result";

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

    public static boolean checkSelfPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isM60() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasStoragePermissions(Context context) {
        return checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static Intent getStoragePermissionsIntent(Context context) {
        Intent intent = new Intent(context, RequestPermissionActivity.class);

        intent.putExtra(EXTRA_REQUIRED_PERMISSIONS, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        });
        intent.putExtra(EXTRA_PERMISSIONS_TYPE, PermissionUtil.REQUEST_EXTERNALSTORAGE_PERMISSION);
        return intent;
    }
}
