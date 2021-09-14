package com.example.eceshop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class RegisterFragment extends Fragment
{

    private EditText name;
    private EditText email;
    private EditText password;
    private EditText confirmPass;
    private EditText phone;
    private CountryCodePicker countryCode;
    private Button registerButton;
    private Context ctx;
    private AlertDialog progressDialog;
    private ConstraintLayout registerContainer;
    private FirebaseAuth mAuth;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.register_fragment, container, false);

        name = root.findViewById(R.id.name_field);
        email = root.findViewById(R.id.email_field);
        password = root.findViewById(R.id.password_field);
        confirmPass = root.findViewById(R.id.confirm_password_field);
        phone = root.findViewById(R.id.phone_input);
        countryCode = root.findViewById(R.id.countryCode);
        registerButton = root.findViewById(R.id.registerButton);
        mAuth = FirebaseAuth.getInstance();

        registerContainer = root.findViewById(R.id.register_container);

        registerContainer.setOnTouchListener(new View.OnTouchListener()
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
                    if(name.isFocused())
                    {
                        name.clearFocus();
                    }
                    else if(email.isFocused())
                    {
                        email.clearFocus();
                    }
                    else if(password.isFocused())
                    {
                        password.clearFocus();
                    }
                    else if(confirmPass.isFocused())
                    {
                        confirmPass.clearFocus();
                    }
                    else if(phone.isFocused())
                    {
                        phone.clearFocus();
                    }
                    else if(countryCode.isFocused())
                    {
                        countryCode.clearFocus();
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

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                registerAccount();
            }
        });

        return root;
    }

    private void registerAccount()
    {
        String pName = name.getText().toString();
        String pEmail = email.getText().toString();
        String pPass = password.getText().toString();
        String pConfirmPass = confirmPass.getText().toString();
        String phoneNumber = countryCode.getSelectedCountryCode() + "-" + phone.getText().toString();
        String countryName = countryCode.getSelectedCountryName();
        if(TextUtils.isEmpty(pName))
        {
            CustomDialog dialog = new CustomDialog(ctx, "Register error", "Please provide your full name.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(pEmail))
        {
            CustomDialog dialog = new CustomDialog(ctx, "Register error", "Please provide your email.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(pPass))
        {
            CustomDialog dialog = new CustomDialog(ctx, "Register error", "Please provide your password.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(pConfirmPass))
        {
            CustomDialog dialog = new CustomDialog(ctx, "Register error", "Please confirm your password.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(phone.getText().toString()))
        {
            CustomDialog dialog = new CustomDialog(ctx, "Register error", "Please provide your phone number.", false);
            dialog.show();
        }
        else if((!PhoneNumberUtils.isGlobalPhoneNumber(countryCode.getSelectedCountryCodeWithPlus() + phone.getText().toString())) || phone.getText().toString().length() != 8)
        {
            CustomDialog dialog = new CustomDialog(ctx, "Register error", "Please provide a valid phone number.", false);
            dialog.show();
        }
        else if(!(pPass.equals(pConfirmPass)))
        {
            CustomDialog dialog = new CustomDialog(ctx, "Register error", "The passwords do not match.", false);
            dialog.show();
        }
        else if(pPass.length() < 9)
        {
            CustomDialog dialog = new CustomDialog(ctx, "Register error", "Your password is too short. The minimum length is 9 characters.", false);
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
            mAuth.createUserWithEmailAndPassword(pEmail, pPass).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        User user = new User(pName, pEmail, phoneNumber, countryName, false);
                        FirebaseUser logged = FirebaseAuth.getInstance().getCurrentUser();
                        FirebaseDatabase database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
                        DatabaseReference mDatabase = database.getReference("Users");
                        mDatabase.child(logged.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(pName).build();
                                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            progressDialog.dismiss();
                                            email.setText("");
                                            password.setText("");
                                            confirmPass.setText("");
                                            phone.setText("");
                                            name.setText("");
                                            FirebaseAuth.getInstance().signOut();
                                            CustomDialog dialog = new CustomDialog(ctx, "Successful registration", "Your account has been created.",
                                                    true);
                                            dialog.show();
                                        }
                                    });
                                }
                                else
                                {
                                    progressDialog.dismiss();
                                    CustomDialog dialog = new CustomDialog(ctx, "Register error", "Failed to register your account. "
                                            + Objects.requireNonNull(task.getException()).getMessage(),
                                            false);
                                    dialog.show();
                                }
                            }
                        });
                    }
                    else
                    {
                        Log.e("TaskRegister", "Outside error is " + task.getException().getMessage());
                        progressDialog.dismiss();
                        CustomDialog dialog = new CustomDialog(ctx, "Register error", "Failed to register your account. "
                                + Objects.requireNonNull(task.getException()).getMessage(),
                                false);
                        dialog.show();
                    }
                }
            });
        }
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

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        this.ctx = context;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        email.setText("");
        password.setText("");
        confirmPass.setText("");
        phone.setText("");
        name.setText("");
    }

}
