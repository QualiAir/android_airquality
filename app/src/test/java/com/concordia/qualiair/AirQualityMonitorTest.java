package com.concordia.qualiair;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
public class AirQualityMonitorTest {

    @Test
    public void getLatest_beforeUpdate_returnsZero() {
        AirQualityMonitor monitor = new AirQualityMonitor();
        assertEquals(0f, monitor.getLatest("nh3"), 0.0f);
        assertEquals(0f, monitor.getLatest("h2s"), 0.0f);
        assertEquals(0f, monitor.getLatest("pm25"), 0.0f);
    }

    @Test
    public void getLatest_afterUpdate_returnsCorrectValue() {
        AirQualityMonitor monitor = new AirQualityMonitor();
        monitor.update(10f, 2f, 50f);
        assertEquals(10f, monitor.getLatest("nh3"), 0.0f);
        assertEquals(2f,  monitor.getLatest("h2s"), 0.0f);
        assertEquals(50f, monitor.getLatest("pm25"), 0.0f);
    }


}
