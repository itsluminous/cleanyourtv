package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** widgets as ring gauges: the three percents a reading gives, and the arc each one draws. */
public class GaugeTest {

    @Test
    public void ramIsUsedOfTotalAndStorageUsedOfData() {
        MainActivity.Readings r = new MainActivity.Readings();
        r.mem = new long[]{620, 1000};
        r.totalBytes = 8_000_000_000L;
        r.freeBytes = 4_900_000_000L;
        assertEquals(62, r.memoryPercent());
        assertEquals(39, r.storagePercent());        // 3.1 GB used of 8
    }

    @Test
    public void theCpuRingIsBusyWhenReadableAndTheClocksShareOtherwise() {
        MainActivity.Readings r = new MainActivity.Readings();
        r.cpu = 8;
        r.cpuFreqPercent = 55;
        assertEquals(8, r.cpuGaugePercent());
        r.cpu = -1;
        assertEquals(55, r.cpuGaugePercent());       // Android 10 hides /proc/stat: scaling_cur_freq / cpuinfo_max_freq
        r.cpuFreqPercent = -1;
        assertEquals(-1, r.cpuGaugePercent());       // neither: the gauge hides
    }

    @Test
    public void noReadingIsMinusOneNotZero() {
        MainActivity.Readings r = new MainActivity.Readings();
        assertEquals(-1, r.memoryPercent());
        assertEquals(-1, r.storagePercent());
        assertEquals(-1, MainActivity.Readings.percentOf(5, 0));
        assertEquals(100, MainActivity.Readings.percentOf(7, 5));   // never past the ring
    }

    @Test
    public void theArcSweepsThreePointSixDegreesAPoint() {
        assertEquals(0f, GaugeView.sweepDegrees(0), 0f);
        assertEquals(223.2f, GaugeView.sweepDegrees(62), 0.01f);
        assertEquals(360f, GaugeView.sweepDegrees(100), 0f);
        assertEquals(360f, GaugeView.sweepDegrees(140), 0f);
    }

    @Test
    public void threeGaugesWithTheirGapsFitBeforeThePills() {
        float d = 2f;
        int width = 3 * GaugeView.widthPx(d) + 2 * Math.round(MainActivity.WIDGET_GAP_DP * d);
        assertEquals(312, width);                                    // 156dp at 1080p
        assertEquals(122, GaugeView.heightPx(d));                    // 61dp: inside the clock row's 65dp
        assertEquals(88, GaugeView.widthPx(d, 60f));                 // a short label: the ring's width
        assertEquals(104, GaugeView.widthPx(d, 102f));               // STORAGE is wider than the ring: the gauge grows to it
    }
}
