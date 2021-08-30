package com.example.eceshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import maes.tech.intentanim.CustomIntent;

public class MainActivity extends AppCompatActivity {

    Animation topAnim;
    Animation bottomAnim;
    ImageView image;
    TextView title;
    TextView slogan;

    private static int SPLASH_SCREEN_DURATION = 3000;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        showCustomUI();
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

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    CustomIntent.customType(MainActivity.this, "left-to-right");
                    finish();
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

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, SigningActivity.class);
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
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void onBackPressed()
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

    private void showCustomUI()
    {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}