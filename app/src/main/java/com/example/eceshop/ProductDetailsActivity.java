package com.example.eceshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class ProductDetailsActivity extends AppCompatActivity
{

    private static final String CLICKED_KEY = "com.example.eceshop.CLICKED_PRODUCT";
    private static final String ORIGIN_KEY = "com.example.eceshop.ORIGIN_KEY";
    private static final String ADMIN_KEY = "com.example.eceshop.Admin";

    private Product model;
    private Toolbar toolbar;
    private AlertDialog progressDialog;
    private ArrayAdapter<String> arrayAdapterSort;

    private NestedScrollView container;
    private FloatingActionButton backToTop;
    private ImageView imageView;
    private TextView nameText;
    private TextView priceText;
    private TextView stockText;
    private TextView longDescText;
    private ImageView categoryImage;
    private TextView categoryTitle;
    private TextView ordersText;
    private AppCompatButton addToCartBtn;
    private AutoCompleteTextView commentsSorter;
    private TextInputEditText commentInput;
    private AppCompatButton postCommentBtn;
    private RecyclerView commentsRecyclerView;
    private TextView placeholderTextView;
    private ProgressBar loadBar;

    private CommentsRecyclerViewAdapter commentsAdapter;

    private Drawable[] categoryIcons;
    private String[] categoryTitles;

    private String sortBy;
    private String nextKey;
    private ArrayList<CommentRvItem> comments;
    private ArrayList<Comment> dbComments;
    private boolean loadMore;
    private int counter;
    private String prevKey;
    private String afterKey;
    private String userKey;
    private static final int BATCH_SIZE = 3;
    private boolean running;
    private boolean origin;
    private boolean admin;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        toolbar = findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prevKey = "";
        afterKey = "";
        nextKey = null;
        sortBy = "Latest";
        loadMore = true;
        running = false;
        dbComments = new ArrayList<>();

        progressDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        changeStatusBarColor();

        admin = getIntent().getBooleanExtra(ADMIN_KEY, false);

        container = findViewById(R.id.details_container);
        backToTop = findViewById(R.id.backToTopDetails);
        imageView = findViewById(R.id.details_image);
        nameText = findViewById(R.id.details_name);
        priceText = findViewById(R.id.details_price);
        stockText = findViewById(R.id.details_inStock);
        longDescText = findViewById(R.id.details_longDesc);
        categoryImage = findViewById(R.id.details_category_image);
        categoryTitle = findViewById(R.id.details_category_title);
        ordersText = findViewById(R.id.details_orders);
        addToCartBtn = findViewById(R.id.addCartButton);
        commentsSorter = findViewById(R.id.sort_comments);
        commentInput = findViewById(R.id.commentInput);
        postCommentBtn = findViewById(R.id.postCommentBtn);
        commentsRecyclerView = findViewById(R.id.comments_recyclerView);
        placeholderTextView = findViewById(R.id.placeholderTextView);
        loadBar = findViewById(R.id.details_item_load_bar);

        comments = new ArrayList<>();
        commentsAdapter = new CommentsRecyclerViewAdapter(this, comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        if(admin)
        {
            String edit = "Edit product";
            addToCartBtn.setText(edit);
            int img = R.drawable.ic_edit;
            addToCartBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(img, 0, 0, 0);
        }

        SwipeHelper swipeHelper = new SwipeHelper(this, commentsRecyclerView)
        {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons)
            {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Delete",
                        0,
                        Color.parseColor("#042382"),
                        new SwipeHelper.UnderlayButtonClickListener()
                        {
                            @Override
                            public void onClick(int pos)
                            {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                String email = user.getEmail();
                                CommentRvItem item = comments.get(pos);
                                String commenter = item.getUserEmail();
                                if(email.equals(commenter))
                                {
                                    int comp = comments.size() - pos;
                                    if(comp <= BATCH_SIZE)
                                    {
                                        if(sortBy.equals("Latest"))
                                        {
                                            nextKey = afterKey;
                                        }
                                        else if(sortBy.equals("Oldest"))
                                        {
                                            nextKey = prevKey;
                                        }
                                    }
                                    FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
                                    DatabaseReference ref = db.getReference("ProductComments");
                                    DatabaseReference childRef = ref.child(model.getProductId()).getRef();
                                    DatabaseReference finalRef = childRef.child(item.getId()).getRef();
                                    //progressDialog.show();
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
                                                    //progressDialog.dismiss();
                                                    comments.remove(pos);
                                                    commentsAdapter.notifyItemRemoved(pos);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error)
                                        {
                                           // progressDialog.dismiss();
                                            CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Server/Network error", "Could not delete this comment.", false);
                                            dialog.show();
                                        }
                                    });
                                }
                                else
                                {
                                    CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Delete error", "Cannot delete other user's comments.", false);
                                    dialog.show();
                                }
                            }
                        }
                ));
            }
        };

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
                    if(commentsSorter.isFocused())
                    {
                        commentsSorter.clearFocus();
                    }
                    if(commentInput.isFocused())
                    {
                        commentInput.clearFocus();
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
                container.smoothScrollTo(0,0);
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
                    backToTop.setVisibility(View.VISIBLE);
                    if(loadMore)
                    {
                        if(!running)
                        {
                            getComments(sortBy, nextKey);
                        }
                    }
                }
            }
        });

        addToCartBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(admin)
                {
                    gotoEditProduct();
                }
                else
                {
                    userAddToCart();
                }
            }
        });

        postCommentBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e("TAGGER", "Post comment button is clicked.");
                String input = commentInput.getText().toString();
                if(TextUtils.isEmpty(input))
                {
                    resetInput();
                    CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Input error", "Cannot post an empty comment.", false);
                    dialog.show();
                }
                else
                {
                    //progressDialog.show();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user != null)
                    {
                        String userId = user.getUid();
                        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
                        DatabaseReference mDatabase = db.getReference("ProductComments").child(model.getProductId());
                        Comment cmn = new Comment(userId, input, System.currentTimeMillis());
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String postedAt = sdf.format(new Date(System.currentTimeMillis()));
                        String key = mDatabase.push().getKey();
                        final HashMap<String, Object> comm = new HashMap<>();
                        comm.put(key, cmn);
                        mDatabase.updateChildren(comm).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    placeholderTextView.setVisibility(View.GONE);
                                    //progressDialog.dismiss();
                                    if(sortBy.equals("Latest"))
                                    {
                                        CommentRvItem item = new CommentRvItem(key, user.getDisplayName(), user.getEmail(), input, postedAt);
                                        resetInput();
                                        comments.add(0, item);
                                        commentsAdapter.notifyItemInserted(0);
                                    }
                                    else if(sortBy.equals("Oldest"))
                                    {
                                        if(!loadMore)
                                        {
                                            loadMore = true;
                                        }
                                        resetInput();
                                    }
                                }
                                else
                                {
                                    resetInput();
                                    //progressDialog.dismiss();
                                    CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Server/Network error", "Could not post this comment.", false);
                                    dialog.show();
                                }
                            }
                        });
                    }
                    else
                    {
                        resetInput();
                       // progressDialog.dismiss();
                        CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Server/Network error", "Cannot get session details.", false);
                        dialog.show();
                    }
                }
            }
        });

        progressDialog.show();

        categoryIcons = loadCategoryIcons();
        categoryTitles = loadCategoryTitles();

        model = getIntent().getParcelableExtra(CLICKED_KEY);
        origin = getIntent().getBooleanExtra(ORIGIN_KEY, false);

        placeholderTextView.setVisibility(View.GONE);
        loadBar.setVisibility(View.VISIBLE);

        if(model != null)
        {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            userKey = user.getUid();
            loadData();
        }
        else
        {
            progressDialog.dismiss();
            CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Server/Network error", "Error loading this view could not obtain the required information.", false);
            dialog.show();
        }
    }

    private void gotoEditProduct()
    {

    }

    private void userAddToCart()
    {
        progressDialog.show();
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference ref = db.getReference("Carts").child(userKey).child("CartItems");
        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                boolean found = false;
                for(DataSnapshot snap : snapshot.getChildren())
                {
                    if(snap.getKey().equals(model.getProductId()))
                    {
                        found = true;
                        break;
                    }
                }
                if(found)
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Task error", "This product is already added to your cart.", false);
                    dialog.show();
                }
                else
                {
                    addToCart();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Server/Network error", error.getMessage(), false);
                dialog.show();
            }
        });
    }

    private void addToCart()
    {
        final HashMap<String, Object> item = new HashMap<>();
        CartData data = new CartData(System.currentTimeMillis(), model.getPrice(), model.getOrders());
        item.put(model.getProductId(), data);
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference ref = db.getReference("Carts");
        ref.child(userKey).child("CartItems").updateChildren(item).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Product status", "Successfully added this product to your cart.", true);
                    dialog.show();
                }
                else
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Server/Network error", task.getException().getMessage(), false);
                    dialog.show();
                }
            }
        });
    }

    private void resetInput()
    {
        commentInput.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView != null)
        {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            if(commentInput.isFocused())
            {
                commentInput.clearFocus();
            }
        }
    }

    private void loadData()
    {
        Picasso.get().load(model.getImgUri()).placeholder(R.drawable.load_placeholder)
                .into(imageView);
        nameText.setText(model.getName());
        String price = "Price: " + model.getPrice() + "$";
        priceText.setText(price);
        int stock = model.getInStock();
        if(stock == 0)
        {
            stockText.setText(getResources().getString(R.string.no));
            stockText.setTextColor(getResources().getColor(R.color.mainTextColor));
        }
        else
        {
            stockText.setText(getResources().getString(R.string.yes));
            stockText.setTextColor(getResources().getColor(R.color.yesColor));
        }
        longDescText.setText(model.getLongDesc());
        String orders = "Orders: " + model.getOrders();
        ordersText.setText(orders);
        String category = model.getCategoryId();
        categoryTitle.setText(category);
        int i = 0;
        for(String comp : categoryTitles)
        {
            if(category.equals(comp))
            {
                categoryImage.setImageDrawable(categoryIcons[i]);
                break;
            }
            else {
                i++;
            }

        }
        getComments(sortBy, nextKey);
    }

    private void getComments(String sortOption, String nextId)
    {
        running = true;
        Query commentFetcher = getCommentsQuery(sortOption, nextId);
        if(commentFetcher != null)
        {
            if(sortOption.equals("Oldest"))
            {
                ValueEventListener listener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            int count = (int) snapshot.getChildrenCount();
                            int i = 1;
                            List<String> keys = new ArrayList<>();
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                Comment comm = snap.getValue(Comment.class);
                                keys.add(snap.getKey());
                                dbComments.add(comm);
                                if(i == count)
                                {
                                    prevKey = nextKey;
                                }
                                nextKey = snap.getKey();
                                i++;
                            }
                            counter = 0;
                            arrangeComments(keys);
                        }
                        else
                        {
                            running = false;
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }
                            loadBar.setVisibility(View.GONE);
                            loadMore = false;
                            if(comments.size() == 0)
                            {
                                placeholderTextView.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                Toast.makeText(ProductDetailsActivity.this, "No more comments left to display.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        if(progressDialog.isShowing())
                        {
                            progressDialog.dismiss();
                        }
                        loadBar.setVisibility(View.GONE);
                        if(comments.size() == 0)
                        {
                            placeholderTextView.setVisibility(View.VISIBLE);
                        }
                        CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                commentFetcher.addListenerForSingleValueEvent(listener);
            }
            else if(sortOption.equals("Latest"))
            {
                ValueEventListener listener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean flag = true;
                            boolean set = false;
                            int count = (int) snapshot.getChildrenCount();
                            ArrayList<Comment> sorter = new ArrayList<>();
                            ArrayList<String> keys = new ArrayList<>();
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                Comment comm = snap.getValue(Comment.class);
                                keys.add(snap.getKey());
                                sorter.add(comm);
                                if(flag)
                                {
                                    if(count == 1)
                                    {
                                        afterKey = snap.getKey();
                                    }
                                    nextKey = snap.getKey();
                                    flag = false;
                                    set = true;
                                }
                                if(set)
                                {
                                    afterKey = snap.getKey();
                                    set = false;
                                }
                            }
                            Collections.reverse(sorter);
                            Collections.reverse(keys);
                            for (Comment c : sorter)
                            {
                                dbComments.add(c);
                            }
                            counter = 0;
                            arrangeComments(keys);
                        }
                        else
                        {
                            running = false;
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }
                            loadBar.setVisibility(View.GONE);
                            loadMore = false;
                            if(comments.size() == 0)
                            {
                                placeholderTextView.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                Toast.makeText(ProductDetailsActivity.this, "No more comments left to display.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        if(progressDialog.isShowing())
                        {
                            progressDialog.dismiss();
                        }
                        loadBar.setVisibility(View.GONE);
                        if(comments.size() == 0)
                        {
                            placeholderTextView.setVisibility(View.VISIBLE);
                        }
                        CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                commentFetcher.addListenerForSingleValueEvent(listener);
            }
        }
        else
        {
            running = false;
            if(progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
            loadBar.setVisibility(View.GONE);
            if(comments.size() == 0)
            {
                placeholderTextView.setVisibility(View.VISIBLE);
            }
            CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Database/Network error", "Cannot specify a method for getting the comments.", false);
            dialog.show();
        }
    }

    private Query getCommentsQuery(String sortOption, String nextId)
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference mDatabase = db.getReference("ProductComments");
        Log.e("da", model.getProductId());
        DatabaseReference ref = mDatabase.child(model.getProductId()).getRef();
        if(sortOption.equals("Oldest"))
        {
            if(nextId == null)
            {
                return ref.orderByKey().limitToFirst(BATCH_SIZE);
            }
            return ref.orderByKey().startAfter(nextId).limitToFirst(BATCH_SIZE);
        }
        else if(sortOption.equals("Latest"))
        {
            if(nextId == null)
            {
                return ref.orderByKey().limitToLast(BATCH_SIZE);
            }
            return ref.orderByKey().endBefore(nextId).limitToLast(BATCH_SIZE);
        }
        else
        {
            return null;
        }
    }

    private void arrangeComments(List<String> ids)
    {
        if(counter >= dbComments.size())
        {
            running = false;
            if(progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
            loadBar.setVisibility(View.GONE);
            if(dbComments.size() > 0)
            {
                dbComments.clear();
                commentsAdapter.notifyDataSetChanged();
            }
            return;
        }
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference ref = db.getReference("Users");
        ref.orderByKey().equalTo(dbComments.get(counter).getUserId());

        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    for(DataSnapshot snap : snapshot.getChildren())
                    {
                        if(snap.getKey().equals(dbComments.get(counter).getUserId()))
                        {
                            User u = snap.getValue(User.class);
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            long postedAtLong = dbComments.get(counter).getPostedAt();
                            String postedAt = sdf.format(new Date(postedAtLong));
                            CommentRvItem item = new CommentRvItem(ids.get(counter),
                                    u.getFullName(), u.getEmail(), dbComments.get(counter).getContent(), postedAt);
                            comments.add(item);
                            break;
                        }
                    }
                    counter++;
                    arrangeComments(ids);
                }
                else
                {
                    running = false;
                    if(progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                    CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Database/Network error", "Cannot load the comments.", false);
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                running = false;
                if(progressDialog.isShowing())
                {
                    progressDialog.dismiss();
                }
                CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Database/Network error", "Cannot load the comments.", false);
                dialog.show();
            }
        });
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
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                if(origin)
                {
                    Intent intent = new Intent(ProductDetailsActivity.this, OrderDetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    CustomIntent.customType(ProductDetailsActivity.this, "right-to-left");
                    finish();
                }
                else
                {
                    Intent intent = new Intent(ProductDetailsActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    CustomIntent.customType(ProductDetailsActivity.this, "right-to-left");
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        String[] sortOptions = getResources().getStringArray(R.array.sortsComments);
        arrayAdapterSort = new ArrayAdapter<>(this, R.layout.category_dropdown_item, sortOptions);
        commentsSorter.setAdapter(arrayAdapterSort);

        commentsSorter.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String item = arrayAdapterSort.getItem(position);
                commentsSorter.clearFocus();
                if(!sortBy.equals(item))
                {
                    comments.clear();
                    commentsAdapter.notifyDataSetChanged();
                    sortBy = item;
                    prevKey = "";
                    afterKey = "";
                    nextKey = null;
                    loadMore = true;
                    getComments(sortBy, nextKey);
                }
                else
                {
                    Toast.makeText(ProductDetailsActivity.this, "Already displaying comments in this order.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        if(origin)
        {
            Intent intent = new Intent(ProductDetailsActivity.this, OrderDetailsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            CustomIntent.customType(ProductDetailsActivity.this, "right-to-left");
            finish();
        }
        else
        {
            Intent intent = new Intent(ProductDetailsActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            CustomIntent.customType(ProductDetailsActivity.this, "right-to-left");
            finish();
        }
    }

    private void changeStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

}