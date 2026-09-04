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

package com.stario.launcher.activities.launcher.widgets.pins;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.stario.launcher.themes.ThemedActivity;
import com.stario.launcher.ui.Measurements;

public class PageIndicatorView extends View {
    private final Paint pillBackgroundPaint;
    private final Paint activeDotPaint;
    private final Paint inactiveDotPaint;
    private final RectF pillRect;

    private int pageCount = 0;
    private int currentPage = 0;

    public PageIndicatorView(Context context) {
        this(context, null);
    }

    public PageIndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        pillBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activeDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        inactiveDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pillRect = new RectF();

        initColors(context);
    }

    private void initColors(Context context) {
        int pillBgColor = 0x66000000;
        int activeColor = 0xFFFFFFFF;
        int inactiveColor = 0x55FFFFFF;

        if (context instanceof ThemedActivity) {
            ThemedActivity activity = (ThemedActivity) context;
            try {
                int surfaceContainer = activity.getAttributeData(
                        com.google.android.material.R.attr.colorSurfaceContainer);
                int onSurface = activity.getAttributeData(
                        com.google.android.material.R.attr.colorOnSurface);
                pillBgColor = (surfaceContainer & 0x00FFFFFF) | 0x99000000;
                activeColor = onSurface;
                inactiveColor = (onSurface & 0x00FFFFFF) | 0x4D000000;
            } catch (Exception ignored) {
            }
        }

        pillBackgroundPaint.setColor(pillBgColor);
        pillBackgroundPaint.setStyle(Paint.Style.FILL);

        activeDotPaint.setColor(activeColor);
        activeDotPaint.setStyle(Paint.Style.FILL);

        inactiveDotPaint.setColor(inactiveColor);
        inactiveDotPaint.setStyle(Paint.Style.FILL);
    }

    public void setPageCount(int count) {
        if (this.pageCount != count) {
            this.pageCount = count;
            if (currentPage >= pageCount) {
                currentPage = Math.max(0, pageCount - 1);
            }
            requestLayout();
            invalidate();
        }
    }

    public void setCurrentPage(int page) {
        int clampedPage = Math.max(0, Math.min(page, Math.max(0, pageCount - 1)));
        if (this.currentPage != clampedPage) {
            this.currentPage = clampedPage;
            invalidate();
        }
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (pageCount <= 1) {
            setMeasuredDimension(0, 0);
            return;
        }

        int dotDiameter = Measurements.dpToPx(5);
        int dotSpacing = Measurements.dpToPx(6);
        int paddingH = Measurements.dpToPx(8);
        int paddingV = Measurements.dpToPx(4);

        int contentWidth = pageCount * dotDiameter + (pageCount - 1) * dotSpacing;
        int totalWidth = contentWidth + paddingH * 2;
        int totalHeight = dotDiameter + paddingV * 2;

        setMeasuredDimension(
                resolveSize(totalWidth, widthMeasureSpec),
                resolveSize(totalHeight, heightMeasureSpec)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pageCount <= 1) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float radius = height / 2f;

        pillRect.set(0, 0, width, height);
        canvas.drawRoundRect(pillRect, radius, radius, pillBackgroundPaint);

        int dotDiameter = Measurements.dpToPx(5);
        int dotSpacing = Measurements.dpToPx(6);
        int contentWidth = pageCount * dotDiameter + (pageCount - 1) * dotSpacing;

        float startX = (width - contentWidth) / 2f;
        float centerY = height / 2f;
        float dotRadius = dotDiameter / 2f;

        for (int i = 0; i < pageCount; i++) {
            float cx = startX + dotRadius + i * (dotDiameter + dotSpacing);
            Paint paint = (i == currentPage) ? activeDotPaint : inactiveDotPaint;
            canvas.drawCircle(cx, centerY, dotRadius, paint);
        }
    }
}
