package com.project.getdataapp;

/**
 * Created by billy_chi on 2017/1/26.
 */

public class UrlParameter {
    public String mFormat;
    public String mCompanyName;
    public String mStatus;

    public UrlParameter(String format, String companyName, String status) {
        mFormat = format;
        mCompanyName = companyName;
        mStatus = status;
    }

    public boolean isValid() {
        if(mFormat != null && mCompanyName != null && mStatus != null) {
            return !(mFormat.isEmpty() || mCompanyName.isEmpty() || mStatus.isEmpty());
        }
        return false;
    }
}
