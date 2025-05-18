package com.example.eskuvobolt;

import static android.app.PendingIntent.FLAG_MUTABLE;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ShopListActivity extends AppCompatActivity {
    private static final String LOG_TAG = ShopListActivity.class.getName();
    private FirebaseUser user;

    private RecyclerView mRecyclerView;
    private ArrayList<ShoppingItem> mItemsData;
    private ShoppingItemAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;

    private FrameLayout redCircle;
    private TextView contentTextView;

    private int gridNumber = 1;
    private boolean viewRow = true;
    private int cartItems = 0;
    private int queryLimit = 25;
    private NotificationHandler mNotificationHandler;
    private AlarmManager mAlarmManager;

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
        mItemsData = new ArrayList<>();

        mAdapter = new ShoppingItemAdapter(this, mItemsData);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }


        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReciever, filter);

        mNotificationHandler = new NotificationHandler(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        setAlarmManager();
    }

    BroadcastReceiver powerReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                return;
            }

            switch (action) {
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 50;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 25;
                    break;
            }

            queryData();
        }
    };

    private void queryData() {
        mItemsData.clear();

        mItems.orderBy("cartedCount", Query.Direction.DESCENDING).limit(queryLimit)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ShoppingItem item = document.toObject(ShoppingItem.class);
                item.setId(document.getId());
                mItemsData.add(item);
            }

            if (mItemsData.size() == 0) {
                initalizeData();
                queryData();
            }

            mAdapter.notifyDataSetChanged();
        });
    }

    public void deleteItem(ShoppingItem item) {
        DocumentReference ref = mItems.document(item._getId());

        ref.delete().addOnSuccessListener(success -> {
                    Log.d(LOG_TAG, "Sikeresen törölve az item from the kosár: " + item._getId());
                })
                .addOnFailureListener(failure -> {
                    Toast.makeText(this, "Tárgy: " + item._getId() + " nem törölhető!", Toast.LENGTH_SHORT).show();
                });

        //mNotificationHandler.send("[-] " + item.getName());
        queryData();
        mNotificationHandler.cancel();
    }

    private void initalizeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemsRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        //mItemList.clear();

        for (int i = 0; i < itemsList.length; i++) {
            mItems.add(new ShoppingItem(itemsList[i],
                    itemsInfo[i],
                    itemsPrice[i],
                    itemsRate.getFloat(i, 0),
                    itemsImageResource.getResourceId(i, 0),
                    0));
            //mItemList.add(new ShoppingItem(itemsList[i], itemsInfo[i], itemsPrice[i], itemsRate.getFloat(i, 0), itemsImageResource.getResourceId(i, 0)));
        }

        itemsImageResource.recycle();
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);

        MenuItem cartItem = menu.findItem(R.id.cart);
        View actionView = cartItem.getActionView();

        redCircle = actionView.findViewById(R.id.view_alert_red_circle);
        contentTextView = actionView.findViewById(R.id.view_alert_count_textview);

        // Kosár ikon kattintás figyelése
        actionView.setOnClickListener(v -> {
            onOptionsItemSelected(cartItem); // vagy startActivity(new Intent(this, CartActivity.class));
        });

        //updateAlertIcon();

        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) menuItem.getActionView();

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

    public void updateAlertIcon(ShoppingItem item) {
        if (redCircle != null && contentTextView != null) {
            if (cartItems > 0) {
                redCircle.setVisibility(View.VISIBLE);
                contentTextView.setText(String.valueOf(cartItems));
            } else {
                redCircle.setVisibility(View.GONE);
            }
        }

        mItems.document(item._getId()).update("cartedCount", item.getCartedCount()+1)
                .addOnFailureListener(failure -> {
                    Toast.makeText(this, "Nem lehet megváltoztatni: " + item._getId(), Toast.LENGTH_SHORT).show();
                });

        mNotificationHandler.send("[+] " + item.getName());
    }



    public void logout(View view) {
        Log.d(LOG_TAG, "Log out clicked!");
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReciever);
    }

    private void setAlarmManager() {
        long repeatInterval = 20*1000; //AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, FLAG_MUTABLE);

        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                repeatInterval,
                pendingIntent);

        //mAlarmManager.cancel(pendingIntent); // kikapcs
    }

}