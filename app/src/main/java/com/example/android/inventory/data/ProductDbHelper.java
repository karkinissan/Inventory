package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * Created by Nissan on 6/26/2017.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "inventory.db";

    private static final String SQL_CREATE_PRODUCTS_TABLE =
            "CREATE TABLE " + ProductEntry.TABLE_NAME + "(" +
                    ProductEntry._ID + " INTEGER PRIMARY KEY, " +
                    ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    ProductEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +
                    ProductEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 0, " +
                    ProductEntry.COLUMN_SUPPLIER + " TEXT, " +
                    ProductEntry.COLUMN_IMAGE + " BLOB);";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;

    ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(LOG_TAG, "Creating table: " + SQL_CREATE_PRODUCTS_TABLE);
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(LOG_TAG, "Dropping Table: " + SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_ENTRIES);

    }
}
