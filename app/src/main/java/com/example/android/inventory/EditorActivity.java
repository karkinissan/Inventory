package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private EditText mProductNameView;
    private EditText mQuantityView;
    private EditText mPriceView;
    private EditText mSupplierView;
    private ImageView mProductImageView;

    private Bitmap mImageThumbnail;

    private Uri mCurrentProductUri;

    private static final int URL_LOADER = 0;

    private boolean hasProductChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hasProductChanged = true;
            return false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Uri mImageUri;
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    mImageUri = imageReturnedIntent.getData();
                    mProductImageView = (ImageView) findViewById(R.id.image_preview);
                    try {
                        mImageThumbnail = DbBitmapUtility.getThumbnail(mImageUri, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mProductImageView.setImageBitmap(mImageThumbnail);

                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    mImageUri = imageReturnedIntent.getData();
                    mProductImageView = (ImageView) findViewById(R.id.image_preview);
                    try {
                        mImageThumbnail = DbBitmapUtility.getThumbnail(mImageUri, this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mProductImageView.setImageBitmap(mImageThumbnail);
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle("Add a Product");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit Product");
            getLoaderManager().initLoader(URL_LOADER, null, EditorActivity.this);
        }

        mProductNameView = (EditText) findViewById(R.id.edit_product_name);
        mQuantityView = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceView = (EditText) findViewById(R.id.edit_product_price);
        mSupplierView = (EditText) findViewById(R.id.edit_product_supplier);
        mProductImageView = (ImageView) findViewById(R.id.image_preview);

        mProductNameView.setOnTouchListener(mTouchListener);
        mQuantityView.setOnTouchListener(mTouchListener);
        mPriceView.setOnTouchListener(mTouchListener);
        mSupplierView.setOnTouchListener(mTouchListener);
        mProductImageView.setOnTouchListener(mTouchListener);

        Button button = (Button) findViewById(R.id.image_picker_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!hasProductChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                showUnsavedChangesDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        //If the user doesn't provide a pet name, we have to prompt him to.
        if (TextUtils.isEmpty(mProductNameView.getText().toString().trim())) {
            showEmptyFieldsDialog();
            return;
        }
        ContentValues values = new ContentValues();

        String productName = mProductNameView.getText().toString().trim();
        int productPrice;
        try {
            productPrice = Integer.parseInt(mPriceView.getText().toString().trim());
        } catch (NumberFormatException e) {
            productPrice = 0;
        }
        int productQuantity;
        try {
            productQuantity = Integer.parseInt(mQuantityView.getText().toString().trim());

        } catch (NumberFormatException e) {
            productQuantity = 0;
        }

        String supplier = mSupplierView.getText().toString().trim();
        byte[] imageStream = imageStream = DbBitmapUtility.getBitmapAsByteArray(mImageThumbnail);
        values.put(ProductEntry.COLUMN_NAME, productName);
        values.put(ProductEntry.COLUMN_PRICE, productPrice);
        values.put(ProductEntry.COLUMN_QUANTITY, productQuantity);
        values.put(ProductEntry.COLUMN_SUPPLIER, supplier);
        values.put(ProductEntry.COLUMN_IMAGE, imageStream);
        if (mCurrentProductUri == null) {
            Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, "Product Insertion Failed", Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG, "Product insertion error.");
            } else {
                Toast.makeText(this, "Product Added", Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG, "New row inserted. URI: " + uri);
            }
        } else {
            int rowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsUpdated != 0) {
                Toast.makeText(this, "Product Updated", Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG, "Product updated. ID: " + ContentUris.parseId(mCurrentProductUri));
            } else {
                Toast.makeText(this, "Product update Failed", Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG, "Product update Failed. URI: " + mCurrentProductUri);
            }
        }
        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this product?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog!=null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void deleteProduct() {
        int numberOfRowsDeleted = getContentResolver().delete(mCurrentProductUri,null,null);
        Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG,"Rows deleted: "+numberOfRowsDeleted);
        finish();
    }

    private void showEmptyFieldsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please provide a Product Name");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!hasProductChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_SUPPLIER,
                ProductEntry.COLUMN_IMAGE
        };
        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        cursor.moveToFirst();

        mProductNameView.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME)));

        mQuantityView.setText(Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY))));

        mPriceView.setText(Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE))));

        mSupplierView.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER)));

        byte[] imageAsByte = cursor.getBlob(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_IMAGE));
        Bitmap bitmap = null;
        if (imageAsByte != null) {
            bitmap = DbBitmapUtility.getImage(imageAsByte);
            //set the read thumbnail as the bitmap we will save if the user doesn't pick a new photo.
            mImageThumbnail = bitmap;
        }
        mProductImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductNameView.setText("");
        mQuantityView.setText("");
        mPriceView.setText("");
        mSupplierView.setText("");
        mProductImageView.setImageBitmap(null);
    }
}
