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

package com.stario.launcher.ui.notifications;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import com.stario.launcher.R;
import com.stario.launcher.preferences.NotificationDots;

public class NotificationDotView extends AppCompatTextView {
    private final GradientDrawable backgroundDrawable = new GradientDrawable();
    private int count = 0;
    private boolean showCount = true;
    private int dotColor = NotificationDots.DEFAULT_COLOR;
    private boolean isCategory = false;

    public NotificationDotView(@NonNull Context context) {
        super(context);
        init();
    }

    public NotificationDotView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NotificationDotView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTextColor(Color.WHITE);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        try {
            setTypeface(ResourcesCompat.getFont(getContext(), R.font.dm_sans_bold));
        } catch (Exception ignored) {
        }
        setIncludeFontPadding(false);
        setGravity(Gravity.CENTER);
        setPadding(0, 0, 0, 0);

        backgroundDrawable.setShape(GradientDrawable.OVAL);
        backgroundDrawable.setColor(dotColor);
        setBackground(backgroundDrawable);
    }

    public void setCategory(boolean category) {
        this.isCategory = category;
    }

    public void update(int count, boolean showCount, int dotColor) {
        update(count, showCount, dotColor, this.isCategory);
    }

    public void update(int count, boolean showCount, int dotColor, boolean isCategory) {
        this.count = count;
        this.showCount = showCount;
        this.dotColor = dotColor;
        this.isCategory = isCategory;

        backgroundDrawable.setColor(dotColor);

        if (count <= 0) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            if (showCount) {
                String countStr = String.valueOf(count);
                if (isCategory) {
                    if (countStr.length() <= 1) {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
                    } else if (countStr.length() == 2) {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                    } else if (countStr.length() == 3) {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
                    } else {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f);
                    }
                } else {
                    if (countStr.length() <= 1) {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                    } else if (countStr.length() == 2) {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                    } else if (countStr.length() == 3) {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f);
                    } else {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 7f);
                    }
                }
                setText(countStr);
            } else {
                setText("");
            }
            requestLayout();
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (count <= 0) {
            setMeasuredDimension(0, 0);
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        float dpSize;
        if (isCategory) {
            dpSize = showCount ? 26f : 14f;
        } else {
            dpSize = showCount ? 22f : 12f;
        }
        int size = Math.round(dpSize * density);

        setMeasuredDimension(size, size);
    }
}
