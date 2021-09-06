package com.example.eceshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.cache.DiskLruCache;

public class PaymentActivity extends AppCompatActivity
{
    private static final String BACKEND_URL = "http://192.168.1.104:4242/";
    private static final String PUBLISHABLE_KEY = "pk_test_51JVv5lGfuf9wZawU43O2I3f7KDT01SDh2LeKBTKOUs656H4s5vTJ0dndQaEON3TyOilFcbP1qCcQCNmVUqglzU3Q00qaQ7TiR8";
    private static final String SELECT_OPTION = "com.example.eceshop.OPTION";

    private ConstraintLayout container;
    private Toolbar toolbar;
    private CardInputWidget cardInputWidget;
    private AppCompatButton payButton;
    private TextView totalAmountTextView;
    private RecyclerView preordersRecyclerView;

    private PreordersRecyclerViewAdapter preordersAdapter;
    private CartItem model;
    private ArrayList<CartItem> model_array;
    private double totalPrice;
    private String userId;
    private String orderId;
    private boolean checkFlag;
    private boolean refreshCart;

    private static AlertDialog progressDialog;

    private Stripe stripe;
    private String paymentIntentClientSecret;
    private OkHttpClient httpClient;
    private FirebaseDatabase database;

    private static final String BUTTON_KEY = "com.example.eceshop.INTENT_ORIGIN";
    private static final String DATA = "com.example.eceshop.DATA_KEY";
    private static final String LOAD_KEY = "com.example.eceshop.LOAD_MORE";
    private static final String SORT_KEY = "com.example.eceshop.SORT";
    private static final String NEXT_KEY = "com.example.eceshop.NEXT_ID";
    private static final String NEXT_PRICE = "com.example.eceshop.NEXT_PRICE";
    private static final String NEXT_ORDERS = "com.example.eceshop.NEXT_ORDERS";
    private static final String NEXT_TIMESTAMP = "com.example.eceshop.NEXT_TIME";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        container = findViewById(R.id.payment_container);
        toolbar = findViewById(R.id.payment_toolbar);
        cardInputWidget = findViewById(R.id.cardInputWidget);
        payButton = findViewById(R.id.payButton);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        preordersRecyclerView = findViewById(R.id.preOrderRecyclerView);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        changeStatusBarColor();

        totalPrice = 0.0d;
        orderId = "";
        checkFlag = false;
        refreshCart = false;
        database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        model_array = new ArrayList<>();
        preordersAdapter = new PreordersRecyclerViewAdapter(model_array, this);
        preordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        preordersRecyclerView.setAdapter(preordersAdapter);

        progressDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        container.setOnTouchListener(new View.OnTouchListener()
        {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = getCurrentFocus();
                if (focusedView != null)
                {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    if(checkFocusRec(cardInputWidget))
                    {
                        clearFocus(cardInputWidget);
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        payButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                performTransaction();
            }
        });

        progressDialog.show();

        httpClient = new OkHttpClient();

        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull(PUBLISHABLE_KEY)
        );

        if(checkFocusRec(cardInputWidget))
        {
            clearFocus(cardInputWidget);
        }

