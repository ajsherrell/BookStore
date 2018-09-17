package com.example.android.bookstore;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.bookstore.data.BookContract.BookEntry;

/**
 * Allows user to create a new book or edit an existing one.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // log tag
    public static final String TAG = EditorActivity.class.getSimpleName();

    // identifier for the book data loader
    private static final int EXISTING_BOOK_LOADER = 0;

    // content URI for the existing book (null if new book)
    private Uri mCurrentBookUri;

    // EditText field to enter the book's title
    private EditText mProductName;

    // EditText field to enter the book price
    private EditText mPrice;

    // EditText to enter book quantity
    private TextView mQuantity;

    // EditText for supplier name
    private EditText mSupplierName;

    // EditText for supplier phone number
    private EditText mSupplierPhoneNumber;

    // counter for quantity buttons
    int count = 0;

    // boolean flag that keeps track of whether the book has been edited (true
    // or not (false)
    private boolean mBookHasChanged = false;

    // order button for making a phone call to order from supplier
    private Button orderButton;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mBookHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };


    private static final int PHONE_CALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // examine the intent that was used to launch this activity, in order
        // to figure out if we're creating a new book or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // if the intent does NOT contain a book content uri, then we know that we
        // are creating a new pet.
        if (mCurrentBookUri == null) {
            // this is a new book, so change the app bar to say "Add a Book"
            setTitle(getString(R.string.editor_activity_title_new_book));

            // invalidate the options menu, so the "Delete" menu option can be hidden.
            // ---it doesn't make sense to delete a book that hasn't been created yet.
            invalidateOptionsMenu();
        } else {
            // otherwise this is an existing book, so change app bar to say "Edit Book"
            setTitle(getString(R.string.editor_activity_title_edit_book));

            // initialize a loader to read the book data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // find all relevant views that we will read user input from
        mProductName = (EditText) findViewById(R.id.edit_product_name);
        mPrice = (EditText) findViewById(R.id.edit_price);
        mQuantity = (TextView) findViewById(R.id.edit_quantity);
        mSupplierName = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneNumber = (EditText) findViewById(R.id.edit_supplier_phone_number);

        // setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductName.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumber.setOnTouchListener(mTouchListener);

        Log.i(TAG, "Something went wrong after mTouchListeners!!!!!!!!");

        // bind the button views
        final Button decrementButton = (Button) findViewById(R.id.decrement_quantity);
        Button incrementButton = (Button) findViewById(R.id.increment_quantity);

        // onClick for increment button
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                mQuantity.setText(String.valueOf(count));
            }
        });

        // onClick for decrement button
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count == 0) {
                    decrementButton.setEnabled(false);
                } else {
                    count--;
                    mQuantity.setText(String.valueOf(count));
                }
            }
        });

        // bind button to order button view
        orderButton = (Button) findViewById(R.id.order);

        // click listener to make a call to order more inventory from the supplier
        // used code from "https://www.tutorialspoint.com/android/android_phone_calls.htm"
        orderButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(EditorActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(
                            EditorActivity.this,
                            new String[] { Manifest.permission.CALL_PHONE },
                            PHONE_CALL
                    );
                    return;
                }

                // get phone number
                String phoneNumber = mSupplierPhoneNumber.getText().toString().trim();

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));

                getApplicationContext().startActivity(callIntent);
            }
        });

    }

    /**
     * get user input from editor and save new book into database
     */
    private void saveBook() {
        // read from input fields
        // use trim to eliminate leading or trailing white space
        String productName = mProductName.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        int price = Integer.parseInt(priceString); // into string
        String quantityString = mQuantity.getText().toString().trim();
        String supplierName = mSupplierName.getText().toString().trim();
        String supplierPhoneNumber = mSupplierPhoneNumber.getText().toString().trim();

       // check if this is supposed to be a new book and check if all the fields in the
        // editor are blank
        if (mCurrentBookUri == null && TextUtils.isEmpty(productName) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierName) && TextUtils.isEmpty(supplierPhoneNumber)) {
            // since no fields were modified, we can return early without creating a new books
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // create a ContentValues object where column name are the keys,
        // and book attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(BookEntry.COLUMN_PRICE, price);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);

        // if the quantity is not provided by the user, don't try to parse the string into
        // an integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(BookEntry.COLUMN_QUANTITY, quantity);

        // determine if this is a new or existing book by checking if mCurrentBookUri
        // is null or not
        if (mCurrentBookUri == null) {
            // this is a NEW book, so insert a new book into the provider,
            // returning the content URI for the new book.
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // if the new content URI is null, then there was an error with insertion
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentBookUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            // show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // if no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        Log.i(TAG, "Something went wrong in saveBook()!!!!!!!!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu options from the res/menu/menu_editor.xml file.
        // this adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // user clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // respond to a click on the "save" menu option
            case R.id.action_save:
                // save book to database
                saveBook();
                // exit activity
                finish();
                return true;
            // respond to a click on the "delete" menu option
            case R.id.action_delete:
                // pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // respond to a click on the "up" arrow button in the app bar
            case android.R.id.home:
                // if the book hasn't changed, continue with navigating up to parent
                // activity which is the {@link MainActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // otherwise if there are unsaved changes, setup a dialog to warn
                // the user. Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // user clicked "Discard" button,
                                // navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // this method is called when the back button is pressed.
    @Override
    public void onBackPressed() {
        // if the book hasn't changed, continue with handling back button press.
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // otherwise if there are unsaved changes, setup a dialog to warn the user.
        // create a click listener to handle the user confirming the changes be discarded
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // user clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // since the editor shows all book attributes, define a projection that contains
        // all columns from the books table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        // this loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       //Parent activity context
                mCurrentBookUri,                  //Query the content URI for the current book
                projection,                       //Columns to include in the resulting Cursor
                null,                     //No selection clause
                null,                  //No selection args
                null);                   //Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // proceed with moving to the first row of the cursor and reading data from it
        // (this should be the only row in cursor)
        if (cursor.moveToFirst()) {
            // find the columns of book attributes that we're interested in
            // product name, price, and quantity.
            int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // read the book attributes from the Cursor for the current book
            String productName = cursor.getString(titleColumnIndex);
            int bookPrice = cursor.getInt(priceColumnIndex);
            int bookQuantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // update the views on the screen with the values from the database
            mProductName.setText(productName);
            mPrice.setText(Integer.toString(bookPrice));
            mQuantity.setText(Integer.toString(bookQuantity));
            mSupplierName.setText(supplierName);
            mSupplierPhoneNumber.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // if the loader is invalidated, clear out all the data from the input fields.
        mProductName.setText("");
        mPrice.setText("");
        mQuantity.setText(0);
        mSupplierName.setText("");
        mSupplierPhoneNumber.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener
                                          discardButtonClickListener) {
        // create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // user clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Prompt the user to confirm that they want to delete this book.
    private void showDeleteConfirmationDialog() {
        //create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative button on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // user clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // user clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // perform the deletion of the book in the database.
    private void deleteBook() {
        // only perform the delete if this an existing book.
        if (mCurrentBookUri != null) {
            // call the ContentResolver to delete the book at the given content URI.
            // pass in null for the selection and selectionArgs because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // if no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // close the activity
        finish();
    }

}
