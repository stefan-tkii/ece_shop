package com.example.eceshop;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager
{

    private static final String SHARED_PREF_NAME = "com.example.eceshop.TOKEN_DB";
    private static final String KEY_ACCESS_TOKEN = "FCM_TOKEN";

    private static Context ctx;
    private static SharedPreferenceManager mInstance;

    private SharedPreferenceManager(Context context)
    {
        ctx = context;
    }

    public static synchronized SharedPreferenceManager getInstance(Context context)
    {
        if(mInstance == null)
        {
            mInstance = new SharedPreferenceManager(context);
        }
        return mInstance;
    }

    public boolean storeToken(String token)
    {
        SharedPreferences preferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
        return true;
    }

    public String getToken()
    {
        SharedPreferences preferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_ACCESS_TOKEN, null);
    }

}
