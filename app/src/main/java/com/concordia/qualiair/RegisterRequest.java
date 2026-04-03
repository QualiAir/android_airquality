package com.concordia.qualiair;

public class RegisterRequest {
    String device_id;
    String token;

    public RegisterRequest(String device_id, String token) {
        this.device_id = device_id;
        this.token = token;
    }
}
