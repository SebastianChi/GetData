package com.project.getdataapp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;

/**
 * Created by billy_chi on 2017/1/26.
 */

public class MyDialog {
    public static AlertDialog getPermissionDialog(final Context context, MyDialogData data, DialogInterface.OnClickListener dialogClickListener, DialogInterface.OnCancelListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.MyDialogTheme));
        builder.setTitle(data.getTitle());
        builder.setMessage(data.getMessage());
        builder.setPositiveButton(data.getPositiveButtonText(), dialogClickListener);
        builder.setNegativeButton(data.getNegativeButtonText(), dialogClickListener);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0.5f / 14);
                    }
                }
                if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setLetterSpacing(0.5f / 14);
                    }
                }
            }
        });

        dialog.getWindow().setLayout(320, ViewGroup.LayoutParams.WRAP_CONTENT);
        return dialog;
    }
}
