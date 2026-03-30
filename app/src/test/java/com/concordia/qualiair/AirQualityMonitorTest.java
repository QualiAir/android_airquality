package com.concordia.qualiair;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;
public class AirQualityMonitorTest {

    @Test
    public void getLatest_beforeUpdate_returnsZero() {
        assertEquals(0f, monitor.getLatest("nh3"), 0.0f);
        assertEquals(0f, monitor.getLatest("h2s"), 0.0f);
        assertEquals(0f, monitor.getLatest("pm25"), 0.0f);
    }

    @Test
    public void getLatest_afterUpdate_returnsCorrectValue() {
        monitor.update(10f, 2f, 50f);
        assertEquals(10f, monitor.getLatest("nh3"), 0.0f);
        assertEquals(2f,  monitor.getLatest("h2s"), 0.0f);
        assertEquals(50f, monitor.getLatest("pm25"), 0.0f);
    }

    private AirQualityMonitor monitor;
    @Before
    public void setUp() {

        monitor = new AirQualityMonitor();

    }

    @Test
    public void testInitialStatusIsGood() {
        Assert.assertEquals(AirQualityMonitor.StatusLevel.GOOD, monitor.getStatus("nh3"));
        Assert.assertFalse(monitor.isAnyCaution());
        Assert.assertFalse(monitor.isAnyAlarm());
    }

    @Test
    public void testNH3Classification() {
        // Default NH3: Caution 25, Alarm 35
        monitor.update(20f, 0f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.GOOD, monitor.getStatus("nh3"));

        monitor.update(26f, 0f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.CAUTION, monitor.getStatus("nh3"));

        monitor.update(40f, 0f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.ALARM, monitor.getStatus("nh3"));
    }
    @Test
    public void testNH3ExactBoundary() {
        monitor.update(25f, 0f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.CAUTION, monitor.getStatus("nh3"));

        monitor.update(35f, 0f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.ALARM, monitor.getStatus("nh3"));
    }

    @Test
    public void testH2SAndPM25Classification() {
        // H2S: Caution 1, Alarm 5
        monitor.update(0f, 2f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.CAUTION, monitor.getStatus("h2s"));

        // PM25: Caution 12, Alarm 35
        monitor.update(0f, 0f, 40f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.ALARM, monitor.getStatus("pm25"));
    }

    @Test
    public void testUpdateThresholds() {
        monitor.updateThresholds(ThresholdLevels.SENSITIVE);
        // Sensitive NH3 caution=10-> 15 should be caution
        monitor.update(15f, 0f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.CAUTION, monitor.getStatus("nh3"));
    }

    @Test
    public void testInvalidSensorName() {
        // Ensure it handles unknown strings gracefully
        Assert.assertEquals(AirQualityMonitor.StatusLevel.GOOD, monitor.getStatus("unknown"));
        Assert.assertEquals(0f, monitor.getLatest("unknown"), 0.001);
    }
    @Test
    public void testH2SExactBoundary() {
        monitor.update(0f, 1f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.CAUTION, monitor.getStatus("h2s"));

        monitor.update(0f, 5f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.ALARM, monitor.getStatus("h2s"));
    }

    @Test
    public void testPM25ExactBoundary() {
        monitor.update(0f, 0f, 12f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.CAUTION, monitor.getStatus("pm25"));

        monitor.update(0f, 0f, 35f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.ALARM, monitor.getStatus("pm25"));
    }
    //sensitive threshold level
    @Test
    public void testSensitivePresetBoundaries() {
        monitor.updateThresholds(ThresholdLevels.SENSITIVE);

        // NH3: caution=10, alarm=20
        monitor.update(10f, 0f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.CAUTION, monitor.getStatus("nh3"));
        monitor.update(20f, 0f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.ALARM, monitor.getStatus("nh3"));

        // H2S: caution=0.5, alarm=2
        monitor.update(0f, 0.5f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.CAUTION, monitor.getStatus("h2s"));
        monitor.update(0f, 2f, 0f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.ALARM, monitor.getStatus("h2s"));

        // PM25: caution=9, alarm=20
        monitor.update(0f, 0f, 9f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.CAUTION, monitor.getStatus("pm25"));
        monitor.update(0f, 0f, 20f);
        Assert.assertEquals(AirQualityMonitor.StatusLevel.ALARM, monitor.getStatus("pm25"));
    }

}
