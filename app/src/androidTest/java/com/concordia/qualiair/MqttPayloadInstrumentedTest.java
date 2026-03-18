package com.concordia.qualiair;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MqttPayloadInstrumentedTest {
    @Test
    public void testValidPayloadParsing() throws JSONException {
        String payload = "{\"device_id\":\"sensor1\",\"ammonia\":8.5,\"hydrogen_sulfide\":5.2,\"dust\":36.0}";
        JSONObject json = new JSONObject(payload);

        float nh3  = (float) json.getDouble("ammonia");
        float h2s  = (float) json.getDouble("hydrogen_sulfide");
        float pm25 = (float) json.getDouble("dust");

        assertEquals(8.5f,  nh3,  0.01f);
        assertEquals(5.2f,  h2s,  0.01f);
        assertEquals(36.0f, pm25, 0.01f);
    }

    @Test
    public void testMissingFieldThrowsException() {
        String payload = "{\"device_id\":\"sensor1\",\"ammonia\":8.5}";
        try {
            JSONObject json = new JSONObject(payload);
            json.getDouble("hydrogen_sulfide");
            fail("Should have thrown JSONException");
        } catch (JSONException e) {
            // expected
        }
    }
}
