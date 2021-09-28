package com.example.eceshop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ProfileFragment extends Fragment
{

    private LinearLayout profileContainer;

    private TextInputLayout oldPasswordLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;

    private TextView displayNameTextView;
    private TextView displayEmailTextView;
    private TextView totalOrdersTextView;
    private TextView totalMoneyTextView;
    private TextInputEditText fullNameEditText;
    private TextInputEditText oldPasswordEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private CountryCodePicker countryCodePicker;
    private TextInputEditText phoneEditText;
    private AppCompatButton updateButton;

    private static final String PASSWORD_PROVIDER = "password";

    private ProfileFragmentTouchListener listener;
    private AlertDialog progressDialog;

    private DatabaseReference usersRef;
    private String userId;
    private User model;
    private String providerId;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.profile_fragment_layout, container, false);

        profileContainer = root.findViewById(R.id.profile_container);

        oldPasswordLayout = root.findViewById(R.id.oldPasswordLayout);
        passwordLayout = root.findViewById(R.id.passwordLayout);
        confirmPasswordLayout = root.findViewById(R.id.confirmPasswordLayout);

        displayNameTextView = root.findViewById(R.id.profile_displayName);
        displayEmailTextView = root.findViewById(R.id.profile_email);
        totalOrdersTextView = root.findViewById(R.id.orders_label);
        totalMoneyTextView = root.findViewById(R.id.money_label);
        fullNameEditText = root.findViewById(R.id.fullName_input);
        oldPasswordEditText = root.findViewById(R.id.oldPassword_input);
        passwordEditText = root.findViewById(R.id.password_input);
        confirmPasswordEditText = root.findViewById(R.id.confirmPassword_input);
        countryCodePicker = root.findViewById(R.id.profileCountryCode);
        phoneEditText = root.findViewById(R.id.profilePhoneInput);
        updateButton = root.findViewById(R.id.updateProfileButton);

        progressDialog = new SpotsDialog.Builder()
                .setContext(getActivityNonNull())
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        usersRef = database.getReference("Users");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

        profileContainer.setOnTouchListener(new View.OnTouchListener()
        {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                listener.onProfileFragmentTouch();
                InputMethodManager imm = (InputMethodManager) getActivityNonNull().getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = getActivityNonNull().getCurrentFocus();
                if (focusedView != null)
                {
                    imm.hideSoftInputFromWindow(getActivityNonNull().getCurrentFocus().getWindowToken(), 0);
                    if(fullNameEditText.isFocused())
                    {
                        fullNameEditText.clearFocus();
                    }
                    else if(passwordEditText.isFocused())
                    {
                        passwordEditText.clearFocus();
                    }
                    else if(confirmPasswordEditText.isFocused())
                    {
                        confirmPasswordEditText.clearFocus();
                    }
                    else if(countryCodePicker.isFocused())
                    {
                        countryCodePicker.clearFocus();
                    }
                    else if(phoneEditText.isFocused())
                    {
                        phoneEditText.clearFocus();
                    }
                    else if(oldPasswordEditText.isFocused())
                    {
                        oldPasswordEditText.clearFocus();
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_NEXT)
                {
                    View focusedView = getActivityNonNull().getCurrentFocus();
                    if (focusedView != null)
                    {
                        passwordEditText.clearFocus();
                        confirmPasswordEditText.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        confirmPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    InputMethodManager imm = (InputMethodManager) getActivityNonNull().getSystemService(Context.INPUT_METHOD_SERVICE);
                    View focusedView = getActivityNonNull().getCurrentFocus();
                    if (focusedView != null)
                    {
                        imm.hideSoftInputFromWindow(getActivityNonNull().getCurrentFocus().getWindowToken(), 0);
                        confirmPasswordEditText.clearFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               updateProfile();
            }
        });

        getUser();

        return root;
    }

    private void getUser()
    {
        progressDialog.show();
        usersRef.child(userId).getRef().addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    model = snapshot.getValue(User.class);
                    if(model != null)
                    {
                        displayEmailTextView.setText(model.getEmail());
                        displayNameTextView.setText(model.getFullName());
                        fullNameEditText.setText(model.getFullName());
                        String number = model.getPhoneNumber();
                        if(!number.equals("N/A"))
                        {
                            String[] numArr = number.split("-");
                            countryCodePicker.setCountryForPhoneCode(Integer.parseInt(numArr[0]));
                            phoneEditText.setText(numArr[1]);
                        }
                        getProviderId();
                    }
                    else
                    {
                        progressDialog.dismiss();
                        String ordersData = "Total orders: N/A";
                        String moneyData = "Total amount: N/A";
                        totalOrdersTextView.setText(ordersData);
                        totalMoneyTextView.setText(moneyData);
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", "Could not obtain the user data.", false);
                        dialog.show();
                    }
                }
                else
                {
                    progressDialog.dismiss();
                    String ordersData = "Total orders: N/A";
                    String moneyData = "Total amount: N/A";
                    totalOrdersTextView.setText(ordersData);
                    totalMoneyTextView.setText(moneyData);
                    CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", "Could not obtain the user data.", false);
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressDialog.dismiss();
                String ordersData = "Total orders: N/A";
                String moneyData = "Total amount: N/A";
                totalOrdersTextView.setText(ordersData);
                totalMoneyTextView.setText(moneyData);
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                dialog.show();
            }
        });
    }

    private void getProviderId()
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        UserInfo providerData = mAuth.getCurrentUser().getProviderData().get(1);
        providerId = providerData.getProviderId();
        if(!providerId.equals(PASSWORD_PROVIDER))
        {
           oldPasswordLayout.setVisibility(View.GONE);
           passwordLayout.setVisibility(View.GONE);
           confirmPasswordLayout.setVisibility(View.GONE);
           phoneEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
           phoneEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
           {
               @Override
               public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
               {
                   if (actionId == EditorInfo.IME_ACTION_DONE)
                   {
                       InputMethodManager imm = (InputMethodManager) getActivityNonNull().getSystemService(Context.INPUT_METHOD_SERVICE);
                       View focusedView = getActivityNonNull().getCurrentFocus();
                       if (focusedView != null)
                       {
                           imm.hideSoftInputFromWindow(getActivityNonNull().getCurrentFocus().getWindowToken(), 0);
                           phoneEditText.clearFocus();
                       }
                       return true;
                   }
                   return false;
               }
           });
        }

        getOrdersInfo();
    }

    private void getOrdersInfo()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference ref = database.getReference("Orders");
        DatabaseReference childRef = ref.child(userId).getRef();
        childRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    int count = (int)snapshot.getChildrenCount();
                    double total = 0.0d;
                    for(DataSnapshot snap : snapshot.getChildren())
                    {
                        OrderDb o = snap.getValue(OrderDb.class);
                        if(o != null)
                        {
                            Order order = createOrder(snap.getKey(), o);
                            if(!order.getStatus().equals("Cancelled"))
                            {
                                total = total + order.calculateTotalPrice();
                            }
                        }
                    }
                    String ordersData = "Total orders: " + String.valueOf(count);
                    String moneyData = "Total amount: " + String.valueOf(total) + "$";
                    totalOrdersTextView.setText(ordersData);
                    totalMoneyTextView.setText(moneyData);
                    progressDialog.dismiss();
                }
                else
                {
                    progressDialog.dismiss();
                    String ordersData = "Total orders: 0";
                    String moneyData = "Total amount: 0$";
                    totalOrdersTextView.setText(ordersData);
                    totalMoneyTextView.setText(moneyData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressDialog.dismiss();
                String ordersData = "Total orders: N/A";
                String moneyData = "Total amount: N/A";
                totalOrdersTextView.setText(ordersData);
                totalMoneyTextView.setText(moneyData);
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", error.getMessage(), false);
                dialog.show();
            }
        });
    }

    private Order createOrder(String id, OrderDb item)
    {
        Map<String, OrderProductContentDb> map = item.getProducts();
        List<String> keys = new ArrayList<>();
        for(String key : map.keySet())
        {
            keys.add(key);
        }
        List<OrderProductContent> productsList = new ArrayList<>();
        for(String k : keys)
        {
            OrderProductContentDb value = map.get(k);
            OrderProductContent content = new OrderProductContent(k, value.getPrice(), value.getQuantity());
            productsList.add(content);
        }
        Order retVal= new Order(id, item.getAddress(), item.getPaymentId(), item.getPaymentMethodId(), productsList, item.getStatus(), item.getTimestamp());
        return retVal;
    }

    private void updateProfile()
    {
        if(providerId.equals(PASSWORD_PROVIDER))
        {
            String oldPass = oldPasswordEditText.getText().toString();
            String pass = passwordEditText.getText().toString();
            String confirmPass = confirmPasswordEditText.getText().toString();
            if(TextUtils.isEmpty(oldPass) && TextUtils.isEmpty(pass) && TextUtils.isEmpty(confirmPass))
            {
                updateProfileWithoutPassword();
            }
            else if(TextUtils.isEmpty(oldPass))
            {
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide your old password.", false);
                dialog.show();
            }
            else if(TextUtils.isEmpty(pass))
            {
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide your new password.", false);
                dialog.show();
            }
            else if(TextUtils.isEmpty(confirmPass))
            {
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please confirm your password.", false);
                dialog.show();
            }
            else if(pass.length() < 9)
            {
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Your password is too short. The minimum length is 9 characters.", false);
                dialog.show();
            }
            else if(!pass.equals(confirmPass))
            {
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Your passwords do not match.", false);
                dialog.show();
            }
            else if(oldPass.equals(pass))
            {
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "You need to provide a different new password in order to change your existing password.", false);
                dialog.show();
            }
            else
            {
                String name = fullNameEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String countryCode = countryCodePicker.getSelectedCountryCode();
                String fullNumber = model.getPhoneNumber();
                if(!fullNumber.equals("N/A"))
                {
                    if(TextUtils.isEmpty(name))
                    {
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Your provide your name.", false);
                        dialog.show();
                    }
                    else if(TextUtils.isEmpty(phone))
                    {
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Your provide your phone number.", false);
                        dialog.show();
                    }
                    else if((!PhoneNumberUtils.isGlobalPhoneNumber(countryCode + phone)) || phone.length() != 8)
                    {
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide a valid phone number.", false);
                        dialog.show();
                    }
                    else
                    {
                        updateProfileWithPassword();
                    }
                }
                else
                {
                    if(TextUtils.isEmpty(name))
                    {
                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Your provide your name.", false);
                        dialog.show();
                    }
                    else if(TextUtils.isEmpty(phone))
                    {
                        updateProfileWithPassword();
                    }
                    else
                    {
                        if((!PhoneNumberUtils.isGlobalPhoneNumber(countryCode + phone)) || phone.length() != 8)
                        {
                            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide a valid phone number.", false);
                            dialog.show();
                        }
                        else
                        {
                            updateProfileWithPassword();
                        }
                    }
                }
            }
        }
        else
        {
            updateProfileWithoutPassword();
        }
    }

    private void updateProfileWithoutPassword()
    {
        String name = fullNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String countryName = countryCodePicker.getSelectedCountryName();
        String countryCode = countryCodePicker.getSelectedCountryCode();
        String code = "";
        String number = "";
        String fullNumber = model.getPhoneNumber();
        if(!fullNumber.equals("N/A"))
        {
            String[] numArr = fullNumber.split("-");
            code = numArr[0];
            number = numArr[1];
        }
        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(phone))
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide your full name and/or phone number.", false);
            dialog.show();
        }
        else if(name.equals(model.getFullName()) && number.equals("") && code.equals(""))
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Cannot update your profile as no changes have been made.", false);
            dialog.show();
        }
        else if(name.equals(model.getFullName()) && number.equals(phone) && code.equals(countryCode))
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Cannot update your profile as no changes have been made.", false);
            dialog.show();
        }
        else if((!PhoneNumberUtils.isGlobalPhoneNumber(countryCode + phone)) || phone.length() != 8)
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide a valid phone number.", false);
            dialog.show();
        }
        else
        {
            final HashMap<String, Object> updatesMap = new HashMap<>();
            model.setCountry(countryName);
            model.setFullName(name);
            String newPhone = countryCode + "-" + phone;
            model.setPhoneNumber(newPhone);
            updatesMap.put(userId, model);
            progressDialog.show();
            usersRef.updateChildren(updatesMap).addOnSuccessListener(new OnSuccessListener<Void>()
            {
                @Override
                public void onSuccess(Void aVoid)
                {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                displayNameTextView.setText(name);
                                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Successful updates", "Your profile has been updated.", true);
                                dialog.show();
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database error", e.getMessage(), false);
                    dialog.show();
                }
            });
        }
    }

    private void updateProfileWithPassword()
    {
        String oldPass = oldPasswordEditText.getText().toString();
        String pass = passwordEditText.getText().toString();
        String name = fullNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String countryName = countryCodePicker.getSelectedCountryName();
        String countryCode = countryCodePicker.getSelectedCountryCode();
        String fullPhone = countryCode + "-" + phone;

        progressDialog.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(model.getEmail(), oldPass);
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            user.updatePassword(pass).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        final HashMap<String, Object> updatesMap = new HashMap<>();
                                        model.setCountry(countryName);
                                        model.setFullName(name);
                                        model.setPhoneNumber(fullPhone);
                                        updatesMap.put(userId, model);
                                        usersRef.updateChildren(updatesMap).addOnSuccessListener(new OnSuccessListener<Void>()
                                        {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {
                                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(name).build();
                                                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {
                                                            progressDialog.dismiss();
                                                            displayNameTextView.setText(name);
                                                            oldPasswordEditText.setText("");
                                                            passwordEditText.setText("");
                                                            confirmPasswordEditText.setText("");
                                                            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Successful updates", "Your profile has been updated.", true);
                                                            dialog.show();
                                                        }
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                progressDialog.dismiss();
                                                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database error", e.getMessage(), false);
                                                dialog.show();
                                            }
                                        });
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Database/Network error", task.getException().getMessage(), false);
                                        dialog.show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            progressDialog.dismiss();
                            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Authentication error", "Failed to authenticate the profile with the provided password.", false);
                            dialog.show();
                        }
                    }
                });
    }

    public interface ProfileFragmentTouchListener
    {
        void onProfileFragmentTouch();
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
        if(context instanceof ProfileFragmentTouchListener)
        {
            listener = (ProfileFragmentTouchListener) context;
        }
        else
        {
            throw  new RuntimeException(context.toString() + " must implement the ProfileFragmentTouchListener.");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }

}
