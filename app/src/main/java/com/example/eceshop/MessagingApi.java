package com.example.eceshop;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MessagingApi
{

    @POST("messaging/sendwelcome")
    @Headers({"Accept: application/json"})
    Call<String> sendWelcome(@Header("Content-Type") String content_type, @Header("Authorization") String authHeader, @Query("key") String registerToken);

    @POST("messaging/informnew")
    @Headers({"Accept: application/json"})
    Call<String> sendInformNew(@Header("Content-Type") String content_type, @Header("Authorization") String authHeader, @Body Product product);

    @POST("messaging/informupdate")
    @Headers({"Accept: application/json"})
    Call<String> sendInformUpdate(@Header("Content-Type") String content_type, @Header("Authorization") String authHeader, @Query("status") String status,
                                  @Body Product product);

    @POST("messaging/sendthanks")
    @Headers({"Accept: application/json"})
    Call<String> sendThanks(@Header("Content-Type") String content_type, @Header("Authorization") String authHeader, @Query("id") String orderId);

    @POST("messaging/orderchange")
    @Headers({"Accept: application/json"})
    Call<String> sendOrderStatusChange(@Header("Content-Type") String content_type, @Header("Authorization") String authHeader, @Body Order order);

}
