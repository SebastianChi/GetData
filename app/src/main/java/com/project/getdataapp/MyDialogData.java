package com.project.getdataapp;

import android.view.View;
import android.widget.ListAdapter;

/**
 * Created by billy_chi on 2017/1/26.
 */

public class MyDialogData {
    private String title = "";
    private String message = "";
    private String positiveButtonText = "";
    private String negativeButtonText = "";
    private boolean cancelable = true;
    private ListAdapter listAdapter = null;
    private View view = null;

    public ListAdapter getListAdapter () {
        return listAdapter;
    }

    public void setListAdapter (ListAdapter adapter) {
        this.listAdapter = adapter;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getPositiveButtonText() {
        return positiveButtonText;
    }

    public void setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
    }

    public String getNegativeButtonText() {
        return negativeButtonText;
    }

    public void setNegativeButtonText(String negativeButtonText) {
        this.negativeButtonText = negativeButtonText;
    }
}
