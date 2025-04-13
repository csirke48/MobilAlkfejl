package com.example.eskuvobolt;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.SearchView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ShopList extends AppCompatActivity {
    private static final String LOG_TAG = ShopList.class.getName();
    private FirebaseUser user;

    private RecyclerView mRecyclerView;
    private ArrayList<ShoppingItem> mItemList;
    private ShoppingItemAdapter mAdapter;

    private FrameLayout redCircle;
    private TextView contentTextView;

    private int gridNumber = 1;
    private boolean viewRow = true;
    private int cartItems = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Authenticated User!");
        } else {
            Log.d(LOG_TAG, "Unauthenticated User!");
            finish();
        }


        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();

        mAdapter = new ShoppingItemAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);

        initalizeData();
    }

    private void initalizeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemsRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        mItemList.clear();

        for (int i = 0; i < itemsList.length; i++) {
            mItemList.add(new ShoppingItem(itemsList[i], itemsInfo[i], itemsPrice[i], itemsRate.getFloat(i, 0), itemsImageResource.getResourceId(i, 0)));
        }

        itemsImageResource.recycle();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.log_out_button) {
            Log.d(LOG_TAG, "Log out clicked!");
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (id == R.id.settings_button) {
            Log.d(LOG_TAG, "Settings clicked!");
            return true;
        } else if (id == R.id.cart) {
            Log.d(LOG_TAG, "Cart clicked!");
            return true;
        } else if (id == R.id.view_selector) {
            Log.d(LOG_TAG, "View selector clicked!");
            if (viewRow) {
                changeSpanCount(item, R.drawable.view, 1);
            } else {
                changeSpanCount(item, R.drawable.grid, 2);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuItem);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon() {
        cartItems = (cartItems + 1);

        if (0 < cartItems) {
            contentTextView.setText(String.valueOf(cartItems));
        } else {
            contentTextView.setText("");
        }
    }

    public void logout(View view) {
        Log.d(LOG_TAG, "Log out clicked!");
        FirebaseAuth.getInstance().signOut();
        finish();
    }
}