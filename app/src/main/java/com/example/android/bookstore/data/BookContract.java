package com.example.android.bookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {

    // string constant for the content authority
    public static final String CONTENT_AUTHORITY = "com.example.android.bookstore";

    // scheme string constant
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // store the path for each table
    public static final String PATH_BOOKS = "books";

    // to prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private BookContract() {}

    // inner class for books database table. each entry represents a single book.
    public static abstract class BookEntry implements BaseColumns {

        // create full URI for the class as a constant
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        // the MIME type of the {@link CONTENT_URI} for a list of books.
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // the MIME type of the {@link CONTENT_URI} for a single book.
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // string constants for the columns
        public static final String TABLE_NAME = "books";
        public static final String COLUMN_PRODUCT_NAME = "product";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "phone";

    }

}
