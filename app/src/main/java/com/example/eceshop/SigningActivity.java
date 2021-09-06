package com.example.eceshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;


public class SigningActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton googleButton;
    private FloatingActionButton facebookButton;
    private float v = 0;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private AlertDialog progressDialog;
    private CallbackManager mCallbackManager;

    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;
    private static final String loginTitle = "Login";
    private static final String registerTitle = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);
        showCustomUI();
        //seed here
        //seedData();
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        googleButton = findViewById(R.id.googleButton);
        facebookButton = findViewById(R.id.facebookButton);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        progressDialog.show();
                        handleFacebookLogin(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                            facebookButton.setEnabled(true);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        facebookButton.setEnabled(true);
                        CustomDialog dialog = new CustomDialog(getApplicationContext(), "Facebook Sign-In error",
                                exception.getMessage(), false);
                        dialog.show();
                    }
                });

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        createRequest();

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doGoogleSignIn();
            }
        });

        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFacebookSignIn();
            }
        });

        tabLayout.addTab(tabLayout.newTab().setText(loginTitle));
        tabLayout.addTab(tabLayout.newTab().setText(registerTitle));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

        final SigningAdapter adapter = new SigningAdapter(getSupportFragmentManager(), this, tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setTranslationY(300);
        googleButton.setTranslationY(300);
        facebookButton.setTranslationY(300);
        googleButton.setAlpha(v);
        facebookButton.setAlpha(v);
        tabLayout.setAlpha(v);
        googleButton.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        facebookButton.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(600).start();
        tabLayout.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(100).start();
    }

    private void doGoogleSignIn()
    {
        googleButton.setEnabled(false);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void createRequest()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {
                progressDialog.show();
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            }
            catch (ApiException e)
            {
                googleButton.setEnabled(true);
                if(e.getStatusCode() != 12501)
                {
                    CustomDialog dialog = new CustomDialog(this, "Email Sign-In error", e.getMessage(), false);
                    dialog.show();
                    progressDialog.dismiss();
                }
                else
                {
                    progressDialog.dismiss();
                }
            }
        }
        else
        {
            if(mCallbackManager.onActivityResult(requestCode, resultCode, data))
            {
                return;
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken)
    {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            googleButton.setEnabled(true);
                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            if(isNew)
                            {
                                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                                String email = acct.getEmail();
                                String fullName = acct.getGivenName() + " " + acct.getFamilyName();
                                String phoneNumber = "N/A";
                                String country = "N/A";
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                String id = currentUser.getUid();
                                User user = new User(fullName, email, phoneNumber, country);
                                FirebaseDatabase database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
                                DatabaseReference mDatabase = database.getReference("Users");
                                mDatabase.child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                            startActivity(intent);
                                            CustomIntent.customType(SigningActivity.this, "left-to-right");
                                            finish();
                                        }
                                        else
                                        {
                                            progressDialog.dismiss();
                                            CustomDialog dialog = new CustomDialog(getApplicationContext(), "Email Sign-In error",
                                                    task.getException().getMessage(), false);
                                            dialog.show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                progressDialog.dismiss();
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                                CustomIntent.customType(SigningActivity.this, "left-to-right");
                                finish();
                            }
                        }
                        else {
                            googleButton.setEnabled(true);
                            progressDialog.dismiss();
                            CustomDialog dialog = new CustomDialog(getApplicationContext(), "Email Sign-In error",
                                    task.getException().getMessage(), false);
                            dialog.show();
                        }
                    }
                });
    }

    private void doFacebookSignIn()
    {
        facebookButton.setEnabled(false);
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    private void handleFacebookLogin(AccessToken token)
    {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    facebookButton.setEnabled(true);
                    FirebaseUser logged = mAuth.getCurrentUser();
                    boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                    if(isNewUser)
                    {
                        String email = logged.getEmail();
                        String fullName = logged.getDisplayName();
                        String phoneNumber = "N/A";
                        String country = "N/A";
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        String id = currentUser.getUid();
                        User user = new User(fullName, email, phoneNumber, country);
                        FirebaseDatabase database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
                        DatabaseReference mDatabase = database.getReference("Users");
                        mDatabase.child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    startActivity(intent);
                                    CustomIntent.customType(SigningActivity.this, "left-to-right");
                                    finish();
                                }
                                else
                                {
                                    progressDialog.dismiss();
                                    CustomDialog dialog = new CustomDialog(getApplicationContext(), "Facebook Sign-In error",
                                            task.getException().getMessage(), false);
                                    dialog.show();
                                }
                            }
                        });
                    }
                    else
                    {
                        progressDialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        CustomIntent.customType(SigningActivity.this, "left-to-right");
                        finish();
                    }
                }
                else
                {
                    facebookButton.setEnabled(true);
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(getApplicationContext(), "Facebook Sign-In error",
                           task.getException().getMessage(), false);
                    dialog.show();
                }
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
        else {

            Toast.makeText(getBaseContext(), "Tap the back button again in order to exit.", Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    private void showCustomUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void seedData()
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference mDatabase = db.getReference("Products");
        ProductDb item = new ProductDb("MacBook Pro M1", "The latest M1 based MacBook Pro.",
                "This is the long description.",
                "https://firebasestorage.googleapis.com/v0/b/ece-shop.appspot.com/o/product_images%2Fmacbook.jpg?alt=media&token=a04ca6e4-db88-4095-a307-258eddea43d0",
                600.0, 4, "Hardware", 8);
        mDatabase.push().setValue(item).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Log.e("Tadz", "Product is added.");
                }
                else
                {
                    Log.e("Tadz", Objects.requireNonNull(task.getException()).getMessage());
                }
            }

        });
    }

}