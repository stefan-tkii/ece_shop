package com.example.eceshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import maes.tech.intentanim.CustomIntent;

public class UserManagementFragment extends Fragment implements UserItemRecyclerViewAdapter.OnUserItemClickListener
{

    private NestedScrollView usersContainer;
    private FloatingActionButton backToTopFab;
    private RecyclerView usersRecyclerView;
    private ProgressBar loadBar;
    private TextInputEditText searchFilterInput;
    private AutoCompleteTextView sortSelector;

    private userManagementFragmentTouchListener listener;
    private ArrayAdapter<String> arrayUsersSort;
    private UserItemRecyclerViewAdapter usersAdapter;
    private ArrayList<UserRvItem> usersList;

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference ordersRef;

    private static final String USER_DATA_KEY = "com.example.eceshop.CLICKED_USER";

    private static final int BATCH_SIZE = 6;
    private boolean loadMore;
    private boolean running;
    private String sortBy;
    private String searchFilter;
    private String nextKey;
    private int prevSize;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.user_management_fragment_layout, container, false);
        usersContainer = root.findViewById(R.id.users_container);
        backToTopFab = root.findViewById(R.id.backToTopUsers);
        usersRecyclerView = root.findViewById(R.id.userItemsRecyclerView);
        loadBar = root.findViewById(R.id.user_item_load_bar);
        searchFilterInput = root.findViewById(R.id.emailFilter_input);
        sortSelector = root.findViewById(R.id.users_Sort_Selector);

        loadMore = true;
        running = false;
        sortBy = "Newest";
        searchFilter = "";
        nextKey = "";
        usersList = new ArrayList<>();
        prevSize = 0;

        database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        usersRef = database.getReference("Users");
        ordersRef = database.getReference("Orders");

        usersContainer.setOnTouchListener(new View.OnTouchListener()
        {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                listener.onUserManagementFragmentTouch();
                InputMethodManager imm = (InputMethodManager) getActivityNonNull().getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = getActivityNonNull().getCurrentFocus();
                if (focusedView != null)
                {
                    imm.hideSoftInputFromWindow(getActivityNonNull().getCurrentFocus().getWindowToken(), 0);
                    if(searchFilterInput.isFocused())
                    {
                        searchFilterInput.clearFocus();
                    }
                    else if(sortSelector.isFocused())
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

        backToTopFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                usersContainer.smoothScrollTo(0,0);
            }
        });

        usersContainer.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener()
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
                            backToTopFab.setVisibility(View.GONE);
                        }
                    }, 2000);
                }
                if (scrollY < oldScrollY)
                {
                    backToTopFab.setVisibility(View.VISIBLE);
                }
                if (scrollY == 0)
                {
                    backToTopFab.setVisibility(View.GONE);
                }
                if(scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()))
                {
                    backToTopFab.setVisibility(View.VISIBLE);
                    if(loadMore)
                    {
                        if(!running)
                        {
                            loadBar.setVisibility(View.VISIBLE);
                            getUsers();
                        }
                    }
                }
            }
        });

        searchFilterInput.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    InputMethodManager imm = (InputMethodManager) getActivityNonNull().getSystemService(Context.INPUT_METHOD_SERVICE);
                    View focusedView = getActivityNonNull().getCurrentFocus();
                    if (focusedView != null)
                    {
                        imm.hideSoftInputFromWindow(getActivityNonNull().getCurrentFocus().getWindowToken(), 0);
                        searchFilterInput.clearFocus();
                    }
                    String input = searchFilterInput.getText().toString();
                    if(input.equals("All"))
                    {
                        if(!running)
                        {
                            searchFilter = "";
                            loadMore = true;
                            prevSize = 0;
                            loadBar.setVisibility(View.VISIBLE);
                            nextKey = "";
                            usersList.clear();
                            usersAdapter.notifyDataSetChanged();
                            getUsers();
                        }
                    }
                    else if(!TextUtils.isEmpty(input) && Patterns.EMAIL_ADDRESS.matcher(input).matches())
                    {
                        if(!running)
                        {
                            loadMore = true;
                            loadBar.setVisibility(View.VISIBLE);
                            nextKey = "";
                            prevSize = 0;
                            searchFilter = input;
                            usersList.clear();
                            usersAdapter.notifyDataSetChanged();
                            getUsers();
                        }
                    }
                    else
                    {
                        searchFilterInput.setText("");
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Either type 'All' to fetch all users or type an email address in a valid format.", false);
                        dialog.show();
                    }
                    return true;
                }
                return false;
            }
        });

        usersRecyclerView.setDrawingCacheEnabled(true);
        usersRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        usersAdapter = new UserItemRecyclerViewAdapter(getActivityNonNull(), usersList);
        usersAdapter.setOnUserItemClickListener(this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivityNonNull()));
        usersRecyclerView.setAdapter(usersAdapter);

        loadBar.setVisibility(View.VISIBLE);

        getUsers();

        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        String[] sortOptions = getResources().getStringArray(R.array.sortUsers);
        arrayUsersSort = new ArrayAdapter<>(getContext(), R.layout.category_dropdown_item, sortOptions);
        sortSelector.setAdapter(arrayUsersSort);

        sortSelector.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String item = arrayUsersSort.getItem(position);
                sortSelector.clearFocus();

                if(!sortBy.equals(item))
                {
                    if(!running)
                    {
                        sortBy = item;
                        nextKey = "";
                        usersList.clear();
                        prevSize = 0;
                        usersAdapter.notifyDataSetChanged();
                        loadMore = true;
                        loadBar.setVisibility(View.VISIBLE);
                        getUsers();
                    }
                }
                else
                {
                    Toast.makeText(getActivityNonNull(), "Already displaying the users in this order.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getUsers()
    {
        running = true;
        Query q = getUsersQuery();
        if(q != null)
        {
            if(sortBy.equals("Oldest"))
            {
                q.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean added = false;
                            List<UserRvItem> dataList = new ArrayList<>();
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                User u = snap.getValue(User.class);
                                if(!searchFilter.equals(""))
                                {
                                    boolean check = checkIfFilterMatches(searchFilter, u.getEmail());
                                    if(check && (!u.isAdmin()))
                                    {
                                        UserRvItem item = new UserRvItem(snap.getKey(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getCountry(),
                                                0, false);
                                        dataList.add(item);
                                        added = true;
                                    }
                                }
                                else
                                {
                                    if(!u.isAdmin())
                                    {
                                        UserRvItem item = new UserRvItem(snap.getKey(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getCountry(),
                                                0, false);
                                        dataList.add(item);
                                        added = true;
                                    }
                                }
                                nextKey = snap.getKey();
                            }
                            if(!searchFilter.equals("") && (!added))
                            {
                                getUsers();
                            }
                            else if(added)
                            {
                                loadUserOrders(dataList);
                            }
                            else
                            {
                                getUsers();
                            }
                        }
                        else
                        {
                            running = false;
                            loadBar.setVisibility(View.GONE);
                            loadMore = false;
                            Toast.makeText(getActivityNonNull(), "No more users left to display for this filtering and order.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        loadMore = false;
                        loadBar.setVisibility(View.GONE);
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                });
            }
            else if(sortBy.equals("Newest"))
            {
                q.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean added = false;
                            List<UserRvItem> dataList = new ArrayList<>();
                            boolean flag = true;
                            List<UserHelper> sorter = new ArrayList<>();
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                User u = snap.getValue(User.class);
                                if(flag)
                                {
                                    nextKey = snap.getKey();
                                    flag = false;
                                }
                                UserHelper helper = new UserHelper(snap.getKey(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getCountry(),
                                        u.isAdmin());
                                sorter.add(helper);
                            }
                            Collections.reverse(sorter);
                            for(UserHelper u : sorter)
                            {
                                if(!searchFilter.equals(""))
                                {
                                    boolean check = checkIfFilterMatches(searchFilter, u.getEmail());
                                    if(check && (!u.isAdmin()))
                                    {
                                        UserRvItem item = new UserRvItem(u.getUserId(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getCountry(),
                                                0, false);
                                        dataList.add(item);
                                        added = true;
                                    }
                                }
                                else
                                {
                                    if(!u.isAdmin())
                                    {
                                        UserRvItem item = new UserRvItem(u.getUserId(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getCountry(),
                                                0, false);
                                        dataList.add(item);
                                        added = true;
                                    }
                                }
                            }
                            if(!searchFilter.equals("") && (!added))
                            {
                                getUsers();
                            }
                            else if(added)
                            {
                                loadUserOrders(dataList);
                            }
                            else
                            {
                                getUsers();
                            }
                        }
                        else
                        {
                            running = false;
                            loadBar.setVisibility(View.GONE);
                            loadMore = false;
                            Toast.makeText(getActivityNonNull(), "No more users left to display for this filtering and order.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        loadMore = false;
                        loadBar.setVisibility(View.GONE);
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                });
            }
            else if(sortBy.equals("Active orders"))
            {
                q.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                            boolean added = false;
                            List<UserRvItem> dataList = new ArrayList<>();
                            boolean flag = true;
                            List<UserHelper> sorter = new ArrayList<>();
                            for(DataSnapshot snap : snapshot.getChildren())
                            {
                                User u = snap.getValue(User.class);
                                if(flag)
                                {
                                    nextKey = snap.getKey();
                                    flag = false;
                                }
                                UserHelper helper = new UserHelper(snap.getKey(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getCountry(),
                                        u.isAdmin());
                                sorter.add(helper);
                            }
                            Collections.reverse(sorter);
                            for(UserHelper u : sorter)
                            {
                                if(!searchFilter.equals(""))
                                {
                                    boolean check = checkIfFilterMatches(searchFilter, u.getEmail());
                                    if(check && (!u.isAdmin()))
                                    {
                                        UserRvItem item = new UserRvItem(u.getUserId(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getCountry(),
                                                0, false);
                                        dataList.add(item);
                                        added = true;
                                    }
                                }
                                else
                                {
                                    if(!u.isAdmin())
                                    {
                                        UserRvItem item = new UserRvItem(u.getUserId(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getCountry(),
                                                0, false);
                                        dataList.add(item);
                                        added = true;
                                    }
                                }
                            }
                            if(!searchFilter.equals("") && (!added))
                            {
                                getUsers();
                            }
                            else if(added)
                            {
                                loadUserOrders(dataList);
                            }
                            else
                            {
                                getUsers();
                            }
                        }
                        else
                        {
                            running = false;
                            loadBar.setVisibility(View.GONE);
                            loadMore = false;
                            Toast.makeText(getActivityNonNull(), "No more users left to display for this filtering and order.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        running = false;
                        loadMore = false;
                        loadBar.setVisibility(View.GONE);
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                        dialog.show();
                    }
                });
            }
        }
        else
        {
            running = false;
            loadBar.setVisibility(View.GONE);
            Toast.makeText(getActivityNonNull(), "Cannot filter users in this order.", Toast.LENGTH_SHORT).show();
        }
    }

    private Query getUsersQuery()
    {
       if(sortBy.equals("Oldest"))
       {
           if(nextKey.equals(""))
           {
               return usersRef.orderByKey().limitToFirst(BATCH_SIZE);
           }
           return usersRef.orderByKey().startAfter(nextKey).limitToFirst(BATCH_SIZE);
       }
       else if(sortBy.equals("Newest") || sortBy.equals("Active orders"))
       {
           if(nextKey.equals(""))
           {
               return usersRef.orderByKey().limitToLast(BATCH_SIZE);
           }
           return usersRef.orderByKey().endBefore(nextKey).limitToLast(BATCH_SIZE);
       }
       else
       {
           return null;
       }
    }

    private void loadUserOrders(List<UserRvItem> list)
    {
        if(list.size() == 0)
        {
            if(prevSize == usersList.size())
            {
                getUsers();
                return;
            }
            else
            {
                loadBar.setVisibility(View.GONE);
                prevSize = usersList.size();
                running = false;
                usersAdapter.notifyDataSetChanged();
                return;
            }
        }
        UserRvItem item = list.remove(0);
        ordersRef.child(item.getUserId()).getRef().addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    int count = (int)snapshot.getChildrenCount();
                    boolean active = false;
                    for(DataSnapshot snap : snapshot.getChildren())
                    {
                        OrderDb o = snap.getValue(OrderDb.class);
                        if(o.getStatus().equals("Ongoing"))
                        {
                            active = true;
                        }
                    }
                    if(!sortBy.equals("Active orders"))
                    {
                        item.setOrderCount(count);
                        item.setHasActiveOrder(active);
                        usersList.add(item);
                        loadUserOrders(list);
                    }
                    else
                    {
                        if(active)
                        {
                            item.setOrderCount(count);
                            item.setHasActiveOrder(active);
                            usersList.add(item);
                            loadUserOrders(list);
                        }
                        else
                        {
                            loadUserOrders(list);
                        }
                    }
                }
                else
                {
                    if(!sortBy.equals("Active orders"))
                    {
                        usersList.add(item);
                        loadUserOrders(list);
                    }
                    else
                    {
                        loadUserOrders(list);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                running = false;
                loadMore = false;
                loadBar.setVisibility(View.GONE);
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                dialog.show();
            }
        });
    }

    @Override
    public void OnUserItemClick(int position, UserRvItem clickedUser)
    {
        Intent intent = new Intent(getActivityNonNull(), UserDetailsActivity.class);
        intent.putExtra(USER_DATA_KEY, clickedUser);
        startActivity(intent);
        CustomIntent.customType(getActivityNonNull(), "left-to-right");
    }

    private boolean checkIfFilterMatches(String filter, String userEmail)
    {
        String filterValue = filter.split("@")[0];
        String userValue = userEmail.split("@")[0];
        if(Pattern.compile(Pattern.quote(filterValue), Pattern.CASE_INSENSITIVE).matcher(userValue).find())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public interface userManagementFragmentTouchListener
    {
        void onUserManagementFragmentTouch();
    }

    protected FragmentActivity getActivityNonNull()
    {
        if (super.getActivity() != null)
        {
            return super.getActivity();
        }
        else
        {
            throw new RuntimeException("null returned from getActivity()");
        }
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        if(context instanceof userManagementFragmentTouchListener)
        {
            listener = (userManagementFragmentTouchListener) context;
        }
        else
        {
            throw  new RuntimeException(context.toString() + " must implement the userManagementFragmentTouchListener.");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        listener = null;
    }

}
