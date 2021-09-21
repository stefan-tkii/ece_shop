package com.example.eceshop;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Base64;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MessagingApiManager
{

    private static final String CONTENT_TYPE = "application/json";
    private static final String ADMIN_ID = "Admin";
    private static final String ADMIN_PASS = "94574549873d9b2a51ff7f4cd009f6d4257d7cfa9910023db546dbc0b872545f12";
    private static final String API_URL = "http://192.168.1.105:8080/firebasemessaging/webapi/";
    private Retrofit retrofit;
    private MessagingApi apiCallManager;

    public MessagingApiManager()
    {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        apiCallManager = retrofit.create(MessagingApi.class);
    }

    public void sendWelcomeRequest(String token)
    {
        String authValue = ADMIN_ID + ":" + ADMIN_PASS;
        String authHead = "Basic " + Base64.getEncoder().encodeToString(authValue.getBytes());
        Call<String> callRequest = apiCallManager.sendWelcome(CONTENT_TYPE, authHead, token);
        callRequest.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if(!response.isSuccessful())
                {
                    Log.e("Api Request Failure", response.code() + response.body());
                }
                else
                {
                    Log.e("Api Request Successful", response.code() + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e("Api Error", t.getMessage());
            }
        });
    }

    public void sendInformNewRequest(Product product)
    {
        String authValue = ADMIN_ID + ":" + ADMIN_PASS;
        String authHead = "Basic " + Base64.getEncoder().encodeToString(authValue.getBytes());
        Call<String> callRequest = apiCallManager.sendInformNew(CONTENT_TYPE, authHead, product);
        callRequest.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if(!response.isSuccessful())
                {
                    Log.e("Api Request Failure", response.code() + response.body());
                }
                else
                {
                    Log.e("Api Request Successful", response.code() + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e("Api Error", t.getMessage());
            }
        });
    }

}
