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
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class ProductDetailsActivity extends AppCompatActivity
{

    private static final String CLICKED_KEY = "com.example.eceshop.CLICKED_PRODUCT";
    private ProductRecyclerViewModel model;
    private Toolbar toolbar;
    private String productId;
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
        productId = "";
        nextKey = null;
        sortBy = "Latest";
        loadMore = true;
        dbComments = new ArrayList<>();

        progressDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        changeStatusBarColor();

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

        placeholderTextView.setVisibility(View.VISIBLE);

        comments = new ArrayList<>();
        commentsAdapter = new CommentsRecyclerViewAdapter(this, comments);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

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
                                    Log.e("das","The email matches.");
                                    int comp = comments.size() - pos;
                                    if(comp <= 3)
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
                                    DatabaseReference ref = db.getReference("Comments");
                                    ref.orderByChild("content").equalTo(item.getContent());
                                    progressDialog.show();
                                    ref.addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {
                                            for(DataSnapshot snap : snapshot.getChildren())
                                            {
                                                Comment c = snap.getValue(Comment.class);
                                                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                                long postedAtLong = c.getPostedAt();
                                                String postedAt = sdf.format(new Date(postedAtLong));
                                                if(postedAt.equals(item.getPostedAt()))
                                                {
                                                    snap.getRef().removeValue(new DatabaseReference.CompletionListener()
                                                    {
                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref)
                                                        {
                                                            progressDialog.dismiss();
                                                            comments.remove(pos);
                                                            commentsAdapter.notifyItemRemoved(pos);
                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error)
                                        {
                                            progressDialog.dismiss();
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
                        getComments(sortBy, nextKey);
                    }
                }
            }
        });

        addToCartBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e("GG", "Add to cart button is clicked.");
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
                    progressDialog.show();
                    Log.e("TAGGER", "Will try to post the comment.");
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user != null)
                    {
                        Log.e("TAGGER", "User has been found.");
                        String userId = user.getUid();
                        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
                        DatabaseReference mDatabase = db.getReference("Comments");
                        Comment cmn = new Comment(userId, productId, input, System.currentTimeMillis());
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String postedAt = sdf.format(new Date(System.currentTimeMillis()));
                        mDatabase.push().setValue(cmn).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    placeholderTextView.setVisibility(View.GONE);
                                    progressDialog.dismiss();
                                    Log.e("TAGGER", "Comment has been posted.");
                                    if(sortBy.equals("Latest"))
                                    {
                                        CommentRvItem item = new CommentRvItem(user.getDisplayName(), user.getEmail(), input, postedAt);
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
                                    Log.e("TAGGER", "Failed to post the comment.");
                                    resetInput();
                                    progressDialog.dismiss();
                                    CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Server/Network error", "Could not post this comment.", false);
                                    dialog.show();
                                }
                            }

                        });
                    }
                    else
                    {
                        Log.e("TAGGER", "Logged in user has not been found.");
                        resetInput();
                        progressDialog.dismiss();
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
        if(model != null)
        {
            Log.e("HG", "Object is not empty will print results.");
            Log.e("HG", "Name: " + model.getName());
            Log.e("HG", "Short description: " + model.getShortDesc());
            Log.e("HG", "Price: " + model.getPrice());
        }
        else
        {
            progressDialog.dismiss();
            Log.e("HG", "Empty object is passed.");
        }
        Log.e("TAGGER", "At end of onCreate will try to fetch the product.");
        getProduct();
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

    private void getProduct()
    {
        Log.e("TAGGER", "Inside get product.");
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference ref = db.getReference("Products");
        ref.orderByChild("name").equalTo(model.getName());
        ValueEventListener valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    for(DataSnapshot snap : snapshot.getChildren())
                    {
                        ProductRecyclerViewModel item = snap.getValue(ProductRecyclerViewModel.class);
                        Double price = snap.child("price").getValue(Double.class);
                        String shortDesc = snap.child("shortDesc").getValue(String.class);
                        String longDesc = snap.child("longDesc").getValue(String.class);
                        if(price.equals(model.getPrice()) && shortDesc.equals(model.getShortDesc()) && longDesc.equals(model.getLongDesc()))
                        {
                            productId = snap.getKey();
                            loadData();
                            break;
                        }
                    }
                    if(productId.equals(""))
                    {
                        progressDialog.dismiss();
                        CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Database/Network error", "Could not retrieve the specified product.", false);
                        dialog.show();
                    }
                }
                else
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Database/Network error", "This product does not exist.", false);
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Database/Network error", error.getMessage(), false);
                dialog.show();
            }
        };
        ref.addListenerForSingleValueEvent(valueEventListener);
    }

    private void loadData()
    {
        Picasso.get().load(model.getImgUri()).into(imageView);
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
        Log.e("TAGGER", "At end of loadData will try to fetch the comments.");
        getComments(sortBy, nextKey);
    }

    private void getComments(String sortOption, String nextId)
    {
        Query commentFetcher = getCommentsQuery(sortOption, nextId);
        if(commentFetcher != null)
        {
            if(sortOption.equals("Oldest"))
            {
                Log.e("TAGGER", "Fetching latest comments.");
                ValueEventListener listener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            Log.e("TAGGER", "Comments exist.");
                            boolean added = false;
                            int count = (int) snapshot.getChildrenCount();
                            int i = 1;
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                Comment comm = snap.getValue(Comment.class);
                                if(comm.getProductId().equals(productId))
                                {
                                    Log.e("TAGGER", "A comment has been found for this product.");
                                    added = true;
                                    dbComments.add(comm);
                                }
                                if(i == count)
                                {
                                    prevKey = nextKey;
                                }
                                nextKey = snap.getKey();
                                i++;
                            }
                            if(added)
                            {
                                Log.e("TAGGER", "Will proceed to arrangeComments. dbComments.size() = " + dbComments.size());
                                counter = 0;
                                arrangeComments();
                            }
                            else
                            {
                                Log.e("TAGGER", "No comments was found in this batch will recursively proceed to the next batch.");
                                getComments(sortBy, nextKey);
                            }
                        }
                        else
                        {
                            Log.e("TAGGER", "No more comments can be found the end of the Db list is reached.");
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }
                            loadMore = false;
                            if(comments.size() > 0)
                            {
                                Toast.makeText(ProductDetailsActivity.this, "No more comments left to display.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        if(progressDialog.isShowing())
                        {
                            progressDialog.dismiss();
                        }
                        CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                };
                commentFetcher.addListenerForSingleValueEvent(listener);
            }
            else if(sortOption.equals("Latest"))
            {
                Log.e("TAGGER", "Will fetch the oldest comments.");
                ValueEventListener listener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean flag = true;
                            boolean added = false;
                            boolean set = false;
                            int count = (int) snapshot.getChildrenCount();
                            ArrayList<Comment> sorter = new ArrayList<>();
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                Comment comm = snap.getValue(Comment.class);
                                if(comm.getProductId().equals(productId))
                                {
                                    Log.e("TAGGER", "A comments has been found");
                                    added = true;
                                    sorter.add(comm);
                                }
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
                            if(added)
                            {
                                Collections.reverse(sorter);
                                for (Comment c : sorter)
                                {
                                    dbComments.add(c);
                                }
                                counter = 0;
                                Log.e("TAGGER", "Will proceed to arrangeComments. dbComments.size() = " + dbComments.size());
                                arrangeComments();
                            }
                            else
                            {
                                Log.e("TAGGER", "No comments were found in this batch proceeding to the next.");
                                getComments(sortBy, nextKey);
                            }
                        }
                        else
                        {
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }
                            loadMore = false;
                            if(comments.size() > 0)
                            {
                                Toast.makeText(ProductDetailsActivity.this, "No more comments left to display.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        if(progressDialog.isShowing())
                        {
                            progressDialog.dismiss();
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
            if(progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
            CustomDialog dialog = new CustomDialog(ProductDetailsActivity.this, "Database/Network error", "Cannot specify a method for getting the comments.", false);
            dialog.show();
        }
    }

    private Query getCommentsQuery(String sortOption, String nextId)
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference ref = db.getReference("Comments");
        if(sortOption.equals("Oldest"))
        {
            if(nextId == null)
            {
                return ref.orderByKey().limitToFirst(3);
            }
            return ref.orderByKey().startAfter(nextId).limitToFirst(3);
        }
        else if(sortOption.equals("Latest"))
        {
            if(nextId == null)
            {
                return ref.orderByKey().limitToLast(3);
            }
            return ref.orderByKey().endBefore(nextId).limitToLast(3);
        }
        else
        {
            return null;
        }
    }

    private void arrangeComments()
    {
        Log.e("JJ", "Inside arrangeComments.");
        Log.e("JJ", "COUNTER = " + counter + ";  SIZE = " + dbComments.size());
        if(counter >= dbComments.size())
        {
            Log.e("JJ", "Will exit arrange comments.");
            if(progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
            if(dbComments.size() > 0)
            {
                Log.e("JJ", "Will reset comments array in arrange comments.");
                placeholderTextView.setVisibility(View.GONE);
                dbComments.clear();
                commentsAdapter.notifyDataSetChanged();
            }
            return;
        }
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference ref = db.getReference("Users");
        Log.e("JJ", dbComments.get(counter).getUserId());
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
                            Log.e("JJ","Counter=" + counter + "   Size=" + dbComments.size());
                            User u = snap.getValue(User.class);
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            long postedAtLong = dbComments.get(counter).getPostedAt();
                            String postedAt = sdf.format(new Date(postedAtLong));
                            Log.e("JJ",u.getFullName());
                            CommentRvItem item = new CommentRvItem(u.getFullName(), u.getEmail(), dbComments.get(counter).getContent(), postedAt);
                            comments.add(item);
                            break;
                        }
                    }
                    counter++;
                    arrangeComments();
                }
                else
                {
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
                Intent intent = new Intent(ProductDetailsActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                CustomIntent.customType(ProductDetailsActivity.this, "right-to-left");
                finish();
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
                Log.e("JJ", "Inside on ItemClick of comments sorter.");
                String item = arrayAdapterSort.getItem(position);
                commentsSorter.clearFocus();
                if(!sortBy.equals(item))
                {
                    Log.e("JJ", "Will proceed to sorting.");
                    comments.clear();
                    commentsAdapter.notifyDataSetChanged();
                    sortBy = item;
                    prevKey = "";
                    afterKey = "";
                    nextKey = null;
                    loadMore = true;
                    getComments(sortBy, nextKey);
                    Log.e("GG", "Will sort by: " + sortBy);
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
        Intent intent = new Intent(ProductDetailsActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        CustomIntent.customType(ProductDetailsActivity.this, "right-to-left");
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