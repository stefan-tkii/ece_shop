package com.example.eceshop;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class HomeActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener, DashboardFragment.DashboardFragmentTouchListener, CartFragment.CartFragmentTouchListener
{

    private FirebaseAuth mAuth;
    private static final int POS_CLOSE = 0;
    private static final int POS_DASHBOARD = 1;
    private static final int POS_CART = 2;
    private static final int POS_ORDERS = 3;
    private static final int POS_PROFILE = 4;
    private static final int POS_SETTINGS = 5;
    private static final int POS_LOGOUT = 7;

    TextView textNotificationsItemCount;
    int mNotificationsItemCount = 1;

    private boolean bundleStatus;
    private Bundle bundle;

    private String lastSearched;

    private SuggestionDatabase database;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private AlertDialog progressDialog;

    private FirebaseAuth.AuthStateListener authStateListener;

    private SlidingRootNav slidingRootNav;

    private int selectedNavigation;
    private DrawerAdapter adapter;

    private SearchView searchView;

    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        showCustomUI();
        mAuth = FirebaseAuth.getInstance();
        Log.e("Home", "Inside on create of home activity.");
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        bundleStatus = false;
        bundle = new Bundle();
        lastSearched = "";

        database = new SuggestionDatabase(this);

        progressDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        selectedNavigation = -1;

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null)
                {
                    Intent intent = new Intent(HomeActivity.this, SigningActivity.class);
                    if(progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                    startActivity(intent);
                    CustomIntent.customType(HomeActivity.this, "right-to-left");
                    finish();
                }
            }
        };

        mAuth.addAuthStateListener(authStateListener);

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withDragDistance(180)
                .withRootViewScale(0.75f)
                .withRootViewElevation(25)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.drawer_menu)
                .inject();

        changeStatusBarColor();

        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        adapter = new DrawerAdapter(Arrays.asList(
           createItemFor(POS_CLOSE),
                createItemFor(POS_DASHBOARD).setChecked(true),
                createItemFor(POS_CART),
                createItemFor(POS_ORDERS),
                createItemFor(POS_PROFILE),
                createItemFor(POS_SETTINGS),
                new SpaceItem(260),
                createItemFor(POS_LOGOUT)
        ));

        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.drawer_list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_DASHBOARD);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_notification);
        View actionView = menuItem.getActionView();
        textNotificationsItemCount = (TextView) actionView.findViewById(R.id.cart_badge);

        setupBadge();

        actionView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener()
        {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {
                if(selectedNavigation == POS_DASHBOARD)
                {
                    if(lastSearched.equals(""))
                    {
                        return true;
                    }
                    bundle.putString("searchInput", "");
                    bundleStatus = false;
                    lastSearched = "";
                    DashboardFragment fragInfo = new DashboardFragment();
                    fragInfo.setArguments(bundle);
                    resetInput(searchView);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_container, fragInfo).commit();
                    return true;
                }
                else if(selectedNavigation == POS_CART)
                {
                    bundle.putString("searchInput", "");
                    bundleStatus = false;
                    lastSearched = "";
                    resetInput(searchView);
                    return true;
                }
                else if(selectedNavigation == POS_ORDERS)
                {
                    bundle.putString("searchInput", "");
                    bundleStatus = false;
                    lastSearched = "";
                    resetInput(searchView);
                    return true;
                }
                else if(selectedNavigation == POS_PROFILE)
                {
                    bundle.putString("searchInput", "");
                    bundleStatus = false;
                    lastSearched = "";
                    resetInput(searchView);
                    return true;
                }
                else if(selectedNavigation == POS_SETTINGS)
                {
                    bundle.putString("searchInput", "");
                    bundleStatus = false;
                    lastSearched = "";
                    resetInput(searchView);
                    return true;
                }
                else
                {
                    return true;
                }
            }
        };

        menu.findItem(R.id.action_search).setOnActionExpandListener(onActionExpandListener);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("Search for a product");

        searchView.setBackground(getResources().getDrawable(R.drawable.search_view_bg));

        ImageView searchClose = (ImageView) searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(R.drawable.ic_delete);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                query = query.trim();
                long result = database.insertSuggestion(query);
                if(result != -1)
                {
                    Log.e("T", "Inserted suggestion.");
                }
                if(lastSearched != null)
                {
                    if(lastSearched.equals(query))
                    {
                        resetInput(searchView);
                        Toast.makeText(HomeActivity.this, "Already displaying results for this query.",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    else
                    {
                        lastSearched = query;
                    }
                }
                else
                {
                    lastSearched = query;
                }
                bundle.putString("searchInput", query);
                bundleStatus = true;
                resetInput(searchView);
                adapter.setSelected(POS_DASHBOARD);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                Cursor cursor = database.getSuggestions(newText);
                if(cursor.getCount() != 0)
                {
                    String[] columns = new String[] {SuggestionDatabase.FIELD_SUGGESTION };
                    int[] columnTextId = new int[] { R.id.suggestion_text};

                    SuggestionSimpleCursorAdapter simple = new SuggestionSimpleCursorAdapter(getBaseContext(),
                            R.layout.suggestion_layout, cursor,
                            columns , columnTextId
                            , 0);

                    searchView.setSuggestionsAdapter(simple);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
        {
            @Override
            public boolean onSuggestionSelect(int position)
            {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position)
            {
                SQLiteCursor cursor = (SQLiteCursor) searchView.getSuggestionsAdapter().getItem(position);
                int indexColumnSuggestion = cursor.getColumnIndex(SuggestionDatabase.FIELD_SUGGESTION);

                searchView.setQuery(cursor.getString(indexColumnSuggestion), false);

                return true;
            }
        });

        return true;
    }

    private void changeStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void setupBadge()
    {
        //get notifications and update the count of mNotificationsItemCount
        if (textNotificationsItemCount != null)
        {
            if (mNotificationsItemCount == 0)
            {
                if (textNotificationsItemCount.getVisibility() != View.GONE)
                {
                    textNotificationsItemCount.setVisibility(View.GONE);
                }
            }
            else {
                    textNotificationsItemCount.setText(String.valueOf(Math.min(mNotificationsItemCount, 99)));
                    if (textNotificationsItemCount.getVisibility() != View.VISIBLE)
                    {
                        textNotificationsItemCount.setVisibility(View.VISIBLE);
                    }
            }
        }
        //open drop down list of notifications
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

    private void resetInput(ViewGroup v)
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView != null)
        {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            if(checkFocusRec(v))
            {
                clearFocus(v);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_notification:
                mNotificationsItemCount = 0;
                setupBadge();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private DrawerItem createItemFor(int position)
    {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.mainTextColor))
                .withTitleTint(color(R.color.black))
                .withSelectedIconTint(color(R.color.mainTextColor))
                .withSelectedTitleTint(color(R.color.mainTextColor));
    }

    @ColorInt
    private int color(@ColorRes int res)
    {
        return ContextCompat.getColor(this, res);
    }

    private String[] loadScreenTitles()
    {
        return getResources().getStringArray(R.array.id_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons()
    {
        TypedArray ta = getResources().obtainTypedArray(R.array.id_activityScreenIcons);
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

    private void showCustomUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    @Override
    public void onBackPressed()
    {
        if(slidingRootNav.isMenuOpened())
        {
            slidingRootNav.closeMenu();
        }
        else
        {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
            {
                super.onBackPressed();
                return;
            }
            else {

                Toast.makeText(getBaseContext(), "Tap the back button again in order to exit.", Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();
        }
    }

    @Override
    public void onItemSelected(int position)
    {
        if(position == POS_CLOSE)
        {
            slidingRootNav.closeMenu();
        }
        else if(position == POS_DASHBOARD)
        {
            if(selectedNavigation == POS_DASHBOARD)
            {
                if(bundleStatus)
                {
                    DashboardFragment fragInfo = new DashboardFragment();
                    fragInfo.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_container, fragInfo).commit();
                    slidingRootNav.closeMenu();
                }
                else
                {
                    slidingRootNav.closeMenu();
                }
            }
            else {
                if(bundleStatus)
                {
                    selectedNavigation = POS_DASHBOARD;
                    DashboardFragment fragInfo = new DashboardFragment();
                    fragInfo.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_container, fragInfo).commit();
                    slidingRootNav.closeMenu();
                }
                else
                {
                    selectedNavigation = POS_DASHBOARD;
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_container, new DashboardFragment()).commit();
                    slidingRootNav.closeMenu();
                }
            }
        }
        else if(position == POS_CART)
        {
            if(selectedNavigation == POS_CART)
            {
                slidingRootNav.closeMenu();
            }
            else {
                selectedNavigation = POS_CART;
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, new CartFragment()).commit();
                slidingRootNav.closeMenu();
            }
        }
        else if(position == POS_ORDERS)
        {
            if(selectedNavigation == POS_ORDERS)
            {
                slidingRootNav.closeMenu();
            }
            else {
                selectedNavigation = POS_ORDERS;
                slidingRootNav.closeMenu();
            }
        }
        else if(position == POS_PROFILE)
        {
            if(selectedNavigation == POS_PROFILE)
            {
                slidingRootNav.closeMenu();
            }
            else {
                selectedNavigation = POS_PROFILE;
                slidingRootNav.closeMenu();
            }
        }
        else if(position == POS_SETTINGS)
        {
            if(selectedNavigation == POS_SETTINGS)
            {

                slidingRootNav.closeMenu();
            }
            else
            {
                selectedNavigation = POS_SETTINGS;
                slidingRootNav.closeMenu();
            }
        }
        else if(position == POS_LOGOUT)
        {
            UserInfo providerData = mAuth.getCurrentUser().getProviderData().get(1);
            String providerId = providerData.getProviderId();
            if(providerId.equals("password"))
            {
                progressDialog.show();
                mAuth.signOut();
            }
            else if(providerId.equals("google.com"))
            {
                progressDialog.show();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                mGoogleSignInClient.signOut();
                mAuth.signOut();
            }
            else if(providerId.equals("facebook.com"))
            {
                progressDialog.show();
                LoginManager fbManager = LoginManager.getInstance();
                fbManager.logOut();
                mAuth.signOut();
            }
        }
    }

    @Override
    public void onDashboardFragmentTouch()
    {
        resetInput(searchView);
    }

    @Override
    public void onCartFragmentTouch()
    {
        resetInput(searchView);
    }

}