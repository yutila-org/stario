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

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stario.launcher.activities.launcher.widgets.pins.dialog.PinnedAppsGroupDialog;
import com.stario.launcher.apps.Category;
import com.stario.launcher.apps.CategoryManager;
import com.stario.launcher.apps.LauncherApplication;
import com.stario.launcher.themes.ThemedActivity;
import com.stario.launcher.ui.recyclers.autogrid.AutoGridRecyclerView;
import com.stario.launcher.ui.recyclers.autogrid.AutoGridLayoutManager;
import com.stario.launcher.ui.utils.UiUtils;

import java.util.UUID;

public class PinnedAppsPagerAdapter extends RecyclerView.Adapter<PinnedAppsPagerAdapter.PageViewHolder> {
    private final ThemedActivity activity;
    private final SharedPreferences preferences;
    private final PinnedAppsAdapter.OnPopUpShowListener popUpShowListener;
    private final PinnedAppsGroupDialog.TransitionListener transitionListener;
    private final RecyclerView.RecycledViewPool sharedPool;

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    private final CategoryManager.CategoryListener categoryManagerChangeListener;
    private final Category.CategoryItemListener categoryChangeListener;

    private Category category;
    private int columns = 4;
    private int rows = 1;
    private int pageWidth = 0;
    private OnPageCountChangeListener onPageCountChangeListener;

    public interface OnPageCountChangeListener {
        void onPageCountChanged(int pageCount);
    }

    public PinnedAppsPagerAdapter(ThemedActivity activity,
                                 SharedPreferences preferences,
                                 PinnedAppsAdapter.OnPopUpShowListener popUpShowListener,
                                 PinnedAppsGroupDialog.TransitionListener transitionListener) {
        this.activity = activity;
        this.preferences = preferences;
        this.popUpShowListener = popUpShowListener;
        this.transitionListener = transitionListener;
        this.sharedPool = new RecyclerView.RecycledViewPool();

        this.sharedPreferenceChangeListener = (sharedPreferences, key) -> {
            if (PinnedCategory.PINNED_CATEGORY.equals(key)) {
                load();
            }
        };

        this.categoryManagerChangeListener = new CategoryManager.CategoryListener() {
            @Override
            public void onRemoved(Category removedCategory) {
                if (removedCategory.equals(category)) {
                    load();
                }
            }
        };

        this.categoryChangeListener = new Category.CategoryItemListener() {
            @Override
            @SuppressLint("NotifyDataSetChanged")
            public void onInserted(LauncherApplication application) {
                notifyDataSetChanged();
                notifyPageCountChanged();
            }

            @Override
            @SuppressLint("NotifyDataSetChanged")
            public void onRemoved(LauncherApplication application) {
                notifyDataSetChanged();
                notifyPageCountChanged();
            }

            @Override
            @SuppressLint("NotifyDataSetChanged")
            public void onUpdated(LauncherApplication application) {
                notifyDataSetChanged();
            }

            @Override
            @SuppressLint("NotifyDataSetChanged")
            public void onSwapped(int index1, int index2) {
                notifyDataSetChanged();
            }
        };
    }

    public void setOnPageCountChangeListener(OnPageCountChangeListener listener) {
        this.onPageCountChangeListener = listener;
    }

    private void notifyPageCountChanged() {
        if (onPageCountChangeListener != null) {
            onPageCountChangeListener.onPageCountChanged(getItemCount());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void load() {
        if (category != null) {
            category.removeCategoryItemListener(categoryChangeListener);
        }

        try {
            category = CategoryManager.getInstance().get(
                    UUID.fromString(preferences.getString(PinnedCategory.PINNED_CATEGORY, ""))
            );

            if (category != null) {
                category.addCategoryItemListener(categoryChangeListener);
            }
        } catch (IllegalArgumentException exception) {
            category = null;
        }

        UiUtils.post(() -> {
            notifyDataSetChanged();
            notifyPageCountChanged();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDimensions(int columns, int rows) {
        setDimensions(columns, rows, 0);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDimensions(int columns, int rows, int pageWidth) {
        this.columns = Math.max(1, columns);
        this.rows = Math.max(1, rows);
        this.pageWidth = pageWidth;
        notifyDataSetChanged();
        notifyPageCountChanged();
    }

    public int getPageSize() {
        return columns * rows;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public int getItemCount() {
        int total = category != null ? category.getSize() : 0;
        int pageSize = getPageSize();
        if (total == 0 || pageSize == 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AutoGridRecyclerView pageRecycler = new AutoGridRecyclerView(activity);
        int initialWidth = pageWidth > 0 ? pageWidth : (parent.getWidth() > 0 ? parent.getWidth() : ViewGroup.LayoutParams.MATCH_PARENT);
        pageRecycler.setLayoutParams(new RecyclerView.LayoutParams(
                initialWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        pageRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        pageRecycler.setRecycledViewPool(sharedPool);
        pageRecycler.setNestedScrollingEnabled(false);

        return new PageViewHolder(pageRecycler, activity, preferences, popUpShowListener, transitionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        int width = pageWidth;
        if (width <= 0) {
            if (holder.itemView.getParent() instanceof View) {
                width = ((View) holder.itemView.getParent()).getWidth();
            }
            if (width <= 0 && activity != null) {
                width = activity.getResources().getDisplayMetrics().widthPixels;
            }
        }

        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && width > 0 && lp.width != width) {
            lp.width = width;
            holder.itemView.setLayoutParams(lp);
        }

        holder.manager.setSpanCount(columns);
        int pageSize = getPageSize();
        int offset = position * pageSize;
        holder.adapter.setPage(offset, pageSize, false);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        CategoryManager.getInstance().addOnCategoryUpdateListener(categoryManagerChangeListener);
        load();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        CategoryManager.getInstance().removeOnCategoryUpdateListener(categoryManagerChangeListener);
        if (category != null) {
            category.removeCategoryItemListener(categoryChangeListener);
        }
    }

    public static class PageViewHolder extends RecyclerView.ViewHolder {
        final AutoGridRecyclerView recycler;
        final AutoGridLayoutManager manager;
        final PinnedAppsAdapter adapter;

        public PageViewHolder(@NonNull AutoGridRecyclerView recycler,
                              ThemedActivity activity,
                              SharedPreferences preferences,
                              PinnedAppsAdapter.OnPopUpShowListener popUpShowListener,
                              PinnedAppsGroupDialog.TransitionListener transitionListener) {
            super(recycler);
            this.recycler = recycler;
            this.manager = new AutoGridLayoutManager(activity, 1) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            this.manager.setCenterItems(false);
            this.adapter = new PinnedAppsAdapter(activity, preferences, popUpShowListener, transitionListener);
            this.adapter.setAllowGroup(false);
            this.recycler.setLayoutManager(manager);
            this.recycler.setAdapter(adapter);
        }
    }
}
