package com.example.android.bookstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.android.bookstore.data.BookContract.*;

public class BookProvider extends ContentProvider {

    // URI matcher code for the content URI for the books table
    private static final int BOOKS = 100;

    // URI matcher code for the content URI for a single book in the books table
    private static final int BOOK_ID = 101;

    // UriMatcher object to match a content URI to a corresponding code.
    // The input passed into the constructor represents the code to return for the
    // root URI. It's common to use NO_MATCH as the input for this case.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // State initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content uri patterns that the
        // provider should recognize. All paths added to the UriMatcher have a corresponding
        // code to return when a match is found.

        // The content uri of the form "content://com.example.android.bookstore/books" will
        // map to the integer code {@link #BOOKS}. This uri is used to provide access to
        // MULTIPLE rows of the books table
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_BOOKS, BOOKS);

        // The content URI of the form "content://com.example.android.bookstore/books/#" will map to the
        // integer code {@link #BOOK_ID}. This URI is used to provide access to ONE single row
        // of the books table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.bookstore/books/3" matches, but
        // "content://com.example.android.bookstore/books" (without a number at the end) doesn't match.
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_BOOKS + "/#", BOOK_ID);
    }

    private BookDbHelper mDbHelper;

    // log tag for log messages
    public static final String TAG = BookProvider.class.getSimpleName();

    // initialize the provider and the database helper object.
    @Override
    public boolean onCreate() {
        // make sure the variable is a global variable, so it can be referenced
        // from other ContentProvider methods.
        mDbHelper = new BookDbHelper(getContext());
        return true;
    }

    // perform the query for the given URI. Use the given projection, selection,
    // selection arguments, and sort order.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // get readable access
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // this cursor will hold the result of the query
        Cursor cursor;

        // figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // for the BOOKS code, query the books table directly with the given
                // projection, selection, selection arguments, and sort order.
                // the cursor could contain multiple rows of the books table.
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection,
                    selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                // for the BOOK_ID code, extract out the ID from the URI. For and example
                // URI such as "content://com.example.android.bookstore/books/3",
                // the selection will be "_id=?" and the selection argument wiil be a
                // string array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the
                // selection arguments that will fill in the "?". since we have 1 question
                // mark in the selection, we have 1 string in the selection arguments'
                // string array.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // this will perform a query on the books table where the _id = 3 to return
                // a Cursor containing that row of the table.
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // set notification URI on the Cursor so we know what content URI the Cursor was
        // created for. if the data at this URI changes, then we know we need to update
        // the Cursor.
        cursor.setNotificationUri(getContext(). getContentResolver(), uri);

        // return the cursor
        return cursor;
    }

    // Insert new data into the provider with the given ContentValues.
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // insert a book into the database with the given content values. Return
    // the new content URI for that specific row in the database.
    private Uri insertBook(Uri uri, ContentValues values) {
        // check that the product name is not null
        String productName = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Book requires a title");
        }

        // check that price is greater than zero
        String price = values.getAsString(BookEntry.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Book requires valid price");
        }

        // check that quantity is not null and greater than zero
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Book requires valid quantity");
        }

        // supplier is not required so no need to check. any value is valid

        // check that phone number is valid
        String phone = values.getAsString(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        //matches numbers and dashes, any order really.
        String regexStr = "^[0-9\\-]*$";
        Pattern pattern = Pattern.compile(regexStr);
        Matcher matcher = pattern.matcher(phone);
        boolean isPhoneFormat = matcher.matches();
        if (!isPhoneFormat) {
            throw new IllegalArgumentException("Book requires valid phone number");
        }

        // get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // insert the new book with the given values
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        // if the ID is -1, then the insertion failed, Log an error and return null
        if (id == -1) {
            Log.e(TAG, "insertBook: failed to insert book " + uri);
            return null;
        }

        // notify all listeners that the data has changed for the book content URI
        // uri:content://com.example.android.bookstore/books
        getContext().getContentResolver().notifyChange(uri, null);

        // return the new URI with the ID (of the newly inserted row)
        // appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    // updates the data at the given selection and selection arguments, with the
    // new ContentValues.
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                // for the BOOK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?"
                // and selection arguments will be a String array containing the actual ID.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // update books in the database with the given content values. Applt the changes to
    // the rows specified in the selection and selection args (which could be 0 or 1
    // or more books). Return number of rows successfully updated.
    private int updateBook(Uri uri, ContentValues values, String selection,
                           String[] selectionArgs) {
        // if the {@link BookEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(BookEntry.COLUMN_PRODUCT_NAME)) {
            String productName = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
            if (productName == null) {
                throw new IllegalArgumentException("Book requires a title");
            }
        }

        // if the {@link BookEntry#COLUMN_PRICE} key is present,
        // check that the price value is valid
        if (values.containsKey(BookEntry.COLUMN_PRICE)) {
            String price = values.getAsString(BookEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Book required valid price");
            }
        }

        // if the {@link BookEntry#COLUMN_QUANTITY} key is present,
        // check that the quantity is valid
        if (values.containsKey(BookEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Book required valid quantity");
            }
        }

        // no need to check the supplier. any value ia valid including null.

        // if the {@link BookEntry#COLUMN_SUPPLIER_PHONE_NUMBER} key is present,
        // check that the phone number is valid
        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER)) {
            String phone = values.getAsString(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            //matches numbers and dashes, any order really.
            String regexStr = "^[0-9\\-]*$";
            Pattern pattern = Pattern.compile(regexStr);
            Matcher matcher = pattern.matcher(phone);
            boolean isPhoneFormat = matcher.matches();
            if (!isPhoneFormat) {
                throw new IllegalArgumentException("Book requires valid phone number");
            }
        }

        // if there are no values to update, then don't try to update the database.
        if (values.size() == 0) {
            return 0;
        }

        // otherwise, get writable database to update the data
        SQLiteDatabase database= mDbHelper.getWritableDatabase();

        // perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        // if 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed.
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return the number of rows updated.
        return rowsUpdated;
    }

    // delete the data at the given selection and selectionArs
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // delete all row that match the selection and selectionArs
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // delete a single row given by the ID in the URI
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // if 1 or more rows were deleted, then notify all listeners that the data at the
        // given uri has changed.
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return the number of rows deleted
        return rowsDeleted;
    }

    // returns the MIMI type of data for the content URI.
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

}
