package com.example.eceshop;

import android.annotation.SuppressLint;
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
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Map;

import maes.tech.intentanim.CustomIntent;

public class OrdersFragment extends Fragment implements OrderRecyclerViewAdapter.OnOrderClickListener
{

    private NestedScrollView ordersContainer;
    private FloatingActionButton backToTop;
    private AutoCompleteTextView sortSelector;
    private RecyclerView ordersRecyclerView;
    private ProgressBar loadBar;
    private TextView placeholderTextView;

    private OrdersFragmentTouchListener listener;
    private ArrayAdapter<String> arrayAdapterSort;
    private FirebaseDatabase database;
    private DatabaseReference ordersRef;

    private static final String CLICKED_ORDER_KEY = "com.example.eceshop.CLICKED_ORDER";
    private boolean loadMore;
    private boolean running;
    private String sortBy;
    private long nextTimestamp;
    private String nextKey;
    private int prevSize;
    private static final int BATCH_SIZE = 6;
    private String userId;
    private List<Order> ordersList;
    private boolean madeVisible;
    private OrderRecyclerViewAdapter ordersAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.orders_fragment, container, false);

        ordersContainer = root.findViewById(R.id.orders_container);
        backToTop = root.findViewById(R.id.backToTopOrders);
        sortSelector = root.findViewById(R.id.orders_Sort_Selector);
        ordersRecyclerView = root.findViewById(R.id.ordersRecyclerView);
        loadBar = root.findViewById(R.id.ordersLoadBar);
        placeholderTextView = root.findViewById(R.id.ordersPlaceholder);

        loadMore = true;
        running = false;
        sortBy = "Latest";
        ordersList = new ArrayList<>();
        resetKeys();

        database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        ordersRef = database.getReference("Orders");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivityNonNull()));
        ordersAdapter = new OrderRecyclerViewAdapter(getActivityNonNull(), ordersList);
        ordersAdapter.setOnOrderClickListener(this);
        ordersRecyclerView.setAdapter(ordersAdapter);

        backToTop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ordersContainer.smoothScrollTo(0, 0);
            }
        });

        ordersContainer.setOnTouchListener(new View.OnTouchListener()
        {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                listener.onOrdersFragmentTouch();
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

        ordersContainer.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener()
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
                            getOrders();
                        }
                    }
                }
            }
        });

        placeholderTextView.setVisibility(View.GONE);
        loadBar.setVisibility(View.VISIBLE);

        getOrders();

        return root;
    }

    private void getOrders()
    {
        running = true;
        Query query = getOrderQuery();
        if(query != null)
        {
            prevSize = ordersList.size();
            if(sortBy.equals("Oldest"))
            {
                ValueEventListener valueEventListener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                String id = snap.getKey();
                                OrderDb value = snap.getValue(OrderDb.class);
                                if(value != null)
                                {
                                    Order item = createOrder(id, value);
                                    ordersList.add(item);
                                    nextTimestamp = item.getTimestamp();
                                }
                            }
                            running = false;
                            loadBar.setVisibility(View.GONE);
                            if(madeVisible)
                            {
                                placeholderTextView.setVisibility(View.GONE);
                                madeVisible = false;
                            }
                            ordersAdapter.notifyItemRangeInserted(prevSize, BATCH_SIZE);
                        }
                        else
                        {
                            running = false;
                            loadBar.setVisibility(View.GONE);
                            loadMore = false;
                            if(ordersList.size() == 0)
                            {
                                madeVisible = true;
                                placeholderTextView.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                Toast.makeText(getActivityNonNull(), "No more orders left to display.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        loadBar.setVisibility(View.GONE);
                        if(ordersList.size() == 0)
                        {
                            madeVisible = true;
                            placeholderTextView.setVisibility(View.VISIBLE);
                        }
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                query.addListenerForSingleValueEvent(valueEventListener);
            }
            else if(sortBy.equals("Latest"))
            {
                ValueEventListener valueEventListener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean flag = true;
                            List<Order> sorter = new ArrayList<>();
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                String id = snap.getKey();
                                OrderDb value = snap.getValue(OrderDb.class);
                                if(value != null)
                                {
                                    Order item = createOrder(id, value);
                                    sorter.add(item);
                                    if(flag)
                                    {
                                        nextTimestamp = item.getTimestamp();
                                        flag = false;
                                    }
                                }
                            }
                            Collections.reverse(sorter);
                            for(Order o : sorter)
                            {
                                ordersList.add(o);
                            }
                            running = false;
                            loadBar.setVisibility(View.GONE);
                            if(madeVisible)
                            {
                                placeholderTextView.setVisibility(View.GONE);
                                madeVisible = false;
                            }
                            ordersAdapter.notifyItemRangeInserted(prevSize, BATCH_SIZE);
                        }
                        else
                        {
                            running = false;
                            loadBar.setVisibility(View.GONE);
                            loadMore = false;
                            if(ordersList.size() == 0)
                            {
                                madeVisible = true;
                                placeholderTextView.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                Toast.makeText(getActivityNonNull(), "No more orders left to display.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        loadBar.setVisibility(View.GONE);
                        if(ordersList.size() == 0)
                        {
                            madeVisible = true;
                            placeholderTextView.setVisibility(View.VISIBLE);
                        }
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                query.addListenerForSingleValueEvent(valueEventListener);
            }
            else
            {
                ValueEventListener valueEventListener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                String id = snap.getKey();
                                OrderDb value = snap.getValue(OrderDb.class);
                                if(value != null)
                                {
                                    Order item = createOrder(id, value);
                                    ordersList.add(item);
                                    nextKey = item.getOrderId();
                                }
                            }
                            running = false;
                            loadBar.setVisibility(View.GONE);
                            if(madeVisible)
                            {
                                placeholderTextView.setVisibility(View.GONE);
                                madeVisible = false;
                            }
                            ordersAdapter.notifyItemRangeInserted(prevSize, BATCH_SIZE);
                        }
                        else
                        {
                            running = false;
                            loadBar.setVisibility(View.GONE);
                            loadMore = false;
                            if(ordersList.size() == 0)
                            {
                                madeVisible = true;
                                placeholderTextView.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                Toast.makeText(getActivityNonNull(), "No more orders left to display.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        loadBar.setVisibility(View.GONE);
                        if(ordersList.size() == 0)
                        {
                            madeVisible = true;
                            placeholderTextView.setVisibility(View.VISIBLE);
                        }
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                query.addListenerForSingleValueEvent(valueEventListener);
            }
        }
        else
        {
            Toast.makeText(getActivityNonNull(), "Cannot sort the orders in this way.", Toast.LENGTH_SHORT).show();
        }
    }

    private Query getOrderQuery()
    {
        DatabaseReference secRef =  ordersRef.child(userId).getRef();
        if(sortBy.equals("Oldest"))
        {
            if(nextTimestamp == 0L)
            {
                return secRef.orderByChild("timestamp").limitToFirst(BATCH_SIZE);
            }
            secRef.orderByChild("timestamp").startAfter(nextTimestamp).limitToFirst(BATCH_SIZE);
        }
        else if(sortBy.equals("Latest"))
        {
            if(nextTimestamp == 0L)
            {
                return secRef.orderByChild("timestamp").limitToLast(BATCH_SIZE);
            }
            secRef.orderByChild("timestamp").endBefore(nextTimestamp).limitToLast(BATCH_SIZE);
        }
        else if(sortBy.equals("Status: Completed"))
        {
            if(nextKey == null)
            {
                return secRef.orderByChild("status").equalTo("Completed").limitToFirst(BATCH_SIZE);
            }
            secRef.orderByChild("status").equalTo("Completed").startAfter("Completed", nextKey).limitToFirst(3);
        }
        else if(sortBy.equals("Status: Ongoing"))
        {
            if(nextKey == null)
            {
                return secRef.orderByChild("status").equalTo("Ongoing").limitToFirst(BATCH_SIZE);
            }
            secRef.orderByChild("status").equalTo("Ongoing").startAfter("Ongoing", nextKey).limitToFirst(3);
        }
        else if(sortBy.equals("Status: Cancelled"))
        {
            if(nextKey == null)
            {
                return secRef.orderByChild("status").equalTo("Cancelled").limitToFirst(BATCH_SIZE);
            }
            secRef.orderByChild("status").equalTo("Cancelled").startAfter("Cancelled", nextKey).limitToFirst(3);
        }
        return null;
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
    public void onResume()
    {
        super.onResume();

        String[] sortOptions = getResources().getStringArray(R.array.sortOrders);
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
                    ordersList.clear();
                    ordersAdapter.notifyDataSetChanged();
                    loadMore = true;
                    loadBar.setVisibility(View.VISIBLE);
                    getOrders();
                }
                else
                {
                    Toast.makeText(getActivityNonNull(), "Already displaying your Orders in this sorting.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        if(context instanceof OrdersFragmentTouchListener)
        {
            listener = (OrdersFragmentTouchListener) context;
        }
        else
        {
            throw  new RuntimeException(context.toString() + " must implement the OrdersFragmentTouchListener.");
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

    @Override
    public void OnOrderClick(int position, Order data)
    {
        Intent intent = new Intent(getActivityNonNull(), OrderDetailsActivity.class);
        intent.putExtra(CLICKED_ORDER_KEY, data);
        startActivity(intent);
        CustomIntent.customType(getActivityNonNull(), "left-to-right");
    }

    public interface OrdersFragmentTouchListener
    {
        void onOrdersFragmentTouch();
    }

    private void resetKeys()
    {
        nextKey = null;
        nextTimestamp = 0L;
    }

}
