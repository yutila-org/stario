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

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.stario.launcher.R;
import com.stario.launcher.Stario;
import com.stario.launcher.preferences.Entry;
import com.stario.launcher.preferences.NotificationDots;
import com.stario.launcher.activities.settings.dialogs.NotificationConfigurator;
import com.stario.launcher.themes.ThemedActivity;
import com.stario.launcher.ui.dialogs.ActionDialog;
import com.stario.launcher.utils.Utils;

import java.util.Locale;

public class NotificationDotsDialog extends ActionDialog {
    private final SharedPreferences preferences;
    private final LocalBroadcastManager localBroadcastManager;

    private MaterialSwitch counterEnabledSwitch;
    private View counterEnabledContainer;
    private MaterialSwitch showCountSwitch;
    private View showCountContainer;
    private View colorContainer;
    private TextView hexBadge;
    private View colorBadge;

    private int currentColor;

    public NotificationDotsDialog(@NonNull ThemedActivity activity) {
        super(activity);

        Stario stario = activity.getApplicationContext();
        this.preferences = stario.getSharedPreferences(Entry.NOTIFICATION_DOTS);
        this.localBroadcastManager = LocalBroadcastManager.getInstance(activity);
    }

    @NonNull
    @Override
    protected View inflateContent(LayoutInflater inflater) {
        View root = inflater.inflate(R.layout.pop_up_notification_dots, null);

        counterEnabledSwitch = root.findViewById(R.id.counter_enabled);
        counterEnabledContainer = root.findViewById(R.id.counter_enabled_container);
        showCountSwitch = root.findViewById(R.id.show_count);
        showCountContainer = root.findViewById(R.id.show_count_container);
        colorContainer = root.findViewById(R.id.color_container);
        hexBadge = root.findViewById(R.id.hex_badge);
        colorBadge = root.findViewById(R.id.color_badge);

        currentColor = preferences.getInt(NotificationDots.NOTIFICATION_DOTS_COLOR,
                NotificationDots.DEFAULT_COLOR);
        updateColorViews(currentColor);

        boolean enabled = preferences.getBoolean(NotificationDots.NOTIFICATION_DOTS_ENABLED, true);
        boolean showCount = preferences.getBoolean(NotificationDots.NOTIFICATION_DOTS_SHOW_COUNT, true);

        setupSwitch(counterEnabledSwitch, counterEnabledContainer, enabled, (button, checked) -> {
            preferences.edit()
                    .putBoolean(NotificationDots.NOTIFICATION_DOTS_ENABLED, checked)
                    .apply();

            updateDependentOptions(checked);
            notifySettingChanged();

            if (checked && !Utils.isNotificationServiceEnabled(activity)) {
                showNotificationPermissionDialog();
            }
        });

        setupSwitch(showCountSwitch, showCountContainer, showCount, (button, checked) -> {
            preferences.edit()
                    .putBoolean(NotificationDots.NOTIFICATION_DOTS_SHOW_COUNT, checked)
                    .apply();

            notifySettingChanged();
        });

        colorContainer.setOnClickListener(v -> {
            if (!counterEnabledSwitch.isChecked()) {
                return;
            }

            ColorPickerDialog dialog = new ColorPickerDialog(activity, currentColor, color -> {
                currentColor = color;
                preferences.edit()
                        .putInt(NotificationDots.NOTIFICATION_DOTS_COLOR, color)
                        .apply();

                updateColorViews(color);
                notifySettingChanged();
            });

            dialog.show();
        });

        updateDependentOptions(enabled);

        return root;
    }

    private void updateColorViews(int color) {
        String hex = String.format(Locale.ROOT, "#%06X", 0xFFFFFF & color);
        hexBadge.setText(hex);

        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setCornerRadius(activity.getResources().getDisplayMetrics().density * 8f);
        gd.setColor(color);
        colorBadge.setBackground(gd);
    }

    private void updateDependentOptions(boolean enabled) {
        showCountContainer.setEnabled(enabled);
        showCountSwitch.setEnabled(enabled);
        showCountContainer.setAlpha(enabled ? 1.0f : 0.45f);

        colorContainer.setEnabled(enabled);
        colorContainer.setAlpha(enabled ? 1.0f : 0.45f);
    }

    private void showNotificationPermissionDialog() {
        NotificationConfigurator dialog = new NotificationConfigurator(activity);
        dialog.setOnDismissListener(d -> checkNotificationPermission());
        dialog.show();
    }

    private void checkNotificationPermission() {
        if (!Utils.isNotificationServiceEnabled(activity)) {
            counterEnabledSwitch.setChecked(false);
            preferences.edit()
                    .putBoolean(NotificationDots.NOTIFICATION_DOTS_ENABLED, false)
                    .apply();
            updateDependentOptions(false);
            notifySettingChanged();
        }
    }

    private void notifySettingChanged() {
        localBroadcastManager.sendBroadcastSync(new Intent(NotificationDots.INTENT_NOTIFICATION_DOTS_CHANGED));
    }

    private void setupSwitch(MaterialSwitch switchView, @Nullable View container,
                             boolean defaultValue, CompoundButton.OnCheckedChangeListener listener) {
        switchView.setChecked(defaultValue);
        switchView.jumpDrawablesToCurrentState();
        switchView.setOnCheckedChangeListener(listener);

        if (container != null) {
            container.setOnClickListener(view -> switchView.performClick());
        }
    }

    @Override
    public void show() {
        super.show();
        if (preferences.getBoolean(NotificationDots.NOTIFICATION_DOTS_ENABLED, true)
                && !Utils.isNotificationServiceEnabled(activity)) {
            showNotificationPermissionDialog();
        }
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
