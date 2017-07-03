package com.example.android.inventory.data;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Nissan on 6/28/2017.
 */

public class ProductInsertionLoader extends AsyncTaskLoader {
    private String LOG_TAG = ProductInsertionLoader.class.getSimpleName();
    private ArrayList<ContentValues> mValues;
    private ContentResolver mContentResolver;

    public ProductInsertionLoader(Context context, ContentResolver contentResolver, ArrayList<ContentValues> values) {
        super(context);
        mValues = values;
        mContentResolver = contentResolver;
        Log.v(LOG_TAG, "constructor called");
    }

    @Override
    public Object loadInBackground() {
        Log.v(LOG_TAG, "loadInBackground() called");
        ArrayList<Uri> uris = new ArrayList<>();
        for (ContentValues value : mValues) {
            Uri uri = mContentResolver.insert(ProductContract.ProductEntry.CONTENT_URI, value);
            Log.v(LOG_TAG, "Dummy data inserted. ID: " + ContentUris.parseId(uri));
            uris.add(uri);
        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        Log.v(LOG_TAG, "onStartLoading() called");
        forceLoad();
    }
}
