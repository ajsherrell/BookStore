package com.example.android.bookstore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.bookstore.data.BookContract.BookEntry;



// used code from Udacity's Pet App
// Displays list of books that were entered and stored into the app

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // log tag for debugging only
    public static final String TAG = MainActivity.class.getSimpleName();

    // identifier for the book data loader
    private static final int BOOK_LOADER = 0;

    // adapter for the ListView
    BookCursorAdapter mCursorAdapter;

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

        // find the ListView which will be populated with the book data.
        ListView bookListView = (ListView) findViewById(R.id.list);

        // find and set empty view on the ListView, so that it only shows when the list
        // has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // setup an adapter to create a list item for each row of book data in the Cursor.
        // there is no book data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);

        // setup the item click listener
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // form the content URI that represents the specific book that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link BookEntry#CONTENT_URI}.
                // for example, the URI would be "content://com.example.android.bookstore/books/2"
                // if the book with ID 2 was clicked on
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                // set the URI on the data field of the intent
                intent.setData(currentBookUri);

                // launch the {@link EditorActivity} to display the data for the current book.
                startActivity(intent);
            }
        });

        // kick of the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    // helper method to insert hard coded book data into the database.
    // for debugging purposes
    private void insertBook() {
        // create a ContentValues object where column names are the keys,
        // and "The Giver" book attributes are the values
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, "The Giver");
        values.put(BookEntry.COLUMN_PRICE, 9);
        values.put(BookEntry.COLUMN_QUANTITY, 4);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, "Penguin House");
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, "999-555-5555");

        // insert a new row for The Giver in the provider using the ContentResolver.
        // use the {@link BookEntry#CONTENT_URI} to indicate that we want to insert
        // into the bookstore database table.
        // receive the new content URI that will allow us to access "The Giver"
        // data in the future.
        Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

        Log.v(TAG, "insertBook: New row ID: " + newUri + " What is this error???????????????????");
    }

    // helper method to delete all books in the database
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Log.e(TAG, "deleteAllBooks: rows deleted from bookstore database: " + rowsDeleted );
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
                return true;
             // respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // define a projection that specifies the columns from the table we care about.
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY
        };

        // this loader will execute the ContentProvider's query method on a background
        // thread
        return new CursorLoader(this,    //parent activity context
                BookEntry.CONTENT_URI,          //provider content URI to query
                projection,                     //columns to include in the resulting cursor
                null,                   //no selection clause
                null,                //no selection arguments
                null);                  //default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // update {@link BookCursorAdapter} with this new cursor containing updated book data.
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
