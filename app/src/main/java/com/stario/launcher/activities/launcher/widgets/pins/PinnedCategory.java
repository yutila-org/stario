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

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.stario.launcher.R;
import com.stario.launcher.activities.launcher.widgets.pins.dialog.PinnedAppsGroupDialog;
import com.stario.launcher.apps.CategoryManager;
import com.stario.launcher.preferences.Entry;
import com.stario.launcher.themes.ThemedActivity;
import com.stario.launcher.ui.Measurements;
import com.stario.launcher.ui.common.grid.DraggableGridItem;
import com.stario.launcher.ui.common.grid.DynamicGridLayout;
import com.stario.launcher.ui.icons.AdaptiveIconView;
import com.stario.launcher.ui.recyclers.autogrid.AutoGridLayoutManager;
import com.stario.launcher.ui.recyclers.autogrid.AutoGridRecyclerView;
import com.stario.launcher.ui.utils.LayoutSizeObserver;
import com.stario.launcher.ui.utils.UiUtils;

public class PinnedCategory {
    public static final String PINNED_CATEGORY_VISIBLE = "com.stario.IS_PINNED_CATEGORY_VISIBLE";
    public static final String PINNED_CATEGORY = "com.stario.PINNED_CATEGORY";
    public static final String PINNED_CATEGORY_MODE = "com.stario.PINNED_CATEGORY_MODE";
    public static final int MODE_FOLDER = 0;
    public static final int MODE_SCROLL = 1;
    private static final String CATEGORY_TAG = "CategoryGlance";

    private final CategoryManager categoryManager;
    private final SharedPreferences preferences;
    private final ThemedActivity activity;

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private AutoGridRecyclerView recycler;
    private HorizontalPagerRecyclerView pagerRecycler;
    private PageIndicatorView pageIndicator;
    private PinnedAppsAdapter adapter;
    private PinnedAppsPagerAdapter pagerAdapter;

    private DynamicGridLayout.ItemLayoutData layoutData;
    private DraggableGridItem gridItem;
    private boolean isAttached;

    public PinnedCategory(ThemedActivity activity) {
        this.isAttached = false;
        this.activity = activity;
        this.categoryManager = CategoryManager.getInstance();
        this.preferences = activity.getApplicationContext()
                .getSharedPreferences(Entry.PINNED_CATEGORY);
    }

    public void attach(DynamicGridLayout container,
                       PinnedAppsAdapter.OnPopUpShowListener popUpShowListener,
                       PinnedAppsGroupDialog.TransitionListener transitionListener) {
        if (gridItem != null) {
            return;
        }

        gridItem = new DraggableGridItem(activity);
        gridItem.itemId = CATEGORY_TAG;

        layoutData = new DynamicGridLayout.ItemLayoutData(CATEGORY_TAG,
                0, 0, 4, 1);
        layoutData.minColSpan = 1;
        layoutData.minRowSpan = 1;

        LinearLayout root = (LinearLayout) activity.getLayoutInflater()
                .inflate(R.layout.pinned_apps, gridItem, false);
        recycler = root.findViewById(R.id.recycler);
        pagerRecycler = root.findViewById(R.id.pager_recycler);
        pageIndicator = root.findViewById(R.id.page_indicator);
        gridItem.addView(root);

        listener = (sharedPreferences, key) -> {
            if (PINNED_CATEGORY_VISIBLE.equals(key)) {
                updateContainerState(container,
                        sharedPreferences.getBoolean(PINNED_CATEGORY_VISIBLE, false));
            } else if (PINNED_CATEGORY_MODE.equals(key)) {
                UiUtils.post(this::updateMode);
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);

        AutoGridLayoutManager manager = new AutoGridLayoutManager(activity, 1) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        adapter = new PinnedAppsAdapter(activity,
                preferences, popUpShowListener, transitionListener);
        adapter.setAllowGroup(true);

        recycler.setItemAnimator(null);
        recycler.setLayoutManager(manager);

        LinearLayoutManager pagerLayoutManager = new LinearLayoutManager(activity,
                LinearLayoutManager.HORIZONTAL, false);
        pagerRecycler.setLayoutManager(pagerLayoutManager);
        pagerRecycler.setItemAnimator(null);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(pagerRecycler);

        pagerAdapter = new PinnedAppsPagerAdapter(activity,
                preferences, popUpShowListener, transitionListener);
        pagerAdapter.setOnPageCountChangeListener(count -> UiUtils.post(() -> {
            pageIndicator.setPageCount(count);
            int mode = preferences.getInt(PINNED_CATEGORY_MODE, MODE_FOLDER);
            if (mode == MODE_SCROLL) {
                pageIndicator.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
            } else {
                pageIndicator.setVisibility(View.GONE);
            }
        }));

        pagerRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                super.onScrollStateChanged(rv, newState);
                updateCurrentPage();
            }

            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                updateCurrentPage();
            }

            private void updateCurrentPage() {
                View snapView = snapHelper.findSnapView(pagerLayoutManager);
                if (snapView != null) {
                    int position = pagerLayoutManager.getPosition(snapView);
                    if (position != RecyclerView.NO_POSITION) {
                        pageIndicator.setCurrentPage(position);
                    }
                }
            }
        });

        LayoutSizeObserver.attach(root, LayoutSizeObserver.WIDTH | LayoutSizeObserver.HEIGHT,
                new LayoutSizeObserver.OnChange() {
            @Override
            public void onChange(View view, int watchFlags, Rect rect) {
                int itemDim = AdaptiveIconView.getMaxIconSize() + Measurements.getDefaultPadding();
                int rowHeight = Measurements.dpToPx(85);
                int columns = MathUtils.clamp(rect.width() / itemDim, 1, 6);
                int rows = Math.max(1, rect.height() / rowHeight);

                manager.setSpanCount(columns);
                adapter.setMaxItemCount(columns * rows);

                pagerAdapter.setDimensions(columns, rows, rect.width());
                updateMode();
            }
        });

        categoryManager.addOnReadyListener(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }

            recycler.setAdapter(adapter);
            pagerRecycler.setAdapter(pagerAdapter);
            updateMode();
        });

        updateContainerState(container, preferences.getBoolean(PINNED_CATEGORY_VISIBLE, false));
    }

    private void updateMode() {
        int mode = preferences.getInt(PINNED_CATEGORY_MODE, MODE_FOLDER);
        if (mode == MODE_SCROLL) {
            recycler.setVisibility(View.GONE);
            pagerRecycler.setVisibility(View.VISIBLE);
            int pageCount = pagerAdapter != null ? pagerAdapter.getItemCount() : 0;
            pageIndicator.setPageCount(pageCount);
            pageIndicator.setVisibility(pageCount > 1 ? View.VISIBLE : View.GONE);
        } else {
            pagerRecycler.setVisibility(View.GONE);
            pageIndicator.setVisibility(View.GONE);
            recycler.setVisibility(View.VISIBLE);
        }
    }

    private void updateContainerState(DynamicGridLayout container, boolean shouldBeVisible) {
        UiUtils.post(() -> {
            if (shouldBeVisible && !isAttached) {
                container.addItem(gridItem, layoutData);
                isAttached = true;
            } else if (!shouldBeVisible && isAttached) {
                container.removeItem(gridItem);
                isAttached = false;
            }
        });
    }

    public void detach() {
        if (recycler != null) {
            recycler.setAdapter(null);
        }
        if (pagerRecycler != null) {
            pagerRecycler.setAdapter(null);
        }

        if (listener != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }
}
