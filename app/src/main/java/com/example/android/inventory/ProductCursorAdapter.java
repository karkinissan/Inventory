package com.example.android.inventory;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * Created by Nissan on 6/26/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();
    protected ContentResolver contentResolver;

    public ProductCursorAdapter(Context context, Cursor c, ContentResolver contentResolver) {
        super(context, c);
        this.contentResolver = contentResolver;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView quantity = (TextView) view.findViewById(R.id.quantity);
        TextView price = (TextView) view.findViewById(R.id.price);
        TextView supplier = (TextView) view.findViewById(R.id.supplier);

        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(ProductEntry._ID));

        ImageView threeDotMenu = (ImageView) view.findViewById(R.id.three_dot_menu);
        threeDotMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.list_item_context_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.context_delete_product:
                                //the uri of the product we're deleting
                                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                                //What to extract from the database
                                String[] projection = {
                                        ProductEntry._ID,
                                        ProductEntry.COLUMN_NAME,
                                        ProductEntry.COLUMN_QUANTITY,
                                        ProductEntry.COLUMN_PRICE,
                                        ProductEntry.COLUMN_SUPPLIER,
                                        ProductEntry.COLUMN_IMAGE
                                };
                                //retrieve the product from the database before deleting in case
                                //we need to restore it later
                                Cursor cursor = contentResolver.query(currentProductUri,
                                        projection,
                                        null,
                                        null,
                                        null,
                                        null);
                                cursor.moveToFirst();
                                Snackbar snackbar = Snackbar.make(view.getRootView().findViewById(R.id.coordinator_layout), "Product Deleted", Snackbar.LENGTH_LONG);
                                snackbar.setAction("Undo", new MyUndoListener(cursor));
                                snackbar.show();
                                contentResolver.delete(currentProductUri, null, null);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });


        byte[] imageAsByte = cursor.getBlob(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_IMAGE));

        Bitmap bitmap = null;
        if (imageAsByte != null) {
            bitmap = DbBitmapUtility.getImage(imageAsByte);
        }
        thumbnail.setImageBitmap(bitmap);

        name.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME)));

        quantity.setText(R.string.quantity);
        quantity.append(Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY))));

        price.setText(R.string.price);
        price.append(Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE))));

        supplier.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER)));

    }

    public class MyUndoListener implements View.OnClickListener {
        Cursor cursor = null;

        public MyUndoListener(Cursor cursor) {
            this.cursor = cursor;

        }

        @Override
        public void onClick(View v) {
            contentResolver.insert(ProductEntry.CONTENT_URI, extractFromCursor(cursor));
        }

        private ContentValues extractFromCursor(Cursor cursor) {

            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry._ID));

            String name = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME));

            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY));

            int price = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE));

            String supplier = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER));

            byte[] image = cursor.getBlob(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_IMAGE));
            ContentValues value = new ContentValues();
            value.put(ProductEntry._ID, id);
            value.put(ProductEntry.COLUMN_NAME, name);
            value.put(ProductEntry.COLUMN_PRICE, price);
            value.put(ProductEntry.COLUMN_QUANTITY, quantity);
            value.put(ProductEntry.COLUMN_SUPPLIER, supplier);
            value.put(ProductEntry.COLUMN_IMAGE, image);
            cursor.close();
            return value;

        }

    }
}
