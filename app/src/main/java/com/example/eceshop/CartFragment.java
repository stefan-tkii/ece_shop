package com.example.eceshop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class CartFragment extends Fragment implements CartItemRecyclerViewAdapter.OnCartButtonClickListener
{
    private NestedScrollView cartContainer;
    private FloatingActionButton backToTop;
    private AutoCompleteTextView sortSelector;
    private RecyclerView cartRecyclerView;
    private TextView placeholderView;
    private ProgressBar loadBar;
    private AppCompatButton orderAllBtn;

    private CartFragmentTouchListener listener;
    private AlertDialog progressDialog;
    private ArrayAdapter<String> arrayAdapterSort;
    private CartItemRecyclerViewAdapter itemsAdapter;

    private ArrayList<CartItem> cartItems;
    private FirebaseDatabase database;
    private String userId;
    private String sortBy;
    private boolean loadMore;
    private long nextTimeStamp;
    private double nextPrice;
    private int nextOrders;
    private String nextKey;
    private static final int BATCH_SIZE = 3;
    private int prevSize;

    private long prevTimeStamp;
    private double prevPrice;
    private String prevKey;

    private long afterTimeStamp;
    private double afterPrice;
    private String afterKey;
    private int afterOrders;

    private boolean running;

    private static final String BUTTON_KEY = "com.example.eceshop.INTENT_ORIGIN";
    private static final String DATA = "com.example.eceshop.DATA_KEY";
    private static final String LOAD_KEY = "com.example.eceshop.LOAD_MORE";
    private static final String SORT_KEY = "com.example.eceshop.SORT";
    private static final String NEXT_KEY = "com.example.eceshop.NEXT_ID";
    private static final String NEXT_PRICE = "com.example.eceshop.NEXT_PRICE";
    private static final String NEXT_ORDERS = "com.example.eceshop.NEXT_ORDERS";
    private static final String NEXT_TIMESTAMP = "com.example.eceshop.NEXT_TIME";

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.cart_fragment, container, false);

        cartContainer = root.findViewById(R.id.cart_container);
        backToTop = root.findViewById(R.id.backToTopCart);
        sortSelector = root.findViewById(R.id.cart_Sort_Selector);
        cartRecyclerView = root.findViewById(R.id.cartItems_RecyclerView);
        placeholderView = root.findViewById(R.id.cartPlaceholder);
        loadBar = root.findViewById(R.id.cart_item_load_bar);
        orderAllBtn = root.findViewById(R.id.orderAllBtn);

        running = false;
        prevSize = 0;

        sortBy = "Latest";
        resetKeys();
        loadMore = true;
        cartItems = new ArrayList<>();
        database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");

        progressDialog = new SpotsDialog.Builder()
                .setContext(getActivityNonNull())
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        cartContainer.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                listener.onCartFragmentTouch();
                InputMethodManager imm = (InputMethodManager) getActivityNonNull().getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = getActivityNonNull().getCurrentFocus();
                if (focusedView != null)
                {
                    imm.hideSoftInputFromWindow(getActivityNonNull().getCurrentFocus().getWindowToken(), 0);
                    if(sortSelector.isFocused())
                    {
                        sortSelector.clearFocus();
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        cartContainer.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener()
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
                    backToTop.setVisibility(View.VISIBLE);
                    if(loadMore)
                    {
                        if(!running)
                        {
                            loadBar.setVisibility(View.VISIBLE);
                            getCartProducts(sortBy, nextTimeStamp, nextPrice, nextOrders, nextKey);
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
                cartContainer.smoothScrollTo(0,0);
            }
        });

        orderAllBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                orderAll();
            }
        });

        placeholderView.setVisibility(View.GONE);

        ((SimpleItemAnimator) Objects.requireNonNull(cartRecyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        cartRecyclerView.setDrawingCacheEnabled(true);
        cartRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getActivityNonNull()));
        itemsAdapter = new CartItemRecyclerViewAdapter(getActivityNonNull(), cartItems);
        itemsAdapter.setOnCartButtonClickListener(this);
        cartRecyclerView.setAdapter(itemsAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        loadBar.setVisibility(View.VISIBLE);

        getCartProducts(sortBy, nextTimeStamp, nextPrice, nextOrders, nextKey);

        return  root;
    }

    private void orderAll()
    {
        Intent intent = new Intent(getActivityNonNull(), PaymentActivity.class);
        intent.putExtra(BUTTON_KEY, "orderAll");
        intent.putParcelableArrayListExtra(DATA, cartItems);
        if(loadMore)
        {
            intent.putExtra(LOAD_KEY, true);
            intent.putExtra(SORT_KEY, sortBy);
            if(sortBy.equals("Latest") || sortBy.equals("Oldest"))
            {
                intent.putExtra(NEXT_TIMESTAMP, nextTimeStamp);
            }
            else if(sortBy.equals("Price ascending") || sortBy.equals("Price descending"))
            {
                intent.putExtra(NEXT_PRICE, nextPrice);
                intent.putExtra(NEXT_KEY, nextKey);
            }
            else if(sortBy.equals("Orders"))
            {
                intent.putExtra(NEXT_ORDERS, nextOrders);
                intent.putExtra(NEXT_KEY, nextKey);
            }
        }
        else
        {
            intent.putExtra(LOAD_KEY, false);
        }
        startActivity(intent);
        CustomIntent.customType(getActivityNonNull(), "left-to-right");
    }

    private Query getCartQuery(String sort, long timestamp, double price, int orders, String key)
    {
        DatabaseReference cartRef = database.getReference("Carts");
        DatabaseReference childRef = cartRef.child(userId).getRef();
        DatabaseReference itemsRef = childRef.child("CartItems").getRef();
        if(sort.equals("Latest"))
        {
            if(nextTimeStamp == 0L)
            {
                return itemsRef.orderByChild("timestamp").limitToFirst(BATCH_SIZE);
            }
            return itemsRef.orderByChild("timestamp").startAfter(timestamp).limitToFirst(BATCH_SIZE);
        }
        else if(sort.equals("Oldest"))
        {
            if(nextTimeStamp == 0L)
            {
                return itemsRef.orderByChild("timestamp").limitToLast(BATCH_SIZE);
            }
            return itemsRef.orderByChild("timestamp").endBefore(timestamp).limitToLast(BATCH_SIZE);
        }
        else if(sort.equals("Price ascending"))
        {
            if(nextPrice == 0.0d)
            {
                return itemsRef.orderByChild("price").limitToFirst(BATCH_SIZE);
            }
            return itemsRef.orderByChild("price").startAfter(price, key).limitToFirst(BATCH_SIZE);
        }
        else if(sort.equals("Price descending"))
        {
            if(price == 0.0d)
            {
                return itemsRef.orderByChild("price").limitToLast(BATCH_SIZE);
            }
            return itemsRef.orderByChild("price").endBefore(price, key).limitToLast(BATCH_SIZE);
        }
        else if(sort.equals("Orders"))
        {
            if(orders == -1)
            {
                return itemsRef.orderByChild("orders").limitToLast(BATCH_SIZE);
            }
            return itemsRef.orderByChild("orders").endBefore(orders, key).limitToLast(BATCH_SIZE);
        }
        return null;
    }

    private void getCartProducts(String sort, long timestamp, double price, int orders, String key)
    {
        running = true;
        prevSize = cartItems.size();
        Query q = getCartQuery(sort, timestamp, price, orders, key);
        if(q == null)
        {
            Toast.makeText(getActivityNonNull(), "Not yet implemented.", Toast.LENGTH_SHORT).show();
        }
        else if(sort.equals("Latest"))
        {
            q.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        int count = (int) snapshot.getChildrenCount();
                        int i = 1;
                        List<String> keys = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren())
                        {
                            keys.add(snap.getKey());
                            CartData data = snap.getValue(CartData.class);
                            if(i == count)
                            {
                                prevTimeStamp = nextTimeStamp;
                            }
                            nextTimeStamp = data.getTimestamp();
                            i++;
                        }
                        getProducts(keys);
                    }
                    else
                    {
                        running = false;
                        loadBar.setVisibility(View.GONE);
                        loadMore = false;
                        if(cartItems.size() == 0)
                        {
                            orderAllBtn.setVisibility(View.GONE);
                            placeholderView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            Toast.makeText(getActivityNonNull(), "No more products left to display in cart.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    running = false;
                    loadBar.setVisibility(View.GONE);
                    if(cartItems.size() == 0)
                    {
                        orderAllBtn.setVisibility(View.GONE);
                        placeholderView.setVisibility(View.VISIBLE);
                    }
                    CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
        else if(sort.equals("Oldest"))
        {
            q.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        boolean flag = true;
                        boolean set = false;
                        int count = (int) snapshot.getChildrenCount();
                        List<String> keys = new ArrayList<>();
                        List<String> sorter = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren())
                        {
                            sorter.add(snap.getKey());
                            CartData data = snap.getValue(CartData.class);
                            if(flag)
                            {
                                if(count == 1)
                                {
                                    afterTimeStamp = data.getTimestamp();
                                }
                                nextTimeStamp = data.getTimestamp();
                                flag = false;
                                set = true;
                            }
                            if(set)
                            {
                                afterTimeStamp = data.getTimestamp();
                                set = false;
                            }
                        }
                        Collections.reverse(sorter);
                        for(String s : sorter)
                        {
                            keys.add(s);
                        }
                        getProducts(keys);
                    }
                    else
                    {
                        running = false;
                        loadBar.setVisibility(View.GONE);
                        loadMore = false;
                        if(cartItems.size() == 0)
                        {
                            orderAllBtn.setVisibility(View.GONE);
                            placeholderView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            Toast.makeText(getActivityNonNull(), "No more products left to display in cart.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    running = false;
                    loadBar.setVisibility(View.GONE);
                    if(cartItems.size() == 0)
                    {
                        orderAllBtn.setVisibility(View.GONE);
                        placeholderView.setVisibility(View.VISIBLE);
                    }
                    CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
        else if(sort.equals("Price ascending"))
        {
            q.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        List<String> keys = new ArrayList<>();
                        int count = (int) snapshot.getChildrenCount();
                        int i = 1;
                        for (DataSnapshot snap : snapshot.getChildren())
                        {
                            keys.add(snap.getKey());
                            CartData data = snap.getValue(CartData.class);
                            if(i == count)
                            {
                                prevPrice = nextPrice;
                                prevKey = nextKey;
                            }
                            nextPrice = data.getPrice();
                            nextKey = snap.getKey();
                            i++;
                        }
                        getProducts(keys);
                    }
                    else
                    {
                        running = false;
                        loadBar.setVisibility(View.GONE);
                        loadMore = false;
                        if(cartItems.size() == 0)
                        {
                            orderAllBtn.setVisibility(View.GONE);
                            placeholderView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            Toast.makeText(getActivityNonNull(), "No more products left to display in cart.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    running = false;
                    loadBar.setVisibility(View.GONE);
                    if(cartItems.size() == 0)
                    {
                        orderAllBtn.setVisibility(View.GONE);
                        placeholderView.setVisibility(View.VISIBLE);
                    }
                    CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
        else if(sortBy.equals("Price descending"))
        {
            q.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        boolean flag = true;
                        boolean set = false;
                        int count = (int) snapshot.getChildrenCount();
                        List<String> keys = new ArrayList<>();
                        List<String> sorter = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren())
                        {
                            CartData data = snap.getValue(CartData.class);
                            sorter.add(snap.getKey());
                            if(flag)
                            {
                                if(count == 1)
                                {
                                    afterPrice = data.getPrice();
                                    afterKey = snap.getKey();
                                }
                                nextPrice = data.getPrice();
                                nextKey = snap.getKey();
                                flag = false;
                                set = true;
                            }
                            if(set)
                            {
                                afterPrice = data.getPrice();
                                afterKey = snap.getKey();
                                set = false;
                            }
                        }
                        Collections.reverse(sorter);
                        for(String s : sorter)
                        {
                            keys.add(s);
                        }
                        getProducts(keys);
                    }
                    else
                    {
                        running = false;
                        loadBar.setVisibility(View.GONE);
                        loadMore = false;
                        if(cartItems.size() == 0)
                        {
                            orderAllBtn.setVisibility(View.GONE);
                            placeholderView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            Toast.makeText(getActivityNonNull(), "No more products left to display in cart.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    running = false;
                    loadBar.setVisibility(View.GONE);
                    if(cartItems.size() == 0)
                    {
                        orderAllBtn.setVisibility(View.GONE);
                        placeholderView.setVisibility(View.VISIBLE);
                    }
                    CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
        else if(sortBy.equals("Orders"))
        {
            q.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        boolean flag = true;
                        boolean set = false;
                        int count = (int) snapshot.getChildrenCount();
                        List<String> keys = new ArrayList<>();
                        List<String> sorter = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren())
                        {
                            CartData data = snap.getValue(CartData.class);
                            sorter.add(snap.getKey());
                            if(flag)
                            {
                                if(count == 1)
                                {
                                    afterOrders = data.getOrders();
                                    afterKey = snap.getKey();
                                }
                                nextOrders = data.getOrders();
                                nextKey = snap.getKey();
                                flag = false;
                                set = true;
                            }
                            if(set)
                            {
                                afterOrders = data.getOrders();
                                afterKey = snap.getKey();
                                set = false;
                            }
                        }
                        Collections.reverse(sorter);
                        for(String s : sorter)
                        {
                            keys.add(s);
                        }
                        getProducts(keys);
                    }
                    else
                    {
                        running = false;
                        loadBar.setVisibility(View.GONE);
                        loadMore = false;
                        if(cartItems.size() == 0)
                        {
                            orderAllBtn.setVisibility(View.GONE);
                            placeholderView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            Toast.makeText(getActivityNonNull(), "No more products left to display in cart.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    running = false;
                    loadBar.setVisibility(View.GONE);
                    if(cartItems.size() == 0)
                    {
                        orderAllBtn.setVisibility(View.GONE);
                        placeholderView.setVisibility(View.VISIBLE);
                    }
                    CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
    }

    private void getProducts(List<String> idList)
    {
        if(idList.size() == 0)
        {
            running = false;
            if(cartItems.size() > 0)
            {
                orderAllBtn.setEnabled(true);
            }
            loadBar.setVisibility(View.GONE);
            itemsAdapter.notifyItemRangeInserted(prevSize, BATCH_SIZE);
            removeAfterItemDuplicate();
            return;
        }
        else
        {
            DatabaseReference productRef = database.getReference("Products");
            DatabaseReference childRef = productRef.child(idList.remove(0)).getRef();
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
                        cartItems.add(item);
                        getProducts(idList);
                    }
                    else
                    {
                        running = false;
                        idList.clear();
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", "Error could not obtain the products.", false);
                        dialog.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    running = false;
                    loadBar.setVisibility(View.GONE);
                    CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
    }

    @Override
    public void OnRemoveButtonClick(int position, CartItem data)
    {
        int comp = cartItems.size() - position;
        if(comp <= BATCH_SIZE)
        {
            if(sortBy.equals("Latest"))
            {
                nextTimeStamp = prevTimeStamp;
            }
            else if(sortBy.equals("Oldest"))
            {
                nextTimeStamp = afterTimeStamp;
            }
            else if(sortBy.equals("Price ascending"))
            {
                nextPrice = prevPrice;
                nextKey = prevKey;
            }
            else if(sortBy.equals("Price descending"))
            {
                nextPrice = afterPrice;
                nextKey = afterKey;
            }
            else if(sortBy.equals("Orders"))
            {
                nextOrders = afterOrders;
                nextKey = afterKey;
            }
        }
        DatabaseReference mainRef = database.getReference("Carts");
        DatabaseReference userIdRef = mainRef.child(userId).getRef();
        DatabaseReference cartItemsRef = userIdRef.child("CartItems").getRef();
        DatabaseReference finalRef = cartItemsRef.child(data.getProductId()).getRef();
        progressDialog.show();
        finalRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                snapshot.getRef().removeValue(new DatabaseReference.CompletionListener()
                {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref)
                    {
                        progressDialog.dismiss();
                        cartItems.remove(position);
                        if(cartItems.size() == 0)
                        {
                            orderAllBtn.setEnabled(false);
                        }
                        itemsAdapter.notifyItemRemoved(position);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Server/Network error", error.getMessage(), false);
                dialog.show();
            }
        });
    }

    @Override
    public void OnOrderButtonClick(int position, CartItem data)
    {
        if(data.getInStock() == 0)
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Task error", "You cannot order this product, because it is not in stock.", false);
            dialog.show();
        }
        else if(data.getQuantity() == 0)
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Task error", "Please set your desired quantity to be bigger than zero.", false);
            dialog.show();
        }
        else
        {
            Intent intent = new Intent(getActivityNonNull(), PaymentActivity.class);
            intent.putExtra(BUTTON_KEY, "orderOne");
            intent.putExtra(DATA, data);
            startActivity(intent);
            CustomIntent.customType(getActivityNonNull(), "left-to-right");
        }
    }

    public interface CartFragmentTouchListener
    {
        void onCartFragmentTouch();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        String[] sortOptions = getResources().getStringArray(R.array.sorts);
        arrayAdapterSort = new ArrayAdapter<>(getContext(), R.layout.category_dropdown_item, sortOptions);
        sortSelector.setAdapter(arrayAdapterSort);

        sortSelector.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String item = arrayAdapterSort.getItem(position);
                sortSelector.clearFocus();

                if(!sortBy.equals(item))
                {
                    sortBy = item;
                    resetKeys();
                    cartItems.clear();
                    itemsAdapter.notifyDataSetChanged();
                    loadMore = true;
                    loadBar.setVisibility(View.VISIBLE);
                    getCartProducts(sortBy, nextTimeStamp, nextPrice, nextOrders, nextKey);
                }
                else
                {
                    Toast.makeText(getActivityNonNull(), "Already displaying products in this order.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof CartFragmentTouchListener)
        {
            listener = (CartFragmentTouchListener) context;
        }
        else
        {
            throw  new RuntimeException(context.toString() + " must implement the CartFragmentTouchListener.");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }

    protected FragmentActivity getActivityNonNull()
    {
        if (super.getActivity() != null)
        {
            return super.getActivity();
        }
        else {
            throw new RuntimeException("null returned from getActivity()");
        }
    }

    private void removeAfterItemDuplicate()
    {
        int i = 0;
        CartItem prev = new CartItem();
        for(CartItem c : cartItems)
        {
            if(i == 0)
            {
                prev.setAll(c);
            }
            else
            {
                if(c.equals(prev))
                {
                    cartItems.remove(i);
                    itemsAdapter.notifyItemRemoved(i);
                    break;
                }
                else
                {
                    prev.setAll(c);
                }
            }
            i++;
        }
    }

    private void resetKeys()
    {
        prevTimeStamp = 0L;
        prevPrice = 0.0d;
        prevKey = "";
        nextTimeStamp = 0L;
        nextPrice = 0.0d;
        nextKey = "";
        nextOrders = -1;
        afterTimeStamp = 0L;
        afterPrice = 0.0d;
        afterKey = "";
        afterOrders = -1;
    }

}
