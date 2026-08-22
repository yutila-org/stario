/*
 * Copyright (C) 2025 Răzvan Albu
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

package com.stario.launcher.apps.popup;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stario.launcher.R;
import com.stario.launcher.apps.IconPackManager;
import com.stario.launcher.themes.ThemedActivity;
import com.stario.launcher.ui.icons.AdaptiveIconView;
import com.stario.launcher.ui.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IconPickerGridAdapter extends RecyclerView.Adapter<IconPickerGridAdapter.ViewHolder> {
    private final ThemedActivity activity;
    private final IconPackManager.IconPack pack;
    private final OnIconSelectedListener listener;
    private final List<String> allNames;
    private List<String> filteredNames;

    public interface OnIconSelectedListener {
        void onIconSelected(IconPackManager.IconPack pack, String drawableName);
    }

    public IconPickerGridAdapter(ThemedActivity activity, IconPackManager.IconPack pack,
                                  OnIconSelectedListener listener) {
        this.activity = activity;
        this.pack = pack;
        this.listener = listener;
        this.allNames = new ArrayList<>();
        this.filteredNames = new ArrayList<>();

        pack.getAllDrawableNames().thenAccept(names -> {
            allNames.addAll(names);
            filteredNames.addAll(names);

            UiUtils.post(this::notifyDataSetChanged);
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (filteredNames == null || position >= filteredNames.size()) {
            return;
        }

        String drawableName = filteredNames.get(position);

        viewHolder.icon.setIcon(null);
        viewHolder.currentName = drawableName;

        pack.getDrawableByName(drawableName).thenAccept(drawable -> {
            UiUtils.post(() -> {
                if (drawableName.equals(viewHolder.currentName)) {
                    viewHolder.icon.setIcon(drawable);
                }
            });
        });

        viewHolder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIconSelected(pack, drawableName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredNames != null ? filteredNames.size() : 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity)
                .inflate(R.layout.pop_up_icon_picker_item, container, false));
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            filteredNames = new ArrayList<>(allNames);
        } else {
            String needle = query.toLowerCase(Locale.ROOT);
            filteredNames = new ArrayList<>();

            for (String name : allNames) {
                if (name.toLowerCase(Locale.ROOT).contains(needle)) {
                    filteredNames.add(name);
                }
            }
        }

        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AdaptiveIconView icon;
        private String currentName;

        public ViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.icon);
            currentName = null;
        }
    }
}
