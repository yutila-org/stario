/*
 * Copyright (C) 2026 Yutila
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package com.stario.launcher.ui.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Interactive HSV color wheel view inspired by ColorPickerView (by skydoves).
 */
public class ColorWheelView extends View {
    private static final int[] COLORS = new int[]{
            0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF,
            0xFF00FF00, 0xFFFFFF00, 0xFFFF0000
    };

    private final Paint wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint brightnessOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float[] hsv = new float[]{0f, 1f, 1f};
    private final PointF thumbPosition = new PointF();

    private float centerX;
    private float centerY;
    private float wheelRadius;
    private float thumbRadius = 14f;

    private OnColorChangeListener onColorChangeListener;

    public interface OnColorChangeListener {
        void onColorChanged(int color, boolean fromUser);
    }

    public ColorWheelView(Context context) {
        super(context);
        init();
    }

    public ColorWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        thumbStrokePaint.setStyle(Paint.Style.STROKE);
        thumbStrokePaint.setStrokeWidth(4f);
        thumbStrokePaint.setColor(Color.WHITE);

        brightnessOverlayPaint.setColor(Color.BLACK);
        thumbRadius = getResources().getDisplayMetrics().density * 12f;
    }

    public void setOnColorChangeListener(OnColorChangeListener listener) {
        this.onColorChangeListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w * 0.5f;
        centerY = h * 0.5f;
        wheelRadius = Math.max(0, Math.min(centerX, centerY) - thumbRadius - 4f);

        if (wheelRadius > 0) {
            Shader sweepShader = new SweepGradient(centerX, centerY, COLORS, null);
            Shader radialShader = new RadialGradient(centerX, centerY, wheelRadius,
                    Color.WHITE, 0x00FFFFFF, Shader.TileMode.CLAMP);
            ComposeShader composeShader = new ComposeShader(sweepShader, radialShader, PorterDuff.Mode.SRC_OVER);
            wheelPaint.setShader(composeShader);
        }

        updateThumbPosition();
    }

    private void updateThumbPosition() {
        if (wheelRadius <= 0) {
            return;
        }

        float angleRad = (float) Math.toRadians(hsv[0]);
        float dist = hsv[1] * wheelRadius;

        thumbPosition.x = centerX + (float) Math.cos(angleRad) * dist;
        thumbPosition.y = centerY + (float) Math.sin(angleRad) * dist;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (wheelRadius <= 0) {
            return;
        }

        // Draw color wheel
        canvas.drawCircle(centerX, centerY, wheelRadius, wheelPaint);

        // Draw brightness overlay
        if (hsv[2] < 1.0f) {
            int alpha = Math.round((1.0f - hsv[2]) * 255);
            brightnessOverlayPaint.setAlpha(alpha);
            canvas.drawCircle(centerX, centerY, wheelRadius, brightnessOverlayPaint);
        }

        // Draw thumb
        int currentColor = getColor();
        thumbPaint.setColor(currentColor);

        canvas.drawCircle(thumbPosition.x, thumbPosition.y, thumbRadius, thumbPaint);
        canvas.drawCircle(thumbPosition.x, thumbPosition.y, thumbRadius, thumbStrokePaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                handleTouch(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                handleTouch(event.getX(), event.getY());
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void handleTouch(float x, float y) {
        if (wheelRadius <= 0) {
            return;
        }

        float dx = x - centerX;
        float dy = y - centerY;
        float dist = (float) Math.hypot(dx, dy);

        float saturation = Math.min(1.0f, dist / wheelRadius);
        float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (angleDeg < 0) {
            angleDeg += 360f;
        }

        hsv[0] = angleDeg;
        hsv[1] = saturation;

        updateThumbPosition();
        invalidate();

        if (onColorChangeListener != null) {
            onColorChangeListener.onColorChanged(getColor(), true);
        }
    }

    public int getColor() {
        return Color.HSVToColor(hsv);
    }

    public float getBrightness() {
        return hsv[2];
    }

    public void setBrightness(float brightness, boolean fromUser) {
        hsv[2] = Math.max(0f, Math.min(1f, brightness));
        invalidate();

        if (onColorChangeListener != null) {
            onColorChangeListener.onColorChanged(getColor(), fromUser);
        }
    }

    public void setColor(int color) {
        Color.colorToHSV(color, hsv);
        updateThumbPosition();
        invalidate();

        if (onColorChangeListener != null) {
            onColorChangeListener.onColorChanged(color, false);
        }
    }
}
