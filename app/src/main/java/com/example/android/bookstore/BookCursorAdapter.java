package com.example.android.bookstore;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bookstore.data.BookContract.BookEntry;

// {@link BookCursorAdapter} is an adapter for a list or grid view that uses a
// {@link Cursor} of book data as its data source. This adapter knows how to create
// list items for each row of book data in the {@link Cursor}.
public class BookCursorAdapter extends CursorAdapter {

    // log tab
    public static final String TAG = BookCursorAdapter.class.getSimpleName();

    // get context
    private Context mContext;

    // global variable for sale button. default is 1
    private int bookQuantity = 1;

    /**
     * {@link  BookCursorAdapter} is an adapter for a list or grid view
     * that uses a {@link Cursor} of pet data as its data source. This adapter knows
     * how to create list items for each row of book data in the {@link Cursor}.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.mContext = context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the title for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // find fields to populate in the list item layout
        TextView titleTextView = (TextView) view.findViewById(R.id.title_view);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_view);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_view);
        final Button saleButton = (Button) view.findViewById(R.id.sale_button);

        // find the columns of book attributes that we are interested in
        // product name, price, and quantity. plus a sale button that
        // decreases quantity when pushed
        int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

        // read the book attributes from the Cursor for the current book
        String productName = cursor.getString(titleColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);
        bookQuantity = cursor.getInt(quantityColumnIndex);

        // if the book supplier is empty string or null, then use some default
        // text that says "Unknown Price", so the TextView isn't blank.
        if (TextUtils.isEmpty(bookPrice)) {
            bookPrice = context.getString(R.string.unknown_price);
        }

        // onClick of Sale Button, decrease book quantity by 1
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bookQuantity == 0) {
                    saleButton.setEnabled(false);
                } else {
                    bookQuantity--;
                    quantityTextView.setText(String.valueOf(bookQuantity));
                }

                Log.i(TAG, "onClick: what is this error???? " + bookQuantity);
            }
        });

        Log.i(TAG, "bindView: what is this error????? " + bookQuantity);

        // update the TextViews and button view with the attributes for the current book.
        titleTextView.setText(productName);
        priceTextView.setText(context.getString(R.string.dollar_sign) + bookPrice);
        quantityTextView.setText(context.getString(R.string.quantity) + bookQuantity);
    }
}
