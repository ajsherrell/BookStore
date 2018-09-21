package com.example.android.bookstore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.bookstore.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    // log tag for debugging
    public static final String TAG = BookDbHelper.class.getSimpleName();

    // name of database file
    private static final String DATABASE_NAME = "bookstore.db";

    // database version. If database schema is changed, increment database version
    private static final int DATABASE_VERSION = 1;

    /**
     * construct a new instance of {@link BookDbHelper}.
     *
     * @param context of the app
     */
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * this is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create a String that contains SQL statement to create the books table
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_PRICE + " TEXT NOT NULL, "
                + BookEntry.COLUMN_QUANTITY + " INTERGER NOT NULL DEFAULT 0, "
                + BookEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT NOT NULL);";

        // execute the SQL statement
        db.execSQL(SQL_CREATE_BOOKS_TABLE);

        Log.v(TAG, "onCreate: this happened!!!!!!!!" );
    }

    // this is called when the database needs to be upgraded.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // the database is still version 1, so nothing to be done here
    }

}
