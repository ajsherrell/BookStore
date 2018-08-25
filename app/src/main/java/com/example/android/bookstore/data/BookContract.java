package com.example.android.bookstore.data;

import android.provider.BaseColumns;

public final class BookContract {

    // to prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private BookContract() {}

    // inner class for books database table. each entry represents a single book.
    public static abstract class BookEntry implements BaseColumns {

        // unique ID number for each book (int)
        public final static String _ID = BaseColumns._ID;

        // string constants for the columns
        public static final String TABLE_NAME = "books";
        public static final String COLUMN_PRODUCT_NAME = "product name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier name";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "phone number";

    }

}
