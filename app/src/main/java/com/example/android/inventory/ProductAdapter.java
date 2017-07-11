package com.example.android.inventory;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import static com.example.android.inventory.data.ProductContract.ProductEntry;


/**
 * Created by Nissan on 7/7/2017.
 */

public class ProductAdapter extends SelectableAdapter<ProductAdapter.ProductAdapterViewHolder> {
    private final Context mContext;
    private static final String LOG_TAG = ProductAdapter.class.getSimpleName();
    private Uri mCurrentProductUri;
    private final ProductAdapterOnClickHandler mClickHandler;
    private final ContentResolver contentResolver;
    AppCompatActivity activity;

    public interface ProductAdapterOnClickHandler {
        void onClick(long itemId);
    }

    private Cursor mCursor;

    public ProductAdapter(AppCompatActivity activity, Context context, ProductAdapterOnClickHandler clickHandler, ContentResolver contentResolver) {
        mContext = context;
        mClickHandler = clickHandler;
        this.contentResolver = contentResolver;
        this.activity = activity;


    }

    class ProductAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        ImageView thumbnail;
        TextView name;
        TextView quantity;
        TextView price;
        TextView supplier;
        ImageView threeDotMenu;
        View selectedOverlay;

        public ProductAdapterViewHolder(final View view) {
            super(view);

            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            name = (TextView) view.findViewById(R.id.name);
            quantity = (TextView) view.findViewById(R.id.quantity);
            price = (TextView) view.findViewById(R.id.price);
            supplier = (TextView) view.findViewById(R.id.supplier);
            threeDotMenu = (ImageView) view.findViewById(R.id.three_dot_menu);
            selectedOverlay = (View) view.findViewById(R.id.selected_overlay);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long itemId = mCursor.getLong(mCursor.getColumnIndexOrThrow(ProductEntry._ID));
            mClickHandler.onClick(itemId);
        }

        @Override
        public boolean onLongClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long itemId = mCursor.getLong(mCursor.getColumnIndexOrThrow(ProductEntry._ID));
            Log.v(LOG_TAG, "Item Long Clicked. ID: " + itemId);
            return true;
        }
    }

    @Override
    public ProductAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        view.setFocusable(true);

        return new ProductAdapterViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ProductAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        byte[] imageAsByte = mCursor.getBlob(mCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_IMAGE));

        Bitmap bitmap = null;
        if (imageAsByte != null) {
            bitmap = DbBitmapUtility.getImage(imageAsByte);
        }
        holder.thumbnail.setImageBitmap(bitmap);

        holder.name.setText(mCursor.getString(mCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME)));

        holder.quantity.setText(R.string.quantity);
        holder.quantity.append(Integer.toString(mCursor.getInt(mCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY))));

        holder.price.setText(R.string.price);
        holder.price.append(Integer.toString(mCursor.getInt(mCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE))));

        holder.supplier.setText(mCursor.getString(mCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER)));

        holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);

        final long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(ProductEntry._ID));
        holder.threeDotMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);
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
                                Snackbar snackbar = Snackbar.make(holder.itemView.getRootView().findViewById(R.id.coordinator_layout), "Product Deleted", Snackbar.LENGTH_LONG);
                                snackbar.setAction("Undo", new ProductAdapter.MyUndoListener(cursor));
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
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    int getCount() {
        return mCursor.getCount();
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
