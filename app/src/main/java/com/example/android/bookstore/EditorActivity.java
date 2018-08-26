package com.example.android.bookstore;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.bookstore.data.BookContract;
import com.example.android.bookstore.data.BookContract.BookEntry;
import com.example.android.bookstore.data.BookDbHelper;

/**
 * Allows user to create a new book or edit an existing one.
 */

public class EditorActivity extends AppCompatActivity {

    // EditText field to enter the book's title
    private EditText mProductName;

    // EditText field to enter the book price
    private EditText mPrice;

    // EditText to enter book quantity
    private EditText mQuantity;

    // EditText for supplier name
    private EditText mSupplierName;

    // EditText for supplier phone number
    private EditText mSupplierPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // find all relevant views that we will read user input from
        mProductName = (EditText) findViewById(R.id.edit_product_name);
        mPrice = (EditText) findViewById(R.id.edit_price);
        mQuantity = (EditText) findViewById(R.id.edit_quantity);
        mSupplierName = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneNumber = (EditText) findViewById(R.id.edit_supplier_phone_number);

    }

    /**
     * get user input from editor and save new book into database
     */
    private void insertBook() {
        // read from input fields
        // use trim to eliminate leading or trailing white space
        String productName = mProductName.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        int price = Integer.parseInt(priceString); // into string
        String quantityString = mQuantity.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String supplierName = mSupplierName.getText().toString().trim();
        String supplierPhoneNumber = mSupplierPhoneNumber.getText().toString().trim();

        // create database helper
        BookDbHelper mDbHelper = new BookDbHelper(this);

        // gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // create a ContentValues object where column name are the keys,
        // and book attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(BookEntry.COLUMN_PRICE, price);
        values.put(BookEntry.COLUMN_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);

        // insert a new row for book in the database, return the ID of that new row
        long newRowId = db.insert(BookEntry.TABLE_NAME, null, values);

        // show a toast message depending on whether or not the insertion was successful
        if (newRowId == -1) {
            // if the row is -1, there is an error with insertion.
            Toast.makeText(this, "Error with saving book", Toast.LENGTH_SHORT).show();
        } else {
            // insertion was successful
            Toast.makeText(this, "Book saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu options from the res/menu/menu_editor.xml file.
        // this adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // user clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // respond to a click on the "save" menu option
            case R.id.action_save:
                // save book to database
                insertBook();
                // exit activity
                finish();
                return true;
            // respond to a click on the "delete" menu option
            case R.id.action_delete:
                // do nothing for now
                return true;
            // respond to a click on the "up" arrow button in the app bar
            case android.R.id.home:
                // navigate back to parent activity (MainActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
