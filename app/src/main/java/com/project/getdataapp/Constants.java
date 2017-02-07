package com.project.getdataapp;

/**
 * Created by billy_chi on 2017/2/7.
 */

public interface Constants {
    String FOLDER_NAME = "/gcis_data";
    String URL_TEMPLATE = "http://data.gcis.nat.gov.tw/od/data/api/6BBA2268-1367-4B42-9CCA-BC17499EBE8C?$format=%s" +
            "&$filter=Company_Name like %s" +
            " and Company_Status eq %s";


    String EXTRA_REQUIRED_PERMISSIONS = "required_permissions";
    String EXTRA_PERMISSIONS_TYPE = "permissions_type";
    String EXTRA_PERMISSION_RESULT = "key_permission_result";

    int REQUEST_EXTERNALSTORAGE_PERMISSION = 1;

    int READ_FILE_REQUEST_CODE = 1101;
    int REQUEST_PERMISSION_REQUEST_CODE = 1002;

}
