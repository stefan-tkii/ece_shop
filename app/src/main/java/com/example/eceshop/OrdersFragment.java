package com.example.eceshop;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class OrdersFragment extends Fragment
{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.orders_fragment, container, false);
        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
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

}
