package com.example.eceshop;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

@GlideModule
public final class EceShopGlideModule extends AppGlideModule
{

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder)
    {
        super.applyOptions(context, builder);

        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new ObjectKey(
                        System.currentTimeMillis() / (24 * 60 * 60 * 1000)));

        builder.setDefaultRequestOptions(requestOptions);
    }

}
