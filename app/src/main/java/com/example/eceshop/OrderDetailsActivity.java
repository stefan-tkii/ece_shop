package com.example.eceshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderDetailsActivity extends AppCompatActivity implements PreordersRecyclerViewAdapter.OnItemClickListener, ComplexDialog.onProceedClicked
{

    private Toolbar toolbar;
    private TextView idTextView;
    private TextView priceTextView;
    private TextView statusTextView;
    private TextView addressTextView;
    private TextView dateTextView;
    private TextView etaTimeTextView;
    private AppCompatButton cancelButton;
    private RecyclerView orderItemsRecyclerView;

    private static AlertDialog progressDialog;
    private Order model;
    private String userId;
    private FirebaseDatabase database;
    private DatabaseReference mainRef;
    private OkHttpClient httpClient;
    private PreordersRecyclerViewAdapter adapter;
    private List<Product> products;
    private List<CartItem> adapterList;
    private int counter;
    private ComplexDialog complexDialog;

    private static final String BACKEND_URL = "http://192.168.1.106:4242/";
    private static final String CLICKED_ORDER_KEY = "com.example.eceshop.CLICKED_ORDER";
    private static final String CLICKED_KEY = "com.example.eceshop.CLICKED_PRODUCT";
    private static final String ORIGIN_KEY = "com.example.eceshop.ORIGIN_KEY";
    private static final String SELECT_OPTION = "com.example.eceshop.OPTION";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        toolbar = findViewById(R.id.order_details_toolbar);
        idTextView = findViewById(R.id.order_details_id);
        priceTextView = findViewById(R.id.order_details_price);
        statusTextView = findViewById(R.id.order_details_status);
        addressTextView = findViewById(R.id.order_details_address);
        dateTextView = findViewById(R.id.order_details_date);
        etaTimeTextView = findViewById(R.id.order_details_eta);
        cancelButton = findViewById(R.id.refundButton);
        orderItemsRecyclerView = findViewById(R.id.orderDetailsItems_RecyclerView);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        complexDialog = new ComplexDialog(this, "Request confirmation",
                "Are you sure you wish to proceed? These changes are irreversible.", this);

        progressDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        products = new ArrayList<>();
        adapterList = new ArrayList<>();
        adapter = new PreordersRecyclerViewAdapter(adapterList, this);

        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnProductClickListener(this);
        orderItemsRecyclerView.setAdapter(adapter);

        changeStatusBarColor();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");

        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if((System.currentTimeMillis() - model.getTimestamp()) < 86400000)
                {
                    complexDialog.show();
                }
                else
                {
                    CustomDialog dialog = new CustomDialog(OrderDetailsActivity.this, "Request error", "You can only cancel an order within the first 24 hours of making the order.", false);
                    dialog.show();
                }
            }
        });

        httpClient = new OkHttpClient();

        getIntentData();
    }

    private void getIntentData()
    {
        model = getIntent().getParcelableExtra(CLICKED_ORDER_KEY);
        if(model != null)
        {
            DatabaseReference firstRef = database.getReference("Orders");
            DatabaseReference secondRef = firstRef.child(userId).getRef();
            mainRef = secondRef.child(model.getOrderId()).getRef();
            String id = "ID: " + model.getOrderId();
            idTextView.setText(id);
            String totalPrice = "Total price: " + model.calculateTotalPrice() + "$";
            priceTextView.setText(totalPrice);
            String status = model.getStatus();
            statusTextView.setText(status);
            if(status.equals("Completed"))
            {
                cancelButton.setEnabled(false);
                statusTextView.setTextColor(getResources().getColor(R.color.yesColor));
            }
            else if(status.equals("Ongoing"))
            {
                statusTextView.setTextColor(getResources().getColor(R.color.pendingColor));
            }
            else if(status.equals("Cancelled"))
            {
                cancelButton.setEnabled(false);
                statusTextView.setTextColor(getResources().getColor(R.color.mainTextColor));
            }
            String addr = "Address: " + model.getAddress();
            addressTextView.setText(addr);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            long timestamp = model.getTimestamp();
            String date = sdf.format(new Date(timestamp));
            String dateText = "Ordered at: " + date;
            dateTextView.setText(dateText);
            String eta = "Estimated arrival time: 30 days";
            etaTimeTextView.setText(eta);
            List<String> productIds = new ArrayList<>();
            for(OrderProductContent content : model.getProducts())
            {
                productIds.add(content.getProductId());
            }
            progressDialog.show();
            counter = 0;
            getProducts(productIds);
        }
        else
        {
            CustomDialog dialog = new CustomDialog(OrderDetailsActivity.this, "Server/Network error", "Error loading this view could not obtain the required information.", false);
            dialog.show();
        }
    }

    private void getProducts(List<String> list)
    {
        if(list.size() == 0)
        {
            progressDialog.dismiss();
            adapter.notifyDataSetChanged();
            return;
        }
        DatabaseReference productRef = database.getReference("Products");
        DatabaseReference childRef = productRef.child(list.remove(0)).getRef();
        childRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    ProductDb item = snapshot.getValue(ProductDb.class);
                    if(item != null)
                    {
                        Product p = new Product(snapshot.getKey(), item.getName(), item.getShortDesc(), item.getLongDesc(), item.getImgUri(),
                                item.getPrice(), item.getOrders(), item.getCategoryId(), item.getInStock());
                        products.add(p);
                        CartItem cartItem = new CartItem(snapshot.getKey(), p.getName(), p.getShortDesc(), p.getImgUri(), p.getPrice(),
                                p.getOrders(), p.getInStock(), model.getProducts().get(counter).getQuantity(), false);
                        adapterList.add(cartItem);
                        counter++;
                        getProducts(list);
                    }
                    else
                    {
                        counter++;
                        getProducts(list);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(OrderDetailsActivity.this, "Database/Network error", error.getMessage(), false);
                dialog.show();
            }
        });
    }

    private void deleteOrder()
    {
        progressDialog.show();
        mainRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                counter = 0;
                                updateProducts();
                            }
                            else
                            {
                                progressDialog.dismiss();
                                CustomDialog dialog = new CustomDialog(OrderDetailsActivity.this, "Database/Network error", task.getException().getMessage(), false);
                                dialog.show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(OrderDetailsActivity.this, "Database/Network error", error.getMessage(), false);
                dialog.show();
            }
        });
    }

    private void updateProducts()
    {
        if(products.size() == 0)
        {
            performRefund();
            return;
        }
        DatabaseReference productRef = database.getReference("Products");
        DatabaseReference re = productRef.child(products.remove(0).getProductId()).getRef();
        re.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ProductDb p = snapshot.getValue(ProductDb.class);
                int stock = p.getInStock();
                int orders = p.getOrders() - 1;
                CartItem item = adapterList.get(counter);
                int newValue = stock + item.getQuantity();
                HashMap<String, Object> updatesMap = new HashMap<>();
                updatesMap.put("inStock", newValue);
                updatesMap.put("orders", orders);
                snapshot.getRef().updateChildren(updatesMap).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            counter++;
                            updateProducts();
                        }
                        else
                        {
                            progressDialog.dismiss();
                            CustomDialog dialog = new CustomDialog(OrderDetailsActivity.this, "Database/Network error", task.getException().getMessage(), false);
                            dialog.show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(OrderDetailsActivity.this, "Database/Network error", error.getMessage(), false);
                dialog.show();
            }
        });
    }

    private void performRefund()
    {
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        Map<String,Object> payMap = new HashMap<>();
        Map<String,Object> itemMap = new HashMap<>();
        List<Map<String,Object>> itemList = new ArrayList<>();
        payMap.put("currency", "usd");
        itemMap.put("paymentId", model.getPaymentId());
        itemList.add(itemMap);
        payMap.put("items", itemList);

        String json = new Gson().toJson(payMap);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "refund-payment")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new OrderDetailsActivity.RefundCallback(this));
    }

    private void onCompletedRefund()
    {
        Intent intent = new Intent(OrderDetailsActivity.this, HomeActivity.class);
        intent.putExtra(SELECT_OPTION, "Orders");
        startActivity(intent);
        CustomIntent.customType(OrderDetailsActivity.this, "right-to-left");
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(OrderDetailsActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                CustomIntent.customType(OrderDetailsActivity.this, "right-to-left");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(OrderDetailsActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        CustomIntent.customType(OrderDetailsActivity.this, "right-to-left");
        finish();
    }

    private void changeStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    @Override
    public void OnItemClick(int position)
    {
        Product p = products.get(position);
        Intent intent = new Intent(this, ProductDetailsActivity.class);
        intent.putExtra(CLICKED_KEY, p);
        intent.putExtra(ORIGIN_KEY, true);
        startActivity(intent);
        CustomIntent.customType(this, "left-to-right");
    }

    @Override
    public void onProceedButtonClicked()
    {
        deleteOrder();
    }

    private static final class RefundCallback implements Callback
    {

        @NonNull private final WeakReference<OrderDetailsActivity> activityRef;

        RefundCallback(@NonNull OrderDetailsActivity activity)
        {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e)
        {
            progressDialog.dismiss();
            final OrderDetailsActivity activity = activityRef.get();
            if (activity == null)
            {
                return;
            }
            activity.runOnUiThread(() ->
                    Toast.makeText(
                            activity, "Error: " + e.toString(), Toast.LENGTH_LONG
                    ).show()
            );
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
        {
            progressDialog.dismiss();
            final OrderDetailsActivity activity = activityRef.get();
            if (activity == null)
            {
                return;
            }
            if (!response.isSuccessful())
            {
                activity.runOnUiThread(() ->
                        activity.displayAlert("Error", response.message())
                );
            }
            else
            {
                activity.runOnUiThread(activity::onCompletedRefund
                );
            }
        }

    }

    private void displayAlert(@NonNull String title, @Nullable String message)
    {
        if(message != null)
        {
            CustomDialog dialog = new CustomDialog(this, title, message, false);
            dialog.show();
        }
        else
        {
            CustomDialog dialog = new CustomDialog(this, "Unknown error", "The reason and details behind this error are unknown.", false);
            dialog.show();
        }
    }

}