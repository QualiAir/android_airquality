package com.concordia.qualiair;

import android.content.Context;
import android.util.Log;

import com.concordia.qualiair.Device.Device;
import com.concordia.qualiair.Device.DeviceList;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FcmTokenManager {

    private static final String TAG = "FcmTokenManager";
    private static final String BASE_URL = "https://backend-airquality.onrender.com/";

    /**
     * Uses the existing DeviceList class to find a registered device ID.
     * Returns the ID of the first device found in the system.
     */
    private static String getRegisteredDeviceId(Context context) {
        DeviceList deviceList = new DeviceList(context);
        List<Device> devices = deviceList.getAllDevices();
        if (devices != null && !devices.isEmpty()) {
            // Your teammate's Device class has this method to get the ID
            return devices.get(0).getDeviceIDESP32();
        }
        return null;
    }

    public static void sendTokenToBackend(Context context, String token) {
        String deviceId = getRegisteredDeviceId(context);

        if (deviceId == null) {
            Log.w(TAG, "No devices found via DeviceList. Saving token for future sync.");
            context.getSharedPreferences("QualiAirPreferences", Context.MODE_PRIVATE)
                    .edit()
                    .putString("fcm_token", token)
                    .apply();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Void> call = apiService.registerToken(new RegisterRequest(deviceId, token));

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM Token successfully associated with device: " + deviceId);
                } else {
                    Log.e(TAG, "Backend rejected token sync. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network failure during token sync: " + t.getMessage());
            }
        });
    }

    public static void syncSavedTokenWithBackend(Context context) {
        String savedToken = context.getSharedPreferences("QualiAirPreferences", Context.MODE_PRIVATE)
                .getString("fcm_token", null);

        if (savedToken != null && !savedToken.isEmpty()) {
            Log.d(TAG, "Syncing saved token with now-registered device...");
            sendTokenToBackend(context, savedToken);
        }
    }

    public static void sendThresholdsToBackend(Context context, ThresholdLevels.Thresholds thresholds) {
        String deviceId = getRegisteredDeviceId(context);

        if (deviceId == null) {
            Log.e(TAG, "Cannot sync thresholds: No device found in DeviceList.");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Void> call = apiService.updateThresholds(new ThresholdUpdateRequest(deviceId, thresholds));

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) Log.d(TAG, "Thresholds updated on backend.");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to update thresholds on backend: " + t.getMessage());
            }
        });
    }
}
