package com.example.eceshop;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SigningAdapter extends FragmentPagerAdapter
{

    private Context context;
    private int totalTabs;
    private static final String loginTitle = "Login";
    private static final String registerTitle = "Register";
    private String origin;
    private Product p;
    private Order o;
    private static final String PRODUCT_KEY = "com.example.eceshop.PRODUCT_VALUE";
    private static final String NAVIGATION_FLAG = "com.example.eceshop.NAVIGATION_KEY";

    public SigningAdapter(FragmentManager fm, Context context, int totalTabs, @Nullable String origin)
    {
        super(fm);
        this.context = context;
        this.totalTabs = totalTabs;
        this.origin = origin;
    }

    public void setProduct(Product p)
    {
        this.p = p;
    }

    public void setOrder(Order o)
    {
        this.o = o;
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                LoginFragment loginFragment = new LoginFragment();
                if(origin != null)
                {
                    if(origin.equals("notification_added"))
                    {
                        Bundle b = new Bundle();
                        b.putParcelable(PRODUCT_KEY, p);
                        b.putString(NAVIGATION_FLAG, origin);
                        loginFragment.setArguments(b);
                    }
                    else if(origin.equals("order_change"))
                    {
                        Bundle b = new Bundle();
                        b.putParcelable(PRODUCT_KEY, o);
                        b.putString(NAVIGATION_FLAG, origin);
                        loginFragment.setArguments(b);
                    }
                }
                return loginFragment;
            case 1:
                RegisterFragment registerFragment = new RegisterFragment();
                return registerFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return totalTabs;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return loginTitle;
            case 1:
                return registerTitle;
            default:
                return null;
        }
    }

}
