package com.example.android.inventory;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
    public void bindView(View view, final Context context, final Cursor cursor) {
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
                                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                                contentResolver.delete(currentProductUri, null, null);
                                Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();
                                Log.v(LOG_TAG, "Pet Deleted. URI: " + currentProductUri);
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


}
