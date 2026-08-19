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

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.stario.launcher.apps.IconPackManager;
import com.stario.launcher.themes.ThemedActivity;

import java.util.ArrayList;
import java.util.List;

public class IconPickerPagerAdapter extends PagerAdapter {
    private final ThemedActivity activity;
    private final IconPickerGridAdapter.OnIconSelectedListener listener;
    private final List<IconPackManager.IconPack> packs;
    private final List<IconPickerPage> pages;

    public IconPickerPagerAdapter(ThemedActivity activity,
                                   List<IconPackManager.IconPack> packs,
                                   IconPickerGridAdapter.OnIconSelectedListener listener) {
        this.activity = activity;
        this.packs = packs;
        this.listener = listener;
        this.pages = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return packs.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull View container, int position) {
        IconPackManager.IconPack pack = packs.get(position);
        IconPickerPage page = new IconPickerPage(activity, pack, listener);
        View view = page.createView(activity.getLayoutInflater(), (android.view.ViewGroup) container);

        pages.add(position, page);
        ((android.view.ViewGroup) container).addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull View container, int position, @NonNull Object object) {
        ((android.view.ViewGroup) container).removeView((View) object);

        if (position < pages.size()) {
            pages.get(position).destroy();
            pages.remove(position);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return packs.get(position).getLabel();
    }

    public void filter(int position, String query) {
        if (position >= 0 && position < pages.size()) {
            pages.get(position).filter(query);
        }
    }
}
