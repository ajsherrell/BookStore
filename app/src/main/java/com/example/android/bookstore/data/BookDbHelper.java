package com.example.android.bookstore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookContract.BookEntry.TABLE_NAME + " ("
                + BookContract.BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookContract.BookEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + BookContract.BookEntry.COLUMN_PRICE + " INTEGER, "
                + BookContract.BookEntry.COLUMN_QUANTITY + " INTERGER NOT NULL DEFAULT 0, "
                + BookContract.BookEntry.COLUMN_SUPPLIER_NAME + " TEXT, "
                + BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT);";

        // execute the SQL statement
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    // this is called when the database needs to be upgraded.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // the database is still version 1, so nothing to be done here
    }

}
