package com.example.eceshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class OrderDetailsActivity extends AppCompatActivity
{

    private Toolbar toolbar;

    private AlertDialog progressDialog;
    private Order model;
    private String userId;
    private FirebaseDatabase database;
    private DatabaseReference mainRef;
    private static final String CLICKED_ORDER_KEY = "com.example.eceshop.CLICKED_ORDER";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        toolbar = findViewById(R.id.order_details_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        changeStatusBarColor();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");

        getIntentData();
    }

    private void getIntentData()
    {
        model = getIntent().getParcelableExtra(CLICKED_ORDER_KEY);
        if(model != null)
        {
            DatabaseReference firstRef = database.getReference("Orders");
            DatabaseReference secondRef = firstRef.child(userId).getRef();
            mainRef = secondRef.child(model.getOrderId()).getRef();
            //to continue with populating
        }
        else
        {
            CustomDialog dialog = new CustomDialog(OrderDetailsActivity.this, "Server/Network error", "Error loading this view could not obtain the required information.", false);
            dialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(OrderDetailsActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                CustomIntent.customType(OrderDetailsActivity.this, "right-to-left");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(OrderDetailsActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        CustomIntent.customType(OrderDetailsActivity.this, "right-to-left");
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