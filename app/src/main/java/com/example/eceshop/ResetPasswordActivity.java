package com.example.eceshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class ResetPasswordActivity extends AppCompatActivity
{
    private EditText emailField;
    private Button resetButton;
    private ConstraintLayout container;
    private AlertDialog progressDialog;
    private FirebaseAuth mAuth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        showCustomUI();
        emailField = findViewById(R.id.reset_email);
        resetButton = findViewById(R.id.resetPassButton);
        container = findViewById(R.id.resetContainer);

        mAuth = FirebaseAuth.getInstance();

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = getCurrentFocus();
                if (focusedView != null)
                {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    if(emailField.isFocused())
                    {
                        emailField.clearFocus();
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doResetPass();
            }
        });

        progressDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();
    }

    private void doResetPass()
    {
        String email = emailField.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            CustomDialog dialog = new CustomDialog(ResetPasswordActivity.this, "Reset password error",
                    "Please provide your email.", false);
            dialog.show();
        }
        else if(!(Patterns.EMAIL_ADDRESS.matcher(email).matches()))
        {
            CustomDialog dialog = new CustomDialog(ResetPasswordActivity.this,
                    "Reset password error", "Please provide a valid email.", false);
            dialog.show();
        }
        else
        {
            progressDialog.show();
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        progressDialog.dismiss();
                        emailField.setText("");
                        CustomDialog dialog = new CustomDialog(ResetPasswordActivity.this,
                                "Success", "A password reset link has been sent to the provided email address.", true);
                        dialog.show();
                    }
                    else
                    {
                        progressDialog.dismiss();
                        CustomDialog dialog = new CustomDialog(ResetPasswordActivity.this,
                                "Reset password error", Objects.requireNonNull(task.getException()).getMessage(), false);
                        dialog.show();
                    }
                }
            });
        }
    }

    private void showCustomUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onStop() {
        super.onStop();
        emailField.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        emailField.setText("");
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(ResetPasswordActivity.this, "right-to-left");
    }

}