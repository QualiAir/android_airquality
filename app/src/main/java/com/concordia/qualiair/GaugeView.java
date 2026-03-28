package com.concordia.qualiair;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.content.res.TypedArray;

public class GaugeView extends View {

    private Paint arcPaint;
    private Paint needlePaint;
    private Paint bgArcPaint;
    private RectF arcRect;

    private float minValue = 0;
    private float maxValue = 50;
    private float currentValue = 0;
    private float cautionThreshold = 25f;
    private float alarmThreshold   = 35f;
    private float strokeWidth      = 60f;


    private int colorLow     = Color.parseColor("#00D4AA");
    private int colorCaution = Color.parseColor("#FFB347");
    private int colorAlarm   = Color.parseColor("#FF4D4D");
    private int colorBg      = Color.parseColor("#1F2937");
    private int colorNeedle  = Color.WHITE;

    public GaugeView(Context context) {
        super(context);
        init(null);
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GaugeView);
            try {
                minValue         = a.getFloat(R.styleable.GaugeView_gaugeMinValue,      minValue);
                maxValue         = a.getFloat(R.styleable.GaugeView_gaugeMaxValue,      maxValue);
                cautionThreshold = a.getFloat(R.styleable.GaugeView_gaugeCautionValue,  cautionThreshold);
                alarmThreshold   = a.getFloat(R.styleable.GaugeView_gaugeAlarmValue,    alarmThreshold);
                strokeWidth      = a.getDimension(R.styleable.GaugeView_gaugeStrokeWidth, strokeWidth);
                colorLow         = a.getColor(R.styleable.GaugeView_gaugeColorLow,      colorLow);
                colorCaution     = a.getColor(R.styleable.GaugeView_gaugeColorCaution,  colorCaution);
                colorAlarm       = a.getColor(R.styleable.GaugeView_gaugeColorAlarm,    colorAlarm);
                colorBg          = a.getColor(R.styleable.GaugeView_gaugeColorBg,       colorBg);
                colorNeedle      = a.getColor(R.styleable.GaugeView_gaugeColorNeedle,   colorNeedle);
            } finally {
                a.recycle();
            }
        }
        setupPaints();
    }
    private void setupPaints() {
        bgArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgArcPaint.setStyle(Paint.Style.STROKE);
        bgArcPaint.setStrokeWidth(strokeWidth);
        bgArcPaint.setColor(colorBg);
        bgArcPaint.setStrokeCap(Paint.Cap.ROUND);

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(strokeWidth);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setColor(Color.WHITE);
        needlePaint.setStrokeWidth(6f);

        arcRect = new RectF();
    }

    public void applyPreset(ThresholdLevels.Thresholds t, String gasType) {
        switch (gasType) {
            case "NH3":  setThresholds(t.nh3Caution,  t.nh3Alarm);  break;
            case "H2S":  setThresholds(t.h2sCaution,  t.h2sAlarm);  break;
            case "PM25": setThresholds(t.pm25Caution, t.pm25Alarm); break;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float cx = width / 2f;
        float cy = height * 0.85f;
        float radius = Math.min(width, height * 2) / 2f - 60f;

        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Background arc
        canvas.drawArc(arcRect, 180, 180, false, bgArcPaint);

// Visual zones — always equal thirds
        float third = 60f;
        Paint p = new Paint(arcPaint);
        p.setColor(colorLow);
        canvas.drawArc(arcRect, 180, third, false, p);
        p.setColor(colorCaution);
        canvas.drawArc(arcRect, 180 + third, third, false, p);
        p.setColor(colorAlarm);
        canvas.drawArc(arcRect, 180 + third + third, third, false, p);

// Needle — piecewise mapping so it hits zone boundaries accurately
        float pct;
        if (currentValue <= cautionThreshold) {
            pct = (currentValue / cautionThreshold) * (1f / 3f);
        } else if (currentValue <= alarmThreshold) {
            pct = (1f / 3f) + ((currentValue - cautionThreshold) / (alarmThreshold - cautionThreshold)) * (1f / 3f);
        } else {
            pct = (2f / 3f) + ((currentValue - alarmThreshold) / (maxValue - alarmThreshold)) * (1f / 3f);
        }
        pct = Math.max(0f, Math.min(1f, pct));

        float angle = (float) Math.toRadians(180 + pct * 180);
        float needleLength = radius - 20;
        float nx = cx + (float) Math.cos(angle) * needleLength;
        float ny = cy + (float) Math.sin(angle) * needleLength;

        final TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, value, true);

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(value.data);
        linePaint.setStrokeWidth(6f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(cx, cy, nx, ny, linePaint);

        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, 10f, circlePaint);
    }

    public void setValue(float value) {
        this.currentValue = value; invalidate();
    }
    public void setMinValue(float min) {
        this.minValue = min;
    }
    public void setMaxValue(float max) {
        this.maxValue = max;
    }

    public void setThresholds(float caution, float alarm) {
        this.cautionThreshold = caution;
        this.alarmThreshold   = alarm;
        this.maxValue         = alarm * 1.4f;
        invalidate();
    }
    public float getMaxValue() {
        return this.maxValue;
    }
}