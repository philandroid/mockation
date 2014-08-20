package com.location.philippweiher.test;

/**
 * Created by philippweiher on 31.07.14.
 */
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.location.philippweiher.test.utils.DatabaseHelper;

import java.util.List;

/**
 * Sample Android UI activity which displays a text window when it is run.
 */
public class AndroidUiActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    private final String LOG_TAG = getClass().getSimpleName();
    private TextView contentView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "creating " + getClass() + " at " + System.currentTimeMillis());
    }

    /**
     * Do our sample database stuff.
     */
    private void databaseActions(String action) {
        // get our dao
         RuntimeExceptionDao<StoredAddress, Integer> simpleDao = getHelper().getStoredAddressRuntimeExceptionDao();
        // query for all of the data objects in the database
        List<StoredAddress> list = StoredAddress.queryForAll();
        // our text builder for building the content-view
        StringBuilder sb = new StringBuilder();
        sb.append("got ").append(list.size()).append(" entries in ").append(action).append("\n");


    }
}