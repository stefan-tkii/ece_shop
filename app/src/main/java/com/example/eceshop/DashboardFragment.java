package com.example.eceshop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class DashboardFragment extends Fragment implements CategoryRecyclerViewAdapter.OnItemClickListener, ProductRecyclerViewAdapter.OnProductClickListener
{

    private DashboardFragmentTouchListener listener;

    private FloatingActionButton backToTop;

    private NestedScrollView dashboardContainer;
    private ArrayAdapter<String> arrayAdapterSort;
    private AutoCompleteTextView productSorter;

    private RecyclerView categoryRecyclerView;
    private CategoryRecyclerViewAdapter categoryAdapter;

    private RecyclerView productRecyclerView;
    private ArrayList<Product> productItems;
    private ProductRecyclerViewAdapter productAdapter;

    private AlertDialog progressDialog;
    private ProgressBar itemBar;

    private String nextKey;
    private int nextOrders;
    private Double nextPrice;
    private boolean loadMore;
    private String sortBy;
    private String searchQuery;
    private String categorySort;

    private String[] categoryTitles;
    private Drawable[] categoryIcons;
    private static final int BATCH_SIZE = 3;

    private static final String CLICKED_KEY = "com.example.eceshop.CLICKED_PRODUCT";
    private static final String ADMIN_KEY = "com.example.eceshop.Admin";

    private boolean running;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.dashboard_fragment, container, false);

        loadMore = true;
        nextKey = null;
        nextPrice = 0.0d;
        nextOrders = -1;
        sortBy = "Latest";
        categorySort = "";

        running = false;

        Log.e("HU", "Inside onCreateView of DashboardFragment");

        dashboardContainer = root.findViewById(R.id.dashboard_container);
        productSorter = root.findViewById(R.id.sort_selector);

        backToTop = root.findViewById(R.id.backToTopFab);
        itemBar = root.findViewById(R.id.item_load_bar);

        categoryTitles = loadCategoryTitles();
        categoryIcons = loadCategoryIcons();

        categoryRecyclerView = root.findViewById(R.id.top_recycler);

        categoryAdapter = new CategoryRecyclerViewAdapter(Arrays.asList(
                createCategoryFor(0),
                createCategoryFor(1),
                createCategoryFor(2),
                createCategoryFor(3),
                createCategoryFor(4),
                createCategoryFor(5),
                createCategoryFor(6),
                createCategoryFor(7)
        ));

        categoryAdapter.setOnItemClickListener(this);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivityNonNull(), LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.setAdapter(categoryAdapter);

        progressDialog = new SpotsDialog.Builder()
                .setContext(getActivityNonNull())
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        productRecyclerView = root.findViewById(R.id.bottom_recycler);
        productItems = new ArrayList<>();

        productRecyclerView.setDrawingCacheEnabled(true);
        productRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        productRecyclerView.setLayoutManager(new LinearLayoutManager(getActivityNonNull()));
        productAdapter = new ProductRecyclerViewAdapter(getActivityNonNull(), productItems);
        productAdapter.setOnProductClickListener(this);
        productRecyclerView.setAdapter(productAdapter);

        dashboardContainer.setOnTouchListener(new View.OnTouchListener()
        {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                listener.onDashboardFragmentTouch();
                InputMethodManager imm = (InputMethodManager) getActivityNonNull().getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = getActivityNonNull().getCurrentFocus();
                if (focusedView != null)
                {
                    imm.hideSoftInputFromWindow(getActivityNonNull().getCurrentFocus().getWindowToken(), 0);
                    if(productSorter.isFocused())
                    {
                        productSorter.clearFocus();
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        backToTop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dashboardContainer.smoothScrollTo(0,0);
            }
        });

        dashboardContainer.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener()
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
                    if(loadMore)
                    {
                        if(!running)
                        {
                            backToTop.setVisibility(View.VISIBLE);
                            itemBar.setVisibility(View.VISIBLE);
                            getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
                        }
                    }
                    else
                    {
                        backToTop.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        Bundle bundle = this.getArguments();
        if(bundle != null)
        {
            searchQuery = bundle.getString("searchInput");
            itemBar.setVisibility(View.VISIBLE);
            if(searchQuery.equals(""))
            {
                Toast.makeText(getActivityNonNull(), "Back to displaying all products.", Toast.LENGTH_SHORT).show();
                getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
            }
            else
            {
                Toast.makeText(getActivityNonNull(), "Displaying results for: " + searchQuery, Toast.LENGTH_SHORT).show();
                getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
            }
        }
        else
        {
            searchQuery = "";
            getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
        }

        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        String[] sortOptions = getResources().getStringArray(R.array.sorts);
        arrayAdapterSort = new ArrayAdapter<>(getContext(), R.layout.category_dropdown_item, sortOptions);
        productSorter.setAdapter(arrayAdapterSort);

        productSorter.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String item = arrayAdapterSort.getItem(position);
                productSorter.clearFocus();
                if(!sortBy.equals(item))
                {
                    sortBy = item;
                    productItems.clear();
                    productAdapter.notifyDataSetChanged();
                    loadMore = true;
                    itemBar.setVisibility(View.VISIBLE);
                    nextPrice = 0.0d;
                    nextOrders = -1;
                    nextKey = null;
                    getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
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
        if(context instanceof DashboardFragmentTouchListener)
        {
            listener = (DashboardFragmentTouchListener) context;
        }
        else
        {
            throw  new RuntimeException(context.toString() + " must implement the DashboardFragmentTouchListener.");
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

    private String[] loadCategoryTitles()
    {
        return getResources().getStringArray(R.array.category_titles);
    }

    private Drawable[] loadCategoryIcons()
    {
        TypedArray ta = getResources().obtainTypedArray(R.array.category_drawables);
        Drawable[] icons = new Drawable[ta.length()];
        for(int i=0;i<ta.length();i++)
        {
            int id = ta.getResourceId(i, 0);
            if(id != 0)
            {
                icons[i] = ContextCompat.getDrawable(getActivityNonNull(), id);
            }
        }
        ta.recycle();
        return icons;
    }

    private CategoryRecyclerViewModel createCategoryFor(int position)
    {
        return new CategoryRecyclerViewModel(categoryIcons[position], categoryTitles[position]);
    }

    @Override
    public void OnItemClick(int position, String data)
    {
        if(position == RecyclerView.NO_POSITION)
        {
            categorySort = "";
            nextKey = null;
            nextPrice = 0.0d;
            nextOrders = -1;
            productItems.clear();
            loadMore = true;
            itemBar.setVisibility(View.VISIBLE);
            productAdapter.notifyDataSetChanged();
            getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
        }
        else
        {
            categorySort = data;
            nextKey = null;
            nextPrice = 0.0d;
            loadMore = true;
            nextOrders = -1;
            productItems.clear();
            itemBar.setVisibility(View.VISIBLE);
            productAdapter.notifyDataSetChanged();
            getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
        }
    }

    @Override
    public void OnProductClick(int position, Product data)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String id = user.getUid();
        checkAdmin(id, data);
    }

    public interface DashboardFragmentTouchListener
    {
        void onDashboardFragmentTouch();
    }

    private void getProducts(String optionSort, String newId, Double newPrice, String searchBy, String category, int newOrders)
    {
        running = true;
        Query mDatabase = getProductsQuery(optionSort, newId, newPrice, newOrders);
        if(mDatabase == null)
        {
            itemBar.setVisibility(View.GONE);
            Toast.makeText(getActivityNonNull(), "No valid sort option is selected.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(optionSort.equals("Oldest"))
            {
                ValueEventListener valueListener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean added = false;
                            for(DataSnapshot dataSnapshot : snapshot.getChildren())
                            {
                                ProductDb dbItem = dataSnapshot.getValue(ProductDb.class);
                                Product item = new Product(dataSnapshot.getKey(), dbItem.getName(),
                                        dbItem.getShortDesc(), dbItem.getLongDesc(),
                                        dbItem.getImgUri(), dbItem.getPrice(),
                                        dbItem.getOrders(), dbItem.getCategoryId(), dbItem.getInStock());

                                if(searchBy.equals("") && category.equals(""))
                                {
                                    productItems.add(item);
                                }
                                else if((!searchBy.equals("")) && category.equals(""))
                                {
                                    String name = dataSnapshot.child("name").getValue(String.class);
                                    if(Pattern.compile(Pattern.quote(searchBy), Pattern.CASE_INSENSITIVE).matcher(name).find())
                                    {
                                        productItems.add(item);
                                        added = true;
                                    }
                                }
                                else if(searchBy.equals("") && (!category.equals("")))
                                {
                                    String categoryId = dataSnapshot.child("categoryId").getValue(String.class);
                                    if(categoryId.equals(category))
                                    {
                                        productItems.add(item);
                                        added = true;
                                    }
                                }
                                else if((!searchBy.equals("")) && (!category.equals("")))
                                {
                                    String name = dataSnapshot.child("name").getValue(String.class);
                                    String categoryId = dataSnapshot.child("categoryId").getValue(String.class);
                                    if(Pattern.compile(Pattern.quote(searchBy), Pattern.CASE_INSENSITIVE).matcher(name).find() && categoryId.equals(category))
                                    {
                                        productItems.add(item);
                                        added = true;
                                    }
                                }
                                nextKey = dataSnapshot.getKey();
                            }
                            if(added)
                            {
                                running = false;
                                itemBar.setVisibility(View.GONE);
                                productAdapter.notifyDataSetChanged();
                            }
                            else
                            {
                                if(searchBy.equals("") && category.equals(""))
                                {
                                    running = false;
                                    itemBar.setVisibility(View.GONE);
                                    productAdapter.notifyDataSetChanged();
                                }
                                else
                                {
                                    getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
                                }
                            }
                        }
                        else
                        {
                            running = false;
                            itemBar.setVisibility(View.GONE);
                            loadMore = false;
                            productAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivityNonNull(), "No more products left to display.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        itemBar.setVisibility(View.GONE);
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                mDatabase.addListenerForSingleValueEvent(valueListener);
            }
            else if(optionSort.equals("Latest"))
            {
                ValueEventListener valueListener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean added = false;
                            ArrayList<Product> sorter = new ArrayList<>();
                            boolean flag = true;
                            for(DataSnapshot dataSnapshot : snapshot.getChildren())
                            {
                                ProductDb dbItem = dataSnapshot.getValue(ProductDb.class);
                                Product item = new Product(dataSnapshot.getKey(), dbItem.getName(),
                                        dbItem.getShortDesc(), dbItem.getLongDesc(),
                                        dbItem.getImgUri(), dbItem.getPrice(),
                                        dbItem.getOrders(), dbItem.getCategoryId(), dbItem.getInStock());
                                sorter.add(item);
                                if(flag)
                                {
                                    nextKey = dataSnapshot.getKey();
                                    flag = false;
                                }
                            }
                            Collections.reverse(sorter);
                            for(Product m : sorter)
                            {
                                if(searchBy.equals("") && category.equals(""))
                                {
                                    productItems.add(m);
                                }
                                else if((!searchBy.equals("")) && category.equals(""))
                                {
                                    String name = m.getName();
                                    if(Pattern.compile(Pattern.quote(searchBy), Pattern.CASE_INSENSITIVE).matcher(name).find())
                                    {
                                        productItems.add(m);
                                        added = true;
                                    }
                                }
                                else if(searchBy.equals("") && (!category.equals("")))
                                {
                                    String categoryId = m.getCategoryId();
                                    if(categoryId.equals(category))
                                    {
                                        productItems.add(m);
                                        added = true;
                                    }
                                }
                                else if((!searchBy.equals("")) && (!category.equals("")))
                                {
                                    String name = m.getName();
                                    String categoryId = m.getCategoryId();
                                    if(Pattern.compile(Pattern.quote(searchBy), Pattern.CASE_INSENSITIVE).matcher(name).find() && categoryId.equals(category))
                                    {
                                        productItems.add(m);
                                        added = true;
                                    }
                                }
                            }
                            if(added)
                            {
                                running = false;
                                itemBar.setVisibility(View.GONE);
                                productAdapter.notifyDataSetChanged();
                            }
                            else
                            {
                                if(searchBy.equals("") && category.equals(""))
                                {
                                    running = false;
                                    itemBar.setVisibility(View.GONE);
                                    productAdapter.notifyDataSetChanged();
                                }
                                else
                                {
                                    getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
                                }
                            }
                        }
                        else
                        {
                            running = false;
                            itemBar.setVisibility(View.GONE);
                            loadMore = false;
                            productAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivityNonNull(), "No more products left to display.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        itemBar.setVisibility(View.GONE);
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                mDatabase.addListenerForSingleValueEvent(valueListener);
            }
            else if(optionSort.equals("Price ascending"))
            {
                ValueEventListener valueListener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean added = false;
                            for(DataSnapshot dataSnapshot : snapshot.getChildren())
                            {
                                ProductDb dbItem = dataSnapshot.getValue(ProductDb.class);
                                Product item = new Product(dataSnapshot.getKey(), dbItem.getName(),
                                        dbItem.getShortDesc(), dbItem.getLongDesc(),
                                        dbItem.getImgUri(), dbItem.getPrice(),
                                        dbItem.getOrders(), dbItem.getCategoryId(), dbItem.getInStock());

                                if(searchBy.equals("") && category.equals(""))
                                {
                                    productItems.add(item);
                                }
                                else if((!searchBy.equals("")) && category.equals(""))
                                {
                                    String name = dataSnapshot.child("name").getValue(String.class);
                                    if(Pattern.compile(Pattern.quote(searchBy), Pattern.CASE_INSENSITIVE).matcher(name).find())
                                    {
                                        productItems.add(item);
                                        added = true;
                                    }
                                }
                                else if(searchBy.equals("") && (!category.equals("")))
                                {
                                    String categoryId = dataSnapshot.child("categoryId").getValue(String.class);
                                    if(categoryId.equals(category))
                                    {
                                        productItems.add(item);
                                        added = true;
                                    }
                                }
                                else if((!searchBy.equals("")) && (!category.equals("")))
                                {
                                    String name = dataSnapshot.child("name").getValue(String.class);
                                    String categoryId = dataSnapshot.child("categoryId").getValue(String.class);
                                    if(Pattern.compile(Pattern.quote(searchBy), Pattern.CASE_INSENSITIVE).matcher(name).find() && categoryId.equals(category))
                                    {
                                        productItems.add(item);
                                        added = true;
                                    }
                                }
                                nextPrice = dataSnapshot.child("price").getValue(Double.class);
                                nextKey = dataSnapshot.getKey();
                            }
                            if(added)
                            {
                                running = false;
                                itemBar.setVisibility(View.GONE);
                                productAdapter.notifyDataSetChanged();
                            }
                            else
                            {
                                if(searchBy.equals("") && category.equals(""))
                                {
                                    running = false;
                                    itemBar.setVisibility(View.GONE);
                                    productAdapter.notifyDataSetChanged();
                                }
                                else
                                {
                                    getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
                                }
                            }
                        }
                        else
                        {
                            running = false;
                            itemBar.setVisibility(View.GONE);
                            loadMore = false;
                            productAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivityNonNull(), "No more products left to display.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        itemBar.setVisibility(View.GONE);
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                mDatabase.addListenerForSingleValueEvent(valueListener);
            }
            else if(optionSort.equals("Price descending"))
            {
                ValueEventListener valueListener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean added = false;
                            ArrayList<Product> sorter = new ArrayList<>();
                            boolean flag = true;
                            for(DataSnapshot dataSnapshot : snapshot.getChildren())
                            {
                                ProductDb dbItem = dataSnapshot.getValue(ProductDb.class);
                                Product item = new Product(dataSnapshot.getKey(), dbItem.getName(),
                                        dbItem.getShortDesc(), dbItem.getLongDesc(),
                                        dbItem.getImgUri(), dbItem.getPrice(),
                                        dbItem.getOrders(), dbItem.getCategoryId(), dbItem.getInStock());

                                sorter.add(item);
                                if(flag)
                                {
                                    nextPrice = dataSnapshot.child("price").getValue(Double.class);
                                    nextKey = dataSnapshot.getKey();
                                    flag = false;
                                }
                            }
                            Collections.reverse(sorter);
                            for(Product m : sorter)
                            {
                                if(searchBy.equals("") && category.equals(""))
                                {
                                    productItems.add(m);
                                }
                                else if((!searchBy.equals("")) && category.equals(""))
                                {
                                    String name = m.getName();
                                    if(Pattern.compile(Pattern.quote(searchBy), Pattern.CASE_INSENSITIVE).matcher(name).find())
                                    {
                                        productItems.add(m);
                                        added = true;
                                    }
                                }
                                else if(searchBy.equals("") && (!category.equals("")))
                                {
                                    String categoryId = m.getCategoryId();
                                    if(categoryId.equals(category))
                                    {
                                        productItems.add(m);
                                        added = true;
                                    }
                                }
                                else if((!searchBy.equals("")) && (!category.equals("")))
                                {
                                    String name = m.getName();
                                    String categoryId = m.getCategoryId();
                                    if(Pattern.compile(Pattern.quote(searchBy), Pattern.CASE_INSENSITIVE).matcher(name).find() && categoryId.equals(category))
                                    {
                                        productItems.add(m);
                                        added = true;
                                    }
                                }
                            }
                            if(added)
                            {
                                running = false;
                                itemBar.setVisibility(View.GONE);
                                productAdapter.notifyDataSetChanged();
                            }
                            else
                            {
                                if(searchBy.equals("") && category.equals(""))
                                {
                                    running = false;
                                    itemBar.setVisibility(View.GONE);
                                    productAdapter.notifyDataSetChanged();
                                }
                                else
                                {
                                    getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
                                }
                            }
                        }
                        else
                        {
                            running = false;
                            itemBar.setVisibility(View.GONE);
                            loadMore = false;
                            productAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivityNonNull(), "No more products left to display.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        itemBar.setVisibility(View.GONE);
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                mDatabase.addListenerForSingleValueEvent(valueListener);
            }
            else if(optionSort.equals("Orders"))
            {
                ValueEventListener valueListener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean added = false;
                            ArrayList<Product> sorter = new ArrayList<>();
                            boolean flag = true;
                            for(DataSnapshot dataSnapshot : snapshot.getChildren())
                            {
                                ProductDb dbItem = dataSnapshot.getValue(ProductDb.class);
                                Product item = new Product(dataSnapshot.getKey(), dbItem.getName(),
                                        dbItem.getShortDesc(), dbItem.getLongDesc(),
                                        dbItem.getImgUri(), dbItem.getPrice(),
                                        dbItem.getOrders(), dbItem.getCategoryId(), dbItem.getInStock());

                                sorter.add(item);
                                if(flag)
                                {
                                    nextOrders = dataSnapshot.child("orders").getValue(int.class);
                                    nextKey = dataSnapshot.getKey();
                                    flag = false;
                                }
                            }
                            Collections.reverse(sorter);
                            for(Product m : sorter)
                            {
                                if(searchBy.equals("") && category.equals(""))
                                {
                                    productItems.add(m);
                                }
                                else if((!searchBy.equals("")) && category.equals(""))
                                {
                                    String name = m.getName();
                                    if(Pattern.compile(Pattern.quote(searchBy), Pattern.CASE_INSENSITIVE).matcher(name).find())
                                    {
                                        productItems.add(m);
                                        added = true;
                                    }
                                }
                                else if(searchBy.equals("") && (!category.equals("")))
                                {
                                    String categoryId = m.getCategoryId();
                                    if(categoryId.equals(category))
                                    {
                                        productItems.add(m);
                                        added = true;
                                    }
                                }
                                else if((!searchBy.equals("")) && (!category.equals("")))
                                {
                                    String name = m.getName();
                                    String categoryId = m.getCategoryId();
                                    if(Pattern.compile(Pattern.quote(searchBy), Pattern.CASE_INSENSITIVE).matcher(name).find() && categoryId.equals(category))
                                    {
                                        productItems.add(m);
                                        added = true;
                                    }
                                }
                            }
                            if(added)
                            {
                                running = false;
                                itemBar.setVisibility(View.GONE);
                                productAdapter.notifyDataSetChanged();
                            }
                            else
                            {
                                if(searchBy.equals("") && category.equals(""))
                                {
                                    running = false;
                                    itemBar.setVisibility(View.GONE);
                                    productAdapter.notifyDataSetChanged();
                                }
                                else
                                {
                                    getProducts(sortBy, nextKey, nextPrice, searchQuery, categorySort, nextOrders);
                                }
                            }
                        }
                        else
                        {
                            running = false;
                            itemBar.setVisibility(View.GONE);
                            loadMore = false;
                            productAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivityNonNull(), "No more products left to display.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        itemBar.setVisibility(View.GONE);
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                mDatabase.addListenerForSingleValueEvent(valueListener);
            }
        }
    }

    private Query getProductsQuery(String optionSort, String newId, Double newPrice, int newOrders)
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference ref = db.getReference("Products");
        if(optionSort.equals("Oldest"))
        {
            if(newId == null)
            {
                return db.getReference("Products").orderByKey().limitToFirst(BATCH_SIZE);
            }
            return db.getReference("Products").orderByKey().startAfter(newId).limitToFirst(BATCH_SIZE);
        }
        else if(optionSort.equals("Latest"))
        {
            if(newId == null)
            {
                return db.getReference("Products").orderByKey().limitToLast(BATCH_SIZE);
            }
            return db.getReference("Products").orderByKey().endBefore(newId).limitToLast(BATCH_SIZE);
        }
        else if(optionSort.equals("Price ascending"))
        {
            if(newPrice == 0.0d)
            {
                return db.getReference("Products").orderByChild("price").limitToFirst(BATCH_SIZE);
            }
            return db.getReference("Products").orderByChild("price").startAfter(newPrice, newId).limitToFirst(BATCH_SIZE);
        }
        else if(optionSort.equals("Price descending"))
        {
            if(newPrice == 0.0d)
            {
                return db.getReference("Products").orderByChild("price").limitToLast(BATCH_SIZE);
            }
            return db.getReference("Products").orderByChild("price").endBefore(newPrice, newId).limitToLast(BATCH_SIZE);
        }
        else if(optionSort.equals("Orders"))
        {
            if(newOrders == -1)
            {
                return db.getReference("Products").orderByChild("orders").limitToLast(BATCH_SIZE);
            }
            return db.getReference("Products").orderByChild("orders").endBefore(newOrders, newId).limitToLast(BATCH_SIZE);
        }
        else
        {
            return null;
        }
    }

    private void checkAdmin(String id, Product data)
    {
        progressDialog.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference userDatabase = database.getReference("Users");
        DatabaseReference userRef = userDatabase.child(id).getRef();
        userRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                User u = snapshot.getValue(User.class);
                if(u != null)
                {
                    boolean admin = u.isAdmin();
                    progressDialog.dismiss();
                    if(admin)
                    {
                        Intent intent = new Intent(getActivityNonNull(), ProductDetailsActivity.class);
                        intent.putExtra(CLICKED_KEY, data);
                        intent.putExtra(ADMIN_KEY, true);
                        startActivity(intent);
                        CustomIntent.customType(getActivityNonNull(), "left-to-right");
                    }
                    else
                    {
                        Intent intent = new Intent(getActivityNonNull(), ProductDetailsActivity.class);
                        intent.putExtra(CLICKED_KEY, data);
                        startActivity(intent);
                        CustomIntent.customType(getActivityNonNull(), "left-to-right");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Task error", "Failed to fetch the connection to the database."
                        + error.getMessage(),
                        false);
                dialog.show();
            }
        });
    }

}
