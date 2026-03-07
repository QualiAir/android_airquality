package com.concordia.qualiair;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface ApiService {
    //rest api call, retrofit to all, (from main.py file on android_backend)
    @GET("history")
    Call<HistoryResponse> getHistory(
        @Query("range") String range,
        @Query("sensor") String sensor,
        @Query("device_id") String deviceId
    );

}
