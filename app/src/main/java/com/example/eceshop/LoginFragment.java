package com.example.eceshop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class LoginFragment extends Fragment
{

    private static final String SHARED_PREFS = "com.example.eceshop.loginPreferences";
    private static final String ADMIN_KEY = "com.example.eceshop.Admin";
    private static final String emailKey = "saved_email";
    private static final String passwordKey = "saved_password";
    private static final String markedKey = "rememberMe_status";
    private float v = 0;
    private EditText emailText;
    private EditText passwordText;
    private Button loginButton;
    private TextView forgotPassword;
    private CheckBox rememberMe;
    private Context ctx;
    private AlertDialog progressDialog;
    private ConstraintLayout loginContainer;
    private FirebaseAuth mAuth;

    private static final String PRODUCT_KEY = "com.example.eceshop.PRODUCT_VALUE";
    private static final String NAVIGATION_FLAG = "com.example.eceshop.NAVIGATION_KEY";
    private String origin;
    private Product product;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.login_fragment, container, false);
        emailText = root.findViewById(R.id.email_field_login);
        passwordText = root.findViewById(R.id.password_field_login);
        loginButton = root.findViewById(R.id.loginButton);
        forgotPassword = root.findViewById(R.id.forgotPass);
        rememberMe = root.findViewById(R.id.rememberMe);
        mAuth = FirebaseAuth.getInstance();
        loginContainer = root.findViewById(R.id.login_container);

        loginContainer.setOnTouchListener(new View.OnTouchListener()
        {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                InputMethodManager imm = (InputMethodManager) getActivityNonNull().getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = getActivityNonNull().getCurrentFocus();
                if (focusedView != null)
                {
                    imm.hideSoftInputFromWindow(getActivityNonNull().getCurrentFocus().getWindowToken(), 0);
                    if(emailText.isFocused())
                    {
                        emailText.clearFocus();
                    }
                    else if(passwordText.isFocused())
                    {
                        passwordText.clearFocus();
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        progressDialog = new SpotsDialog.Builder()
                .setContext(ctx)
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                emailText.setText("");
                passwordText.setText("");
                Intent intent = new Intent(getActivityNonNull(), ResetPasswordActivity.class);
                startActivity(intent);
                CustomIntent.customType(getActivityNonNull(), "left-to-right");
            }
        });

        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                SharedPreferences prefs = getActivityNonNull().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                if(isChecked)
                {
                    editor.putString(emailKey, emailText.getText().toString());
                    editor.putString(passwordKey, passwordText.getText().toString());
                    editor.putBoolean(markedKey, true);
                    editor.apply();
                }
                else
                {
                    editor.remove(emailKey);
                    editor.remove(passwordKey);
                    editor.remove(markedKey);
                    editor.apply();
                }
            }
        });

        emailText.setTranslationX(800);
        passwordText.setTranslationX(800);
        loginButton.setTranslationX(800);
        forgotPassword.setTranslationX(800);
        rememberMe.setTranslationX(800);

        emailText.setAlpha(v);
        passwordText.setAlpha(v);
        loginButton.setAlpha(v);
        forgotPassword.setAlpha(v);
        rememberMe.setAlpha(v);

        emailText.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        passwordText.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        forgotPassword.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        rememberMe.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        loginButton.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();

        origin = null;
        Bundle bundle = this.getArguments();
        if(bundle != null)
        {
            if(bundle.getString(NAVIGATION_FLAG) != null)
            {
                origin = bundle.getString(NAVIGATION_FLAG);
                product = bundle.getParcelable(PRODUCT_KEY);
            }
        }

        return root;
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
    public void onStart()
    {
        super.onStart();
        checkPreferences();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkPreferences();
    }

    private void checkPreferences()
    {
        SharedPreferences prefs = getActivityNonNull().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String em = prefs.getString(emailKey, "");
        String pass = prefs.getString(passwordKey, "");
        boolean status = prefs.getBoolean(markedKey, false);
        if(!em.equals(""))
        {
            emailText.setText(em);
        }
        if(!pass.equals(""))
        {
            passwordText.setText(pass);
        }
        if(status)
        {
            rememberMe.setChecked(status);
        }
        else
        {
            rememberMe.setChecked(status);
        }
    }

    private void loginUser()
    {
        String pEmail = emailText.getText().toString();
        String pPassword = passwordText.getText().toString();
        if(TextUtils.isEmpty(pEmail))
        {
            CustomDialog dialog = new CustomDialog(ctx, "Login error", "Please provide your email.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(pPassword))
        {
            CustomDialog dialog = new CustomDialog(ctx, "Login error", "Please provide your password.", false);
            dialog.show();
        }
        else if(!(Patterns.EMAIL_ADDRESS.matcher(pEmail).matches()))
        {
            CustomDialog dialog = new CustomDialog(ctx, "Login error", "Please provide a valid email.", false);
            dialog.show();
        }
        else
        {
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(pEmail, pPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user.isEmailVerified())
                        {
                            checkAdmin(user.getUid());
                        }
                        else
                        {
                            progressDialog.dismiss();
                            FirebaseAuth.getInstance().signOut();
                            passwordText.setText("");
                            user.sendEmailVerification();
                            CustomDialog dialog = new CustomDialog(ctx, "Login error", "Please verify your email before proceeding to login.",
                                    false);
                            dialog.show();
                        }
                    }
                    else
                    {
                        progressDialog.dismiss();
                        passwordText.setText("");
                        CustomDialog dialog = new CustomDialog(ctx, "Login error", "Failed to login. "
                                + Objects.requireNonNull(task.getException()).getMessage(),
                                false);
                        dialog.show();
                    }
                }
            });
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
                    progressDialog.dismiss();
                    if(admin)
                    {
                        Intent intent = new Intent(getActivityNonNull(), HomeActivity.class);
                        intent.putExtra(ADMIN_KEY, true);
                        if(origin != null)
                        {
                            intent.putExtra(PRODUCT_KEY, product);
                            intent.putExtra(NAVIGATION_FLAG, origin);
                        }
                        startActivity(intent);
                        CustomIntent.customType(getActivityNonNull(), "left-to-right");
                        getActivityNonNull().finish();
                    }
                    else
                    {
                        Intent intent = new Intent(getActivityNonNull(), HomeActivity.class);
                        if(origin != null)
                        {
                            intent.putExtra(PRODUCT_KEY, product);
                            intent.putExtra(NAVIGATION_FLAG, origin);
                        }
                        startActivity(intent);
                        CustomIntent.customType(getActivityNonNull(), "left-to-right");
                        getActivityNonNull().finish();
                    }
                }
                else
                {
                    FirebaseAuth.getInstance().signOut();
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(ctx, "Login error", "Failed to login. "
                            + "Could not retrieve the login data.",
                            false);
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                FirebaseAuth.getInstance().signOut();
                CustomDialog dialog = new CustomDialog(ctx, "Login error", "Failed to login. "
                        + error.getMessage(),
                        false);
                dialog.show();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        this.ctx = context;
    }

}
