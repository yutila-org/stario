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

package com.stario.launcher.activities.settings.dialogs.notifications;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.slider.Slider;
import com.stario.launcher.R;
import com.stario.launcher.themes.ThemedActivity;
import com.stario.launcher.ui.colorpicker.ColorWheelView;
import com.stario.launcher.ui.dialogs.ActionDialog;

import java.util.Locale;

public class ColorPickerDialog extends ActionDialog {
    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    private final int initialColor;
    private final OnColorSelectedListener listener;

    private ColorWheelView colorWheel;
    private Slider brightnessSlider;
    private EditText hexInput;
    private View colorPreview;

    private int selectedColor;
    private boolean isUpdatingFromCode = false;

    public ColorPickerDialog(@NonNull ThemedActivity activity, int initialColor, OnColorSelectedListener listener) {
        super(activity);
        this.initialColor = initialColor;
        this.selectedColor = initialColor;
        this.listener = listener;
    }

    @NonNull
    @Override
    protected View inflateContent(LayoutInflater inflater) {
        View root = inflater.inflate(R.layout.pop_up_color_picker, null);

        colorWheel = root.findViewById(R.id.color_wheel);
        brightnessSlider = root.findViewById(R.id.brightness_slider);
        hexInput = root.findViewById(R.id.hex_input);
        colorPreview = root.findViewById(R.id.color_preview);

        brightnessSlider.setValueFrom(0f);
        brightnessSlider.setValueTo(1f);

        colorWheel.setColor(initialColor);
        float initialBrightness = Math.max(0f, Math.min(1f, colorWheel.getBrightness()));
        brightnessSlider.setValue(initialBrightness);
        updateHexText(initialColor);
        updateColorPreview(initialColor);

        colorWheel.setOnColorChangeListener((color, fromUser) -> {
            selectedColor = color;
            updateColorPreview(color);

            if (!isUpdatingFromCode) {
                isUpdatingFromCode = true;
                updateHexText(color);
                float b = Math.max(0f, Math.min(1f, colorWheel.getBrightness()));
                brightnessSlider.setValue(b);
                isUpdatingFromCode = false;
            }
        });

        brightnessSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                colorWheel.setBrightness(value, true);
            }
        });

        hexInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingFromCode || s == null) {
                    return;
                }

                String text = s.toString().trim();
                if (!text.startsWith("#")) {
                    text = "#" + text;
                }

                if (text.length() == 7 || text.length() == 9) {
                    try {
                        int parsed = Color.parseColor(text);
                        selectedColor = parsed;
                        updateColorPreview(parsed);

                        isUpdatingFromCode = true;
                        colorWheel.setColor(parsed);
                        float b = Math.max(0f, Math.min(1f, colorWheel.getBrightness()));
                        brightnessSlider.setValue(b);
                        isUpdatingFromCode = false;
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        });

        root.findViewById(R.id.cancel).setOnClickListener(v -> dismiss());
        root.findViewById(R.id.proceed).setOnClickListener(v -> {
            if (listener != null) {
                listener.onColorSelected(selectedColor);
            }
            dismiss();
        });

        return root;
    }

    private void updateColorPreview(int color) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setCornerRadius(activity.getResources().getDisplayMetrics().density * 12f);
        gd.setColor(color);
        colorPreview.setBackground(gd);
    }

    private void updateHexText(int color) {
        String hex = String.format(Locale.ROOT, "#%06X", 0xFFFFFF & color);
        hexInput.setText(hex);
        hexInput.setSelection(hex.length());
    }

    @Override
    protected int getDesiredInitialState() {
        return BottomSheetBehavior.STATE_EXPANDED;
    }

    @Override
    protected boolean blurBehind() {
        return true;
    }
}
