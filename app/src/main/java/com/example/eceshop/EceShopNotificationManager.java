package com.example.eceshop;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class EceShopNotificationManager
{

    private Context context;
    private NotificationManager notificationManager;
    public static final int NOTIFICATION_ID_REG = 789;
    public static final int NOTIFICATION_ID_IMG = 123;
    private static final String NOTIFICATION_CHANNEL_ID = "Main channel";

    public EceShopNotificationManager(Context context)
    {
        this.context = context;
        notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        setupNotificationChannel();
    }

    private void setupNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Ece shop notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Main notification channel of Ece shop.");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void showRegularNotification(String title, String body, Intent intent)
    {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID_REG, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        Notification notif = builder.setStyle(new NotificationCompat.BigTextStyle())
                .setSmallIcon(R.drawable.ic_notify)
                .setColor(context.getResources().getColor(R.color.yesColor))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.large_notify))
                .build();
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID_REG, notif);
    }

    public void showImageNotification(String title, String body, String src, Intent intent)
    {
        Glide.with(context).asBitmap().load(src).fitCenter().into(new CustomTarget<Bitmap>()
        {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition)
            {
                PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID_IMG, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
                Notification notif = builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(resource))
                        .setSmallIcon(R.drawable.ic_notify)
                        .setColor(context.getResources().getColor(R.color.yesColor))
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.large_notify))
                        .build();
                notif.flags |= Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(NOTIFICATION_ID_IMG, notif);
            }

             @Override
             public void onLoadCleared(@Nullable Drawable placeholder)
             {

             }
        });
    }

}
