package com.example.eceshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import maes.tech.intentanim.CustomIntent;

public class MainActivity extends AppCompatActivity
{

    private Animation topAnim;
    private Animation bottomAnim;
    private ImageView image;
    private TextView title;
    private TextView slogan;

    private String userId;
    private static final String ADMIN_KEY = "com.example.eceshop.Admin";
    private static final String PRODUCT_KEY = "com.example.eceshop.PRODUCT_VALUE";
    private static final String NAVIGATION_FLAG = "com.example.eceshop.NAVIGATION_KEY";

    private static int SPLASH_SCREEN_DURATION = 3000;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    private String token;
    private String originFlag;
    private Product product;
    private Order order;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        showCustomUI();
        userId = "";
        originFlag = null;

        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                token = SharedPreferenceManager.getInstance(MainActivity.this).getToken();
                Log.e("Token", token);
            }
        };

        if(SharedPreferenceManager.getInstance(MainActivity.this).getToken() != null)
        {
            token = SharedPreferenceManager.getInstance(MainActivity.this).getToken();
            Log.e("Token", token);
        }

        registerReceiver(receiver, new IntentFilter(EceShopMessagingService.TOKEN_BROADCAST));
        if(getIntent().getStringExtra("origin") != null)
        {
            originFlag = getIntent().getStringExtra("origin");
            if(originFlag.equals("notification_added"))
            {
                product = getIntent().getParcelableExtra("object");
            }
            else if(originFlag.equals("order_change"))
            {
                order = getIntent().getParcelableExtra("object");
            }
        }

        boolean flag = checkIfLoggedIn();

        if(flag)
        {
            topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
            bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

            image = findViewById(R.id.imageView);
            title = findViewById(R.id.textView);
            slogan = findViewById(R.id.textView2);

            image.setAnimation(topAnim);
            title.setAnimation(bottomAnim);
            slogan.setAnimation(bottomAnim);

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                   checkAdmin(userId);
                }
            }, SPLASH_SCREEN_DURATION);
        }
        else
        {
            topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
            bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

            image = findViewById(R.id.imageView);
            title = findViewById(R.id.textView);
            slogan = findViewById(R.id.textView2);

            image.setAnimation(topAnim);
            title.setAnimation(bottomAnim);
            slogan.setAnimation(bottomAnim);

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    Intent intent = new Intent(MainActivity.this, SigningActivity.class);
                    if(originFlag != null)
                    {
                        if(originFlag.equals("notification_added"))
                        {
                            intent.putExtra(PRODUCT_KEY, product);
                        }
                        else if(originFlag.equals("order_change"))
                        {
                            intent.putExtra(PRODUCT_KEY, order);
                        }
                        intent.putExtra(NAVIGATION_FLAG, originFlag);
                    }
                    startActivity(intent);
                    CustomIntent.customType(MainActivity.this, "left-to-right");
                    finish();
                }
            }, SPLASH_SCREEN_DURATION);
        }

    }

    private boolean checkIfLoggedIn()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            userId = user.getUid();
            return true;
        }
        else
        {
            return false;
        }
    }

    private void checkAdmin(String id)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference mDatabase = database.getReference("Users");
        DatabaseReference userRef = mDatabase.child(id).getRef();
        userRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                User u = snapshot.getValue(User.class);
                if(u != null)
                {
                    boolean admin = u.isAdmin();
                    if(admin)
                    {
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        if(originFlag != null)
                        {
                            if(originFlag.equals("notification_added"))
                            {
                                intent.putExtra(PRODUCT_KEY, product);
                            }
                            else if(originFlag.equals("order_change"))
                            {
                                intent.putExtra(PRODUCT_KEY, order);
                            }
                            intent.putExtra(NAVIGATION_FLAG, originFlag);
                        }
                        intent.putExtra(ADMIN_KEY, true);
                        startActivity(intent);
                        CustomIntent.customType(MainActivity.this, "left-to-right");
                        finish();
                    }
                    else
                    {
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        if(originFlag != null)
                        {
                            if(originFlag.equals("notification_added"))
                            {
                                intent.putExtra(PRODUCT_KEY, product);
                            }
                            else if(originFlag.equals("order_change"))
                            {
                                intent.putExtra(PRODUCT_KEY, order);
                            }
                            intent.putExtra(NAVIGATION_FLAG, originFlag);
                        }
                        startActivity(intent);
                        CustomIntent.customType(MainActivity.this, "left-to-right");
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                CustomDialog dialog = new CustomDialog(MainActivity.this, "Network/Database error", "Failed to process the data. "
                        + error.getMessage(),
                        false);
                dialog.show();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            super.onBackPressed();
            return;
        }
        else
        {
            Toast.makeText(getBaseContext(), "Tap the back button again in order to exit.", Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    private void showCustomUI()
    {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}