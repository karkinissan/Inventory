package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventory.data.ProductInsertionLoader;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.android.inventory.data.ProductContract.ProductEntry;

public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, ProductAdapter.ProductAdapterOnClickHandler {
    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private ProductCursorAdapter mCursorAdapter;
    private static final int URL_LOADER = 0;
    private static final int INSERT_DUMMY_DATA_ID = 1;
    private Uri mCurrentProductUri;
    private ArrayList<ContentValues> mValues;
    private ProductAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.putExtra("fab", true);
                startActivity(intent);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_product);
        emptyView = (TextView) findViewById(R.id.empty_view);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ProductAdapter(this, CatalogActivity.this, this, getContentResolver());
        mRecyclerView.setAdapter(mAdapter);

//        final ListView listView = (ListView) findViewById(R.id.list_view_product);
//        TextView textView = (TextView) findViewById(R.id.empty_view);
//        listView.setEmptyView(textView);
//        mCursorAdapter = new ProductCursorAdapter(this, null, getContentResolver());
//        listView.setAdapter(mCursorAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
//                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
//                intent.setData(currentProductUri);
//                intent.putExtra("edit", true);
//                startActivity(intent);
//            }
//        });

//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                mCurrentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI,id);
//                mActionMode = startActionMode(mActionModeCallback);
//                listView.setSelected(true);
//                return true;
//            }
//        });

        //Adding batch contextual menus.
//        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
//            int itemsSelectedCount = 0;
//
//            @Override
//            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
//                //What to do when an item is selected or unselected.
//                if (checked) {
//                    if (itemsSelectedCount < listView.getAdapter().getCount()) {
//                        itemsSelectedCount++;
//                    }
//                } else if (itemsSelectedCount > 0) {
//                    itemsSelectedCount--;
//                }
//                //set the title to reflect the number of items selected.
//                mode.setTitle(itemsSelectedCount + " selected");
//            }
//
//            @Override
//            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                itemsSelectedCount = 0;
//                mode.getMenuInflater().inflate(R.menu.contextual_action_mode_menu, menu);
//                return true;
//            }
//
//            @Override
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//
//                return false;
//            }
//
//            @Override
//            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.context_delete_product:
//                        long[] checkedItemIds = listView.getCheckedItemIds();
//                        showMultipleDeleteConfirmationDialog(checkedItemIds);
//                        return true;
//                    case R.id.select_all:
//                        if (itemsSelectedCount < listView.getAdapter().getCount()) {
//                            for (int i = 0; i < listView.getAdapter().getCount(); i++) {
//                                listView.setItemChecked(i, true);
//                            }
//                        }
//                        return true;
//                    default:
//                        return false;
//                }
//            }
//
//            @Override
//            public void onDestroyActionMode(ActionMode mode) {
//
//            }
//
//        });
        getLoaderManager().initLoader(URL_LOADER, null, CatalogActivity.this);
    }

    @Override
    public void onClick(long itemId) {
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, itemId);
        intent.setData(currentProductUri);
        intent.putExtra("edit", true);
        startActivity(intent);
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        getMenuInflater().inflate(R.menu.list_item_context_menu, menu);
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        switch (item.getItemId()) {
//            case R.id.context_delete_product:
//                mCurrentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, info.id);
//                showDeleteConfirmationDialog("Delete this product?", mCurrentProductUri);
//                return true;
//            default:
//                return super.onContextItemSelected(item);
//        }
//    }

//    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
//        //Called when the action mode is created;
//        @Override
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            mode.getMenuInflater().inflate(R.menu.contextual_action_mode_menu,menu);
//            return true;
//        }
//        // Called each time the action mode is shown. Always called after onCreateActionMode, but
//        // may be called multiple times if the mode is invalidated.
//        @Override
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//
//            return false;
//        }
//
//        // Called when the user selects a contextual menu item
//        @Override
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            switch (item.getItemId()){
//                case R.id.context_delete_product:
//                    showDeleteConfirmationDialog("Delete this item?",mCurrentProductUri);
//                    mode.finish();
//                    return true;
//                default:
//                    return false;
//
//            }
//        }
//
//        @Override
//        public void onDestroyActionMode(ActionMode mode) {
//            mActionMode = null;
//
//        }
//    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog("Delete all products?", null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyData() {
        mValues = new ArrayList<>();
        mValues.add(getContentValues("Sugar", 50, 60, "Sweet Sugar Company", R.drawable.sugar));
        mValues.add(getContentValues("Salt", 20, 30, "Salty Salt Company", R.drawable.salt));
        mValues.add(getContentValues("Bread", 45, 20, "Nanglo", R.drawable.bread));
        mValues.add(getContentValues("Biscuit", 75, 20, "GoodDay", R.drawable.biscuit));
        mValues.add(getContentValues("Noodles", 15, 200, "CupMen", R.drawable.noodles));
        mValues.add(getContentValues("Milk", 33, 100, "DDC", R.drawable.milk));
        mValues.add(getContentValues("Cheese", 80, 30, "Amul", R.drawable.cheese));

        new InventoryAsyncTask().execute(mValues);
        /*for (ContentValues value : values) {
            Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, value);
            Log.v(LOG_TAG, "Dummy data inserted. ID: " + ContentUris.parseId(uri));

        }*/
//        new ProductInsertionLoader(this,getContentResolver(),values);
        //getLoaderManager().initLoader(INSERT_DUMMY_DATA_ID, null, this).forceLoad();
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Dummy Data Inserted", Snackbar.LENGTH_SHORT);
        snackbar.show();
//        Toast.makeText(this, "Dummy Data Inserted", Toast.LENGTH_SHORT).show();
    }

    private ContentValues getContentValues(String name, int price, int quantity, String supplier, int resId) {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_NAME, name);
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_SUPPLIER, supplier);

        //Uri for the sugar drawable
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(resId)
                + '/' + getResources().getResourceTypeName(resId)
                + '/' + getResources().getResourceEntryName(resId));
        Bitmap bitmap = null;
        //Converting the sugar image into a thumbnail
        try {
            bitmap = DbBitmapUtility.getThumbnail(imageUri, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Converting the sugar thumbnail into a byte array.
        byte[] image = DbBitmapUtility.getBitmapAsByteArray(bitmap);

        values.put(ProductEntry.COLUMN_IMAGE, image);
        return values;
    }

    private ContentValues getContentValues(int id, String name, int price, int quantity, String supplier, byte[] image) {
        ContentValues values = new ContentValues();
        values.put(ProductEntry._ID, id);
        values.put(ProductEntry.COLUMN_NAME, name);
        values.put(ProductEntry.COLUMN_PRICE, price);
        values.put(ProductEntry.COLUMN_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_SUPPLIER, supplier);
        values.put(ProductEntry.COLUMN_IMAGE, image);
        return values;
    }

    private void showDeleteConfirmationDialog(String message, final Uri currentProductUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllProducts(currentProductUri);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean deleteAllProducts(Uri currentProductUri) {
        if (currentProductUri == null) {
            int numberOfRowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
            Log.v(LOG_TAG, "All Products Deleted. Rows: " + numberOfRowsDeleted);
        } else {
            getContentResolver().delete(currentProductUri, null, null);
            Log.v(LOG_TAG, "Product Deleted. Uri: " + currentProductUri);
        }
        return true;
    }

    private boolean showMultipleDeleteConfirmationDialog(final long[] checkedItemIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (checkedItemIds.length > 1) {
            builder.setMessage("Delete " + checkedItemIds.length + " products?");
        } else {
            builder.setMessage("Delete this product?");
        }
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<Cursor> cursors = new ArrayList<Cursor>();
                Cursor cursor = null;
                for (long itemId : checkedItemIds) {
                    mCurrentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, itemId);
                    String[] projection = {
                            ProductEntry._ID,
                            ProductEntry.COLUMN_NAME,
                            ProductEntry.COLUMN_QUANTITY,
                            ProductEntry.COLUMN_PRICE,
                            ProductEntry.COLUMN_SUPPLIER,
                            ProductEntry.COLUMN_IMAGE
                    };
                    cursor = getContentResolver().query(mCurrentProductUri,
                            projection,
                            null,
                            null,
                            null,
                            null);
                    cursor.moveToFirst();
                    cursors.add(cursor);
                    deleteAllProducts(mCurrentProductUri);
                }
                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Items Deleted", Snackbar.LENGTH_LONG);
                snackbar.setAction("Undo", new MyUndoListener(cursors));
                snackbar.show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return true;
    }


    private class InventoryAsyncTask extends AsyncTask<ArrayList<ContentValues>, Void, Uri> {

        @Override
        protected Uri doInBackground(ArrayList<ContentValues>... params) {
            for (ContentValues value : params[0]) {
                Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, value);
                Log.v(LOG_TAG, "Pet Inserted. URI: " + uri);
            }
            return ProductEntry.CONTENT_URI;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            getContentResolver().notifyChange(uri, null);
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "onCreateLoader");
        switch (id) {
            case URL_LOADER:
                String[] projection = {
                        ProductEntry._ID,
                        ProductEntry.COLUMN_NAME,
                        ProductEntry.COLUMN_QUANTITY,
                        ProductEntry.COLUMN_PRICE,
                        ProductEntry.COLUMN_SUPPLIER,
                        ProductEntry.COLUMN_IMAGE
                };
                return new CursorLoader(
                        this,
                        ProductEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null);
            case INSERT_DUMMY_DATA_ID:
                new ProductInsertionLoader(this, getContentResolver(), mValues);
                return null;
            default:
                Log.v(LOG_TAG, "onCreateLoader: Invalid id");
                return null;

        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished");

        if (loader.getId() == URL_LOADER) {
//            mCursorAdapter.swapCursor(data);
            if (data != null && data.getCount() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else if (data != null && data.getCount() > 0) {
                mRecyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
            mAdapter.swapCursor(data);
        } else return;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "onLoaderReset");

        mAdapter.swapCursor(null);
    }

    public class MyUndoListener implements View.OnClickListener {
        ArrayList<Cursor> cursors = null;

        public MyUndoListener(ArrayList<Cursor> cursors) {
            this.cursors = cursors;

        }

        @Override
        public void onClick(View v) {
            ArrayList<ContentValues> values = new ArrayList<>();
            for (Cursor cursor : cursors) {
                values.add(extractFromCursor(cursor));
            }
            new InventoryAsyncTask().execute(values);
        }

        private ContentValues extractFromCursor(Cursor cursor) {

            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry._ID));

            String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME));

            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY));

            int price = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE));

            String supplier = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER));

            byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_IMAGE));
            cursor.close();
            return getContentValues(id, name, quantity, price, supplier, image);
        }
    }
}
