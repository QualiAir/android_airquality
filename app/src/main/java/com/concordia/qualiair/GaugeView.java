package com.concordia.qualiair;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class GaugeView extends View {

    private Paint arcPaint;
    private Paint needlePaint;
    private Paint bgArcPaint;
    private RectF arcRect;

    private float minValue = 0;
    private float maxValue = 50;
    private float currentValue = 18;

    // Colors
    private int[] colors = {
            Color.parseColor("#00D4AA"), // green
            Color.parseColor("#FFB347"), // yellow
            Color.parseColor("#FF4D4D")  // red
    };

    public GaugeView(Context context) {
        super(context);
        init();
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Background arc (gray track)
        bgArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgArcPaint.setStyle(Paint.Style.STROKE);
        bgArcPaint.setStrokeWidth(60f);
        bgArcPaint.setColor(Color.parseColor("#1F2937"));
        bgArcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Colored arc
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(60f);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Needle
        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setColor(Color.WHITE);
        needlePaint.setStrokeWidth(6f);

        arcRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float cx = width / 2f;
        float cy = height * 0.85f; // center point lower so arc shows nicely
        float radius = Math.min(width, height * 2) / 2f - 60f;

        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Draw background arc (gray)
        canvas.drawArc(arcRect, 180, 180, false, bgArcPaint);

        // Draw green section (0 to 50% of arc)
        Paint greenPaint = new Paint(arcPaint);
        greenPaint.setColor(Color.parseColor("#00D4AA"));
        canvas.drawArc(arcRect, 180, 60, false, greenPaint);

        // Draw yellow section (50% to 80%)
        Paint yellowPaint = new Paint(arcPaint);
        yellowPaint.setColor(Color.parseColor("#FFB347"));
        canvas.drawArc(arcRect, 240, 54, false, yellowPaint);

        // Draw red section (80% to 100%)
        Paint redPaint = new Paint(arcPaint);
        redPaint.setColor(Color.parseColor("#FF4D4D"));
        canvas.drawArc(arcRect, 294, 66, false, redPaint);

        // Draw needle
        float pct = (currentValue - minValue) / (maxValue - minValue);
        float angle = (float) Math.toRadians(180 + pct * 180);
        float needleLength = radius - 20;
        float nx = cx + (float) Math.cos(angle) * needleLength;
        float ny = cy + (float) Math.sin(angle) * needleLength;

        // --- FIX STARTS HERE ---
        // 1. Get the correct color from the theme
        final TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, value, true);
        int needleColor = value.data;

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(needleColor);
        linePaint.setStrokeWidth(6f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(cx, cy, nx, ny, linePaint);

        // Draw center circle
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, 10f, circlePaint);
    }

    public void setValue(float value) {
        this.currentValue = value;
        invalidate(); // redraws the view
    }

    public void setMinValue(float min) { this.minValue = min; }
    public void setMaxValue(float max) { this.maxValue = max; }
}