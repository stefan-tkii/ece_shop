package com.example.eceshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blongho.country_data.World;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import maes.tech.intentanim.CustomIntent;

public class UserDetailsActivity extends AppCompatActivity implements OrderRecyclerViewAdapter.OnOrderClickListener
{

    private Toolbar toolbar;
    private NestedScrollView container;
    private FloatingActionButton backToTop;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private ImageView flagImageView;
    private TextView countryTextView;
    private TextView totalOrdersTextView;
    private TextView totalAmountTextView;
    private AutoCompleteTextView sortSelector;
    private RecyclerView ordersRecyclerView;
    private ProgressBar loadBar;
    private TextView placeholderTextView;

    private static final int BATCH_SIZE = 6;
    private ArrayAdapter<String> arrayOrdersSort;

    private static final String USER_DATA_KEY = "com.example.eceshop.CLICKED_USER";
    private static final String CLICKED_ORDER_KEY = "com.example.eceshop.CLICKED_ORDER";
    private static final String USER_ID_KEY = "com.example.eceshop.CLICKED_USER_ID";
    private static final String ADMIN_KEY = "com.example.eceshop.Admin";
    private static final String KIRO_GLIGOROV = "Macedonia (FYROM)";
    private static final String ZORAN_ZAEV = "North Macedonia";

    private DatabaseReference mainRef;

    private UserRvItem model;
    private boolean running;
    private boolean loadMore;
    private String sortBy;
    private String nextKey;
    private String status;
    private List<Order> ordersList;
    private OrderRecyclerViewAdapter ordersAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        toolbar = findViewById(R.id.user_details_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        World.init(getApplicationContext());

        container = findViewById(R.id.user_details_main_container);
        backToTop = findViewById(R.id.backToTopUserDetails);
        nameTextView = findViewById(R.id.user_details_name);
        emailTextView = findViewById(R.id.user_details_email);
        phoneTextView = findViewById(R.id.user_details_phone);
        flagImageView = findViewById(R.id.user_details_country_flag);
        countryTextView = findViewById(R.id.user_details_country_name);
        totalOrdersTextView = findViewById(R.id.user_details_orders_label);
        totalAmountTextView = findViewById(R.id.user_details_money_label);
        sortSelector = findViewById(R.id.sort_user_orders);
        ordersRecyclerView = findViewById(R.id.user_details_orders_recyclerView);
        loadBar = findViewById(R.id.user_details_load_bar);
        placeholderTextView = findViewById(R.id.user_details_placeholder);

        sortBy = "Status: Ongoing";
        status = "Ongoing";
        nextKey = "";
        ordersList = new ArrayList<>();
        running = false;
        loadMore = true;

        changeStatusBarColor();

        container.setOnTouchListener(new View.OnTouchListener()
        {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(sortSelector.isFocused())
                {
                    sortSelector.clearFocus();
                    return true;
                }
                return false;
            }
        });

        container.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener()
        {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
            {
                if (scrollY > oldScrollY)
                {
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            backToTop.setVisibility(View.GONE);
                        }
                    }, 2000);
                }
                if (scrollY < oldScrollY)
                {
                    backToTop.setVisibility(View.VISIBLE);
                }
                if (scrollY == 0)
                {
                    backToTop.setVisibility(View.GONE);
                }
                if(scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()))
                {
                    if(!running)
                    {
                        if(loadMore)
                        {
                            backToTop.setVisibility(View.VISIBLE);
                            loadBar.setVisibility(View.VISIBLE);
                            getUserOrders();
                        }
                        else
                        {
                            backToTop.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        backToTop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                container.smoothScrollTo(0, 0);
            }
        });

        placeholderTextView.setVisibility(View.GONE);
        loadBar.setVisibility(View.GONE);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersAdapter = new OrderRecyclerViewAdapter(this, ordersList);
        ordersAdapter.setOnOrderClickListener(this);
        ordersRecyclerView.setAdapter(ordersAdapter);

        model = getIntent().getParcelableExtra(USER_DATA_KEY);
        if(model != null)
        {
            loadUserData();
        }
        else
        {
            CustomDialog dialog = new CustomDialog(this, "Database/Network error", "Could not obtain user information.", false);
            dialog.show();
        }
    }

    private void loadUserData()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference parentRef = database.getReference("Orders");
        mainRef = parentRef.child(model.getUserId()).getRef();

        nameTextView.setText(model.getFullName());
        emailTextView.setText(model.getEmail());
        String phone;
        if(!model.getPhoneNumber().equals("N/A"))
        {
            phone = "+" + model.getPhoneNumber();
        }
        else
        {
            phone = "N/A";
        }
        phoneTextView.setText(phone);
        String country;
        int flagNum;
        if(!model.getCountry().equals("N/A"))
        {
            country = model.getCountry();
            if(country.equals(KIRO_GLIGOROV))
            {
                country = ZORAN_ZAEV; //go prodadovme imeto
            }
            flagNum = World.getFlagOf(country.toLowerCase());
        }
        else
        {
            country = "N/A";
            flagNum = World.getWorldFlag();
        }
        if(country.equals(ZORAN_ZAEV))
        {
            country = KIRO_GLIGOROV; // go vrativme imeto
        }
        countryTextView.setText(country);
        flagImageView.setImageResource(flagNum);

        String totalOrder = "Total orders: 0";
        totalOrdersTextView.setText(totalOrder);

        String totalAmount = "Total amount : 0$";
        totalAmountTextView.setText(totalAmount);

        loadBar.setVisibility(View.VISIBLE);
        getUserOrders();
    }

    private void getUserOrders()
    {
        Query q = getOrderQuery();
        if(q != null)
        {
            running = true;
            q.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        boolean added = false;
                        for(DataSnapshot snap : snapshot.getChildren())
                        {
                            String id = snap.getKey();
                            OrderDb value = snap.getValue(OrderDb.class);
                            if(value != null)
                            {
                                Order item = createOrder(id, value);
                                if(item.getStatus().equals(status))
                                {
                                    ordersList.add(item);
                                    added = true;
                                }
                                nextKey = id;
                            }
                        }
                        running = false;
                        if(!added)
                        {
                            getUserOrders();
                            return;
                        }
                        loadBar.setVisibility(View.GONE);
                        if(ordersList.size() > 0)
                        {
                            double total = 0d;
                            int count = ordersList.size();
                            for(Order o : ordersList)
                            {
                                total = total + o.calculateTotalPrice();
                            }
                            String totalOrders = "Total orders: " + count;
                            totalOrdersTextView.setText(totalOrders);
                            String totalAmount = "Total amount: " + total + "$";
                            totalAmountTextView.setText(totalAmount);
                            ordersAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            String totalOrder = "Total orders: 0";
                            totalOrdersTextView.setText(totalOrder);
                            String totalAmount = "Total amount : 0$";
                            totalAmountTextView.setText(totalAmount);
                            loadMore = false;
                            placeholderTextView.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        running = false;
                        loadMore = false;
                        loadBar.setVisibility(View.GONE);
                        if(ordersList.size() == 0)
                        {
                            String totalOrder = "Total orders: 0";
                            totalOrdersTextView.setText(totalOrder);
                            String totalAmount = "Total amount : 0$";
                            totalAmountTextView.setText(totalAmount);
                            placeholderTextView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            Toast.makeText(UserDetailsActivity.this, "No more orders left to display for this filtering.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    running = false;
                    CustomDialog dialog = new CustomDialog(UserDetailsActivity.this, "Database/Network error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
        else
        {
            Toast.makeText(this, "Cannot sort orders in this way.", Toast.LENGTH_SHORT).show();
        }
    }

    private Query getOrderQuery()
    {
        if(sortBy.equals("Status: Ongoing") || sortBy.equals("Status: Completed") || sortBy.equals("Status: Cancelled"))
        {
            if(nextKey.equals(""))
            {
                return mainRef.orderByKey().limitToFirst(BATCH_SIZE);
            }
            return mainRef.orderByKey().startAfter(nextKey).limitToFirst(BATCH_SIZE);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void OnOrderClick(int position, Order data)
    {
        Intent intent = new Intent(UserDetailsActivity.this, OrderDetailsActivity.class);
        intent.putExtra(CLICKED_ORDER_KEY, data);
        intent.putExtra(ADMIN_KEY, true);
        intent.putExtra(USER_ID_KEY, model.getUserId());
        intent.putExtra(USER_DATA_KEY, model);
        startActivity(intent);
        CustomIntent.customType(UserDetailsActivity.this, "left-to-right");
    }

    private Order createOrder(String id, OrderDb item)
    {
        Map<String, OrderProductContentDb> map = item.getProducts();
        List<String> keys = new ArrayList<>();
        for(String key : map.keySet())
        {
            keys.add(key);
        }
        List<OrderProductContent> productsList = new ArrayList<>();
        for(String k : keys)
        {
            OrderProductContentDb value = map.get(k);
            OrderProductContent content = new OrderProductContent(k, value.getPrice(), value.getQuantity());
            productsList.add(content);
        }
        Order retVal= new Order(id, item.getAddress(), item.getPaymentId(), item.getPaymentMethodId(), productsList, item.getStatus(), item.getTimestamp());
        return retVal;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        String[] sortOptions = getResources().getStringArray(R.array.sortUserOrders);
        arrayOrdersSort = new ArrayAdapter<>(this, R.layout.category_dropdown_item, sortOptions);
        sortSelector.setAdapter(arrayOrdersSort);

        sortSelector.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String item = arrayOrdersSort.getItem(position);
                sortSelector.clearFocus();

                if(!sortBy.equals(item))
                {
                    sortBy = item;
                    String helper = sortBy.split(":")[1];
                    status = helper.trim();
                    nextKey = "";
                    ordersList.clear();
                    ordersAdapter.notifyDataSetChanged();
                    loadMore = true;
                    placeholderTextView.setVisibility(View.GONE);
                    loadBar.setVisibility(View.VISIBLE);
                    getUserOrders();
                }
                else
                {
                    Toast.makeText(UserDetailsActivity.this, "Already displaying the orders through this filtering.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(UserDetailsActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                CustomIntent.customType(UserDetailsActivity.this, "right-to-left");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(UserDetailsActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        CustomIntent.customType(UserDetailsActivity.this, "right-to-left");
        finish();
    }

    private void changeStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

}