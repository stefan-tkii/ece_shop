package com.example.eceshop;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EceShopMessagingService extends FirebaseMessagingService
{

    public static final String TOKEN_BROADCAST = "com.example.eceshop.TOKEN_GENERATED";

    @Override
    public void onNewToken(@NonNull String s)
    {
        super.onNewToken(s);
        getApplicationContext().sendBroadcast(new Intent(TOKEN_BROADCAST));
        saveToken(s);
        subscribeToProductsTopic();
    }

    private void subscribeToProductsTopic()
    {
        FirebaseMessaging.getInstance().subscribeToTopic("products").addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Log.e("Subscription success","Subscribed to products topic.");
                }
                else
                {
                    Log.e("Subscription error", task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getNotification() != null)
        {
            Map<String, String> data = remoteMessage.getData();
            String type = data.get("type");
            if(type.equals("regular"))
            {
                notifyUserWithRegular(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
            }
            else if(type.equals("image"))
            {
                String productJson = data.get("object");
                Product p = Product.fromJson(productJson);
                notifyUserWithImage(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), p);
            }
        }
    }

    public void notifyUserWithRegular(String title, String body)
    {
        EceShopNotificationManager manager = new EceShopNotificationManager(getApplicationContext());
        manager.showRegularNotification(title, body, new Intent());
    }

    public void notifyUserWithImage(String title, String body, Product product)
    {
        EceShopNotificationManager manager = new EceShopNotificationManager(getApplicationContext());
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("object", product);
        intent.putExtra("origin", "notification_added");
        manager.showImageNotification(title, body, product.getImgUri(), intent);
    }

    public void saveToken(String token)
    {
        SharedPreferenceManager.getInstance(getApplicationContext()).storeToken(token);
    }

}