        DatabaseReference firstRef = database.getReference("Orders").child(userId).getRef();
        orderId = firstRef.push().getKey();
        initializeClientSecret();
    }

    private void getIntentData()
    {
        String origin = getIntent().getStringExtra(BUTTON_KEY);
        if(origin.equals("orderOne"))
        {
            model = getIntent().getParcelableExtra(DATA);
            model_array.add(model);
            totalPrice = model.getPrice()*model.getQuantity();
            String toSet = String.valueOf(totalPrice) + "$";
            totalAmountTextView.setText(toSet);
            progressDialog.dismiss();
            preordersAdapter.notifyDataSetChanged();
        }
        else if(origin.equals("orderAll"))
        {
            List<CartItem> arr = new ArrayList<>();
            arr = getIntent().getParcelableArrayListExtra(DATA);
            boolean load_more = getIntent().getBooleanExtra(LOAD_KEY, false);
            if(load_more)
            {
                String sortBy = getIntent().getStringExtra(SORT_KEY);
                if(sortBy.equals("Latest") || sortBy.equals("Oldest"))
                {
                    long nextTimestamp = getIntent().getLongExtra(NEXT_TIMESTAMP, 0L);
                    double nextPrice = 0.0d;
                    String nextKey = "";
                    int nextOrders = -1;
                    getCartItems(arr, sortBy, nextTimestamp, nextPrice, nextOrders, nextKey);
                }
                else if(sortBy.equals("Price ascending") || sortBy.equals("Price descending"))
                {
                    double nextPrice = getIntent().getDoubleExtra(NEXT_PRICE, 0.0d);
                    String nextKey = getIntent().getStringExtra(NEXT_KEY);
                    long nextTimestamp = 0L;
                    int nextOrders = -1;
                    getCartItems(arr, sortBy, nextTimestamp, nextPrice, nextOrders, nextKey);
                }
                else if(sortBy.equals("Orders"))
                {
                    int nextOrders = getIntent().getIntExtra(NEXT_ORDERS, -1);
                    String nextKey = getIntent().getStringExtra(NEXT_KEY);
                    long nextTimestamp = 0L;
                    double nextPrice = 0.0d;
                    getCartItems(arr, sortBy, nextTimestamp, nextPrice, nextOrders, nextKey);
                }
            }
            else
            {
                processList(arr);
            }
        }
    }

    private void initializeClientSecret()
    {
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        Map<String,Object> payMap = new HashMap<>();
        Map<String,Object> itemMap = new HashMap<>();
        List<Map<String,Object>> itemList = new ArrayList<>();
        payMap.put("currency","usd");
        itemMap.put("id", orderId);
        itemMap.put("amount", totalPrice);
        itemList.add(itemMap);
        payMap.put("items",itemList);

        String json = new Gson().toJson(payMap);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new PayCallback(this));

        getIntentData();
    }

    private void processList(List<CartItem> items)
    {
        List<CartItem> toAdd = new ArrayList<>();
        for(CartItem item : items)
        {
            if(item.getInStock() != 0)
            {
                toAdd.add(item);
            }
        }
        items.clear();
        for(CartItem item : toAdd)
        {
            items.add(item);
        }
        for(CartItem item : items)
        {
            totalPrice = totalPrice + item.getQuantity()*item.getPrice();
        }
        String toSet = String.valueOf(totalPrice) + "$";
        totalAmountTextView.setText(toSet);
        progressDialog.dismiss();
        for(CartItem m : items)
        {
            model_array.add(m);
        }
        preordersAdapter.notifyDataSetChanged();
    }

    private void performTransaction()
    {
        PaymentMethodCreateParams input = cardInputWidget.getPaymentMethodCreateParams();
        if(totalPrice == 0.0d || model_array.size() == 0)
        {
            CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Database/Network error", "No products are present to perform a transaction.", false);
            dialog.show();
        }
        else if(input == null)
        {
            CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Input error", "Please fill in your credit card details.", false);
            dialog.show();
        }
        else
        {
            progressDialog.show();
            List<String> idList = new ArrayList<>();
            for(CartItem t : model_array)
            {
                idList.add(t.getProductId());
            }
            checkInStock(idList);
        }
    }

    private void continueWithTransaction()
    {
        if(checkFlag)
        {
            progressDialog.dismiss();
            CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Order error", "The products you seek to order are no longer in stock, please check your cart again.", false);
            dialog.show();
            payButton.setEnabled(false);
            refreshCart = true;
        }
        else
        {
            makeStripePayment();
        }
    }

    private void continueWithTransactionAfterPayment()
    {
        DatabaseReference secondRef = database.getReference("Orders").child(userId).child(orderId).getRef();
        final HashMap<String, Object> ordersMap = new HashMap<>();
        ordersMap.put("Timestamp", System.currentTimeMillis());
        secondRef.updateChildren(ordersMap).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    DatabaseReference thirdRef = database.getReference("Orders").child(userId).child(orderId).child("Products").getRef();
                    final HashMap<String, Object> idMap = new HashMap<>();
                    for(CartItem t : model_array)
                    {
                        double price = t.getQuantity()*t.getPrice();
                        idMap.put(t.getProductId(), price);
                    }
                    thirdRef.updateChildren(idMap).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                List<String> ids = new ArrayList<>();
                                for(CartItem t : model_array)
                                {
                                    ids.add(t.getProductId());
                                }
                                DatabaseReference mainRef = database.getReference("Carts");
                                DatabaseReference userIdRef = mainRef.child(userId).getRef();
                                DatabaseReference cartItemsRef = userIdRef.child("CartItems");
                                deleteCartProducts(ids, cartItemsRef);
                            }
                            else
                            {
                                secondRef.addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                    {
                                        snapshot.getRef().removeValue();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error)
                                    {
                                        progressDialog.dismiss();
                                        CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Order error", error.getMessage(), false);
                                        dialog.show();
                                    }
                                });
                                progressDialog.dismiss();
                                CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Order error", task.getException().getMessage(), false);
                                dialog.show();
                            }
                        }
                    });
                }
                else
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Order error", task.getException().getMessage(), false);
                    dialog.show();
                }
            }
        });
    }

    private void removeAfterItemDuplicate(List<CartItem> items)
    {
        boolean flag = false;
        int i = 0;
        CartItem prev = new CartItem();
        for(CartItem c : items)
        {
            if(i == 0)
            {
                prev.setAll(c);
            }
            else
            {
                if(c.equals(prev))
                {
                   items.remove(i);
                   flag = true;
                   break;
                }
                else
                {
                    prev.setAll(c);
                }
            }
            i++;
        }
        if(flag)
        {
            removeAfterItemDuplicate(items);
        }
        else
        {
            processList(items);
        }
    }

    private void getCartItems(List<CartItem> items, String sort, long timestamp, Double price, int orders, String key)
    {
        Query q = getCartDbQuery(sort, timestamp, price, orders, key);
        if(q != null)
        {
            q.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        List<String> idList = new ArrayList<>();
                        for(DataSnapshot snap : snapshot.getChildren())
                        {
                            idList.add(snap.getKey());
                        }
                        getProducts(idList, items);
                    }
                    else
                    {
                        processList(items);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Database/Network error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
        else
        {
            CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Database/Network error", "Could not query the database.", false);
            dialog.show();
        }
    }

    private Query getCartDbQuery(String sort, long timestamp, Double price, int orders, String key)
    {
        DatabaseReference cartRef = database.getReference("Carts");
        DatabaseReference childRef = cartRef.child(userId).getRef();
        DatabaseReference itemsRef = childRef.child("CartItems").getRef();
        if(sort.equals("Latest"))
        {
            return itemsRef.orderByChild("timestamp").startAfter(timestamp);
        }
        else if(sort.equals("Oldest"))
        {
            return itemsRef.orderByChild("timestamp").endBefore(timestamp);
        }
        else if(sort.equals("Price ascending"))
        {
            return itemsRef.orderByChild("price").startAfter(price, key);
        }
        else if(sort.equals("Price descending"))
        {
            return itemsRef.orderByChild("price").endBefore(price, key);
        }
        else if(sort.equals("Orders"))
        {
            return itemsRef.orderByChild("orders").endBefore(orders, key);
        }
        return null;
    }

    private void getProducts(List<String> list, List<CartItem> items)
    {
        if(list.size() == 0)
        {
            removeAfterItemDuplicate(items);
            return;
        }
        else
        {
            DatabaseReference productRef = database.getReference("Products");
            DatabaseReference childRef = productRef.child(list.remove(0)).getRef();
            childRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        ProductDb model = snapshot.getValue(ProductDb.class);
                        int quantity;
                        if(model.getInStock() > 0)
                        {
                            quantity = 1;
                        }
                        else
                        {
                            quantity = 0;
                        }
                        CartItem item = new CartItem(snapshot.getKey(),
                                model.getName(),
                                model.getShortDesc(),
                                model.getImgUri(),
                                model.getPrice(), model.getOrders(), model.getInStock(), quantity, false);
                        items.add(item);
                        getProducts(list, items);
                    }
                    else
                    {
                        progressDialog.dismiss();
                        list.clear();
                        CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Database/Network error", "Error could not obtain the products.", false);
                        dialog.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Database/Network error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
    }

    private void deleteCartProducts(List<String> list, DatabaseReference reference)
    {
        if(list.size() == 0)
        {
            List<String> idsList = new ArrayList<>();
            for(CartItem t : model_array)
            {
                idsList.add(t.getProductId());
            }
            DatabaseReference r = database.getReference("Products");
            updateProducts(idsList, r);
            return;
        }
        DatabaseReference ref = reference.child(list.remove(0)).getRef();
        ref.addListenerForSingleValueEvent(new ValueEventListener()
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
                                deleteCartProducts(list, reference);
                            }
                            else
                            {
                                progressDialog.dismiss();
                                CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Database/Network error", task.getException().getMessage(), false);
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
                CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Database/Network error", error.getMessage(), false);
                dialog.show();
            }
        });
    }

    private void updateProducts(List<String> list, DatabaseReference ref)
    {
        if(list.size() == 0)
        {
            progressDialog.dismiss();
            Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);
            intent.putExtra(SELECT_OPTION, "Orders");
            startActivity(intent);
            CustomIntent.customType(PaymentActivity.this, "right-to-left");
            finish();
            return;
        }
        DatabaseReference re = ref.child(list.remove(0)).getRef();
        re.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                ProductDb p = snapshot.getValue(ProductDb.class);
                int stock = p.getInStock();
                int orders = p.getOrders() + 1;
                for(CartItem m : model_array)
                {
                    if(m.getProductId().equals(snapshot.getKey()))
                    {
                        int newValue = stock - m.getQuantity();
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
                                    updateProducts(list, ref);
                                }
                                else
                                {
                                    progressDialog.dismiss();
                                    CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Database/Network error", task.getException().getMessage(), false);
                                    dialog.show();
                                }
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Database/Network error", error.getMessage(), false);
                dialog.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);
                if(!refreshCart)
                {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }
                else
                {
                    intent.putExtra(SELECT_OPTION, "Cart");
                }
                startActivity(intent);
                CustomIntent.customType(PaymentActivity.this, "right-to-left");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);
        if(!refreshCart)
        {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        else
        {
            intent.putExtra(SELECT_OPTION, "Cart");
        }
        startActivity(intent);
        CustomIntent.customType(PaymentActivity.this, "right-to-left");
        finish();
    }

    private void changeStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    private boolean checkFocusRec(View view)
    {
        if (view.isFocused())
        {
            return true;
        }

        if (view instanceof ViewGroup)
        {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
            {
                if (checkFocusRec(viewGroup.getChildAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void clearFocus(ViewGroup viewGroup)
    {
        for (int i = 0; i < viewGroup.getChildCount(); i++)
        {
            if(checkFocusRec(viewGroup.getChildAt(i)))
            {
                viewGroup.getChildAt(i).clearFocus();
            }
        }
    }

    private void checkInStock(List<String> list)
    {
        if (list.size() == 0)
        {
            continueWithTransaction();
            return;
        }
        else
        {
            DatabaseReference productRef = database.getReference("Products");
            DatabaseReference childRef = productRef.child(list.remove(0)).getRef();
            childRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    ProductDb model = snapshot.getValue(ProductDb.class);
                    if(model.getInStock() == 0)
                    {
                        checkFlag = true;
                    }
                    checkInStock(list);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(PaymentActivity.this, "Database/Network error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
    }

    private void makeStripePayment()
    {
        PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
        ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
        stripe.confirmPayment(this, confirmParams);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException
    {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).string(),
                type
        );
        paymentIntentClientSecret = responseMap.get("clientSecret");
    }

    private static final class PayCallback implements Callback
    {
        @NonNull private final WeakReference<PaymentActivity> activityRef;

        PayCallback(@NonNull PaymentActivity activity)
        {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e)
        {
            progressDialog.dismiss();
            final PaymentActivity activity = activityRef.get();
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
        public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException
        {
            final PaymentActivity activity = activityRef.get();
            if (activity == null)
            {
                return;
            }
            if (!response.isSuccessful())
            {
                activity.runOnUiThread(() ->
                        Toast.makeText(
                                activity, "Error: " + response.toString(), Toast.LENGTH_LONG
                        ).show()
                );
            }
            else
            {
                activity.onPaymentSuccess(response);
            }
        }
    }
    private static final class PaymentResultCallback implements ApiResultCallback<PaymentIntentResult>
    {
        @NonNull private final WeakReference<PaymentActivity> activityRef;

        PaymentResultCallback(@NonNull PaymentActivity activity)
        {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result)
        {
            progressDialog.dismiss();
            final PaymentActivity activity = activityRef.get();
            if (activity == null)
            {
                return;
            }
            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded)
            {
                activity.continueWithTransactionAfterPayment();
            }
            else if (status == PaymentIntent.Status.RequiresPaymentMethod)
            {
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage()
                );
            }
        }
        @Override
        public void onError(@NonNull Exception e)
        {
            progressDialog.dismiss();
            final PaymentActivity activity = activityRef.get();
            if (activity == null)
            {
                return;
            }
            activity.displayAlert("Error", e.toString());
        }
    }

    private void displayAlert(@NonNull String title, @Nullable String message)
    {
        if(title != null && message != null)
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