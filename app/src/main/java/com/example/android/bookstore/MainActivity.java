package com.example.android.bookstore;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.bookstore.data.BookContract;
import com.example.android.bookstore.data.BookContract.BookEntry;
import com.example.android.bookstore.data.BookDbHelper;


// used code from Udacity's Pet App
// Displays list of books that were entered and stored into the app

public class MainActivity extends AppCompatActivity {

    // log tag for debugging
    public static final String TAG = MainActivity.class.getSimpleName();

    // database helper that provides access to the database
    private BookDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(MainActivity.this, EditorActivity.class);
               startActivity(intent);
           }
        });

        // to access database, we instantiate the subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new BookDbHelper(this);
    }

    // when the user saves a book, the activity will restart with the new book in the
    // database
    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     *  Temporary helper method to display information in the onscreen TextView
     *  about the state of the book database
     */
    private void displayDatabaseInfo() {
        // create and/or open a database to read from it:
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };

        // perform a query on the books table
        Cursor cursor = db.query(
           BookEntry.TABLE_NAME,    //the table to query
           projection,              // the columns to return
           null,            // the columns for the WHERE clause
           null,        // the values for the WHERE clause
           null,           // don't group the rows
           null,            // don't filter by row groups
           null             // the sort order
        );

        TextView displayView = (TextView) findViewById(R.id.text_view_book);

        try {
            // create a header in the Text View that looks like this:
            //
            // the books table contains <number of rows in Cursor> books.
            // _id - product name - price - quantity - supplier name - supplier phone number
            //
            // in the while loop below, iterate through the rows of the curson and display
            // the info from each column in this order.
            displayView.setText("The books table contains " + cursor.getCount() + " books.\n\n");
            displayView.append(BookEntry._ID + " - " +
                    BookEntry.COLUMN_PRODUCT_NAME + " - " +
                    BookEntry.COLUMN_PRICE + " - " +
                    BookEntry.COLUMN_QUANTITY + " - " +
                    BookEntry.COLUMN_SUPPLIER_NAME + " - " +
                    BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + "\n");

            // figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
            int productNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneNumberIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // iterate  through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentProductName = cursor.getString(productNameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                String currentSupplierPhoneNumber = cursor.getString(supplierPhoneNumberIndex);
                // display the values from each column of the current row in the
                // cursor in the TextView
                displayView.append(("/n" + currentID + " - " +
                        currentProductName + " - " +
                        currentPrice + " - " +
                        currentQuantity + " - " +
                        currentSupplierName + " - " +
                        currentSupplierPhoneNumber));
            }
        } finally {
            // always close the cursor when you're done reading form it. this releases all
            // its resources and makes it invalid.
            cursor.close();
        }

    }

    private void insertBook() {
        // gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // create a ContentValues object where names are the keys,
        // and The Giver's book attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, "The Giver");
        values.put(BookEntry.COLUMN_PRICE, 9);
        values.put(BookEntry.COLUMN_QUANTITY, 4);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, "Penguin House");
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, "555-5555");

        // insert a new row for The Giver in the database, returning the ID of that new
        // row, the first argument for db.insert() is the books table name.
        // the second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if this is
        // set to "null", then the framework will not insert a row when there are no
        // values). The third argument is the ContentValues object containing the
        // info for The Giver.
        long newRowId = db.insert(BookContract.BookEntry.TABLE_NAME, null, values);

        Log.i(TAG, "insertBook: New row ID: " + newRowId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu options from the res/menu/menu_catalog.xml file.
        // this adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // user clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                displayDatabaseInfo();
                return true;
             // respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
