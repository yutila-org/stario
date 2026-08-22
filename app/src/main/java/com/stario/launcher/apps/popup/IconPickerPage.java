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

package com.stario.launcher.apps.popup;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stario.launcher.R;
import com.stario.launcher.apps.IconPackManager;
import com.stario.launcher.themes.ThemedActivity;
import com.stario.launcher.ui.recyclers.autogrid.AutoGridLayoutManager;
import com.stario.launcher.ui.recyclers.overscroll.OverScrollEffect;
import com.stario.launcher.ui.recyclers.overscroll.OverScrollRecyclerView;
import com.stario.launcher.ui.utils.LayoutSizeObserver;

public class IconPickerPage {
    private final ThemedActivity activity;
    private final IconPackManager.IconPack pack;
    private final IconPickerGridAdapter.OnIconSelectedListener listener;
    private View rootView;
    private IconPickerGridAdapter adapter;
    private OverScrollRecyclerView recyclerView;

    public IconPickerPage(ThemedActivity activity, IconPackManager.IconPack pack,
                          IconPickerGridAdapter.OnIconSelectedListener listener) {
        this.activity = activity;
        this.pack = pack;
        this.listener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    public View createView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        rootView = inflater.inflate(R.layout.pop_up_icon_picker_page, container, false);

        recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.setOverscrollPullEdges(OverScrollEffect.PULL_EDGE_BOTTOM);

        AutoGridLayoutManager manager = new AutoGridLayoutManager(activity, 1);
        LayoutSizeObserver.attach(rootView, LayoutSizeObserver.WIDTH, new LayoutSizeObserver.OnChange() {
            @Override
            public void onChange(View view, int watchFlags, Rect rect) {
                manager.setSpanCount(Math.min(6, rect.width() / 90));
            }
        });

        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(null);

        adapter = new IconPickerGridAdapter(activity, pack, listener);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    public void destroy() {
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }

        rootView = null;
        recyclerView = null;
        adapter = null;
    }

    public void filter(String query) {
        if (adapter != null) {
            adapter.filter(query);
        }
    }

    public IconPackManager.IconPack getPack() {
        return pack;
    }
}
