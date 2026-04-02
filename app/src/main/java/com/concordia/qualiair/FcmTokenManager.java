package com.concordia.qualiair;

import android.content.Context;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FcmTokenManager {

    // Tag used to identify logs from this class in Logcat
    private static final String TAG = "FcmTokenManager";
    // Sends the phone's FCM token to the Python backend so it knows where to deliver notifications
    public static void sendTokenToBackend(Context context, String token) {
        // Build a Retrofit instance pointing to our Python backend
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-airquality.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // Create the ApiService using the Retrofit instance (connects the menu to the waiter)
        ApiService apiService = retrofit.create(ApiService.class);

        // Prepare the API call to send the token to the /register-token endpoint
        Call<Void> call = apiService.registerToken(token);
        // Execute the call in the background so it doesn't freeze the app
        call.enqueue(new Callback<Void>() {

            // Called when the backend responds (success or failure)
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Token sent successfully to backend");
                } else {
                    Log.e(TAG, "Failed to send token: " + response.code());
                }
            }
            // Called when the request couldn't reach the backend at all
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error sending token: " + t.getMessage());
            }
        });
    }

    // Sends the user's current threshold settings to the backend
// so background notifications respect the user's chosen sensitivity level
    public static void sendThresholdsToBackend(Context context, ThresholdLevels.Thresholds thresholds) {

        // Build Retrofit instance pointing to our Python backend
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-airquality.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create the ApiService using the Retrofit instance
        ApiService apiService = retrofit.create(ApiService.class);

        // Prepare the API call to send thresholds to /update-thresholds endpoint
        Call<Void> call = apiService.updateThresholds(thresholds);

        // Execute the call in the background so it doesn't freeze the app
        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Thresholds sent successfully to backend");
                } else {
                    Log.e(TAG, "Failed to send thresholds: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error sending thresholds: " + t.getMessage());
            }
        });
    }
}