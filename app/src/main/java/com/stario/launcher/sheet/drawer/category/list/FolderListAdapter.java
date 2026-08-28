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

package com.stario.launcher.sheet.drawer.category.list;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stario.launcher.R;
import com.stario.launcher.apps.Category;
import com.stario.launcher.apps.CategoryManager;
import com.stario.launcher.preferences.Entry;
import com.stario.launcher.preferences.NotificationDots;
import com.stario.launcher.preferences.Vibrations;
import com.stario.launcher.sheet.drawer.category.Categories;
import com.stario.launcher.sheet.drawer.category.folder.Folder;
import com.stario.launcher.themes.ThemedActivity;
import com.stario.launcher.ui.icons.AdaptiveIconView;
import com.stario.launcher.ui.notifications.NotificationDotView;
import com.stario.launcher.ui.recyclers.async.AsyncRecyclerAdapter;
import com.stario.launcher.ui.utils.UiUtils;
import com.stario.launcher.ui.utils.animation.Animation;
import com.stario.launcher.ui.utils.animation.FragmentTransition;
import com.stario.launcher.ui.utils.animation.SharedElementTransition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FolderListAdapter extends AsyncRecyclerAdapter<FolderListAdapter.ViewHolder> {
    private static final float TARGET_ELEVATION = 10;
    private static final float TARGET_SCALE = 0.9f;

    private final CategoryManager.CategoryListener listener;
    private final CategoryManager categoryManager;
    private final ThemedActivity activity;
    private final FolderList folderList;
    private final Folder folder;

    private final SharedPreferences notificationDotsPrefs;
    private final BroadcastReceiver notificationDotsReceiver;
    private boolean notificationDotsEnabled;
    private boolean notificationDotsShowCount;
    private int notificationDotsColor;

    public FolderListAdapter(ThemedActivity activity, FolderList folderList) {
        super(activity);

        this.activity = activity;
        this.folderList = folderList;

        this.categoryManager = CategoryManager.getInstance();
        this.folder = new Folder();

        this.notificationDotsPrefs = activity.getApplicationContext()
                .getSharedPreferences(Entry.NOTIFICATION_DOTS);
        loadNotificationDotsSettings();

        this.notificationDotsReceiver = new BroadcastReceiver() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onReceive(Context context, Intent intent) {
                loadNotificationDotsSettings();
                notifyDataSetChanged();
            }
        };

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                notificationDotsReceiver,
                new IntentFilter(NotificationDots.INTENT_NOTIFICATION_DOTS_CHANGED)
        );

        this.listener = new CategoryManager.CategoryListener() {
            int preparedRemovalIndex = -1;

            @Override
            public void onCreated(Category category) {
                int index = categoryManager.indexOf(category);

                if (index >= 0) {
                    UiUtils.post(() -> notifyItemInserted(index));
                }
            }

            @Override
            public void onChanged(Category category) {
                int index = categoryManager.indexOf(category);

                if (index >= 0) {
                    UiUtils.post(() -> notifyItemChanged(index));
                }
            }

            @Override
            public void onPrepareRemoval(Category category) {
                if (preparedRemovalIndex < 0) {
                    preparedRemovalIndex = categoryManager.indexOf(category);
                }
            }

            @Override
            public void onRemoved(Category category) {
                if (preparedRemovalIndex >= 0) {
                    UiUtils.post(() -> notifyItemRemoved(preparedRemovalIndex));

                    preparedRemovalIndex = -1;
                } else {
                    //noinspection NotifyDataSetChanged
                    UiUtils.post(() -> notifyDataSetChanged());
                }
            }
        };

        setHasStableIds(true);
    }

    public boolean move(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder targetHolder) {
        int position = viewHolder.getBindingAdapterPosition();
        int target = targetHolder.getBindingAdapterPosition();

        if (position == target
                || position == RecyclerView.NO_POSITION
                || target == RecyclerView.NO_POSITION) {
            return false;
        }

        while (position - target != 0) {
            int newTarget = position - ((position - target) > 0 ? 1 : -1);

            categoryManager.swap(position, newTarget);
            notifyItemMoved(position, newTarget);

            position = newTarget;
        }

        return true;
    }

    public void focus(RecyclerView.ViewHolder holder) {
        if (holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;

            holder.itemView.bringToFront();

            viewHolder.itemView.animate()
                    .scaleY(TARGET_SCALE)
                    .scaleX(TARGET_SCALE)
                    .translationZ(TARGET_ELEVATION)
                    .setDuration(Animation.MEDIUM.getDuration());
            viewHolder.category.animate()
                    .alpha(0)
                    .setDuration(Animation.MEDIUM.getDuration());
        }
    }

    public void reset(RecyclerView.ViewHolder holder) {
        if (holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;

            viewHolder.itemView.animate()
                    .scaleY(1f)
                    .scaleX(1f)
                    .translationZ(0)
                    .setDuration(Animation.MEDIUM.getDuration());
            viewHolder.category.animate()
                    .alpha(1f)
                    .setDuration(Animation.MEDIUM.getDuration());
        }
    }

    private void loadNotificationDotsSettings() {
        this.notificationDotsEnabled = notificationDotsPrefs.getBoolean(
                NotificationDots.NOTIFICATION_DOTS_ENABLED, true);
        this.notificationDotsShowCount = notificationDotsPrefs.getBoolean(
                NotificationDots.NOTIFICATION_DOTS_SHOW_COUNT, true);
        this.notificationDotsColor = notificationDotsPrefs.getInt(
                NotificationDots.NOTIFICATION_DOTS_COLOR, NotificationDots.DEFAULT_COLOR);
    }

    public class ViewHolder extends AsyncViewHolder {
        private TextView category;
        private RecyclerView recycler;
        private FolderListItemAdapter adapter;
        private NotificationDotView notification;

        @Override
        protected void onInflated() {
            category = itemView.findViewById(R.id.category);
            recycler = itemView.findViewById(R.id.items);
            notification = itemView.findViewById(R.id.notification_dot);

            recycler.setLayoutManager(createManager());
            recycler.setItemAnimator(null);

            adapter = new FolderListItemAdapter(activity);

            recycler.setAdapter(adapter);

            itemView.setHapticFeedbackEnabled(false);
            recycler.setHapticFeedbackEnabled(false);
        }

        @NonNull
        private GridLayoutManager createManager() {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 4) {
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            };

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (position < FolderListItemAdapter.SOFT_LIMIT ||
                            (adapter != null && adapter.getItemCount() < FolderListItemAdapter.HARD_LIMIT)) {
                        return 2;
                    } else {
                        return 1;
                    }
                }
            });

            return gridLayoutManager;
        }

        public void updateCategory(Category category) {
            adapter.setCategory(category);
        }
    }

    @Override
    public void onBind(@NonNull ViewHolder viewHolder, int index) {
        Category category = categoryManager.get(index);

        viewHolder.category.setText(categoryManager.getCategoryName(category.identifier));

        if (viewHolder.notification != null) {
            int categoryCount = category != null ? category.getNotificationCount() : 0;
            if (notificationDotsEnabled && categoryCount > 0) {
                viewHolder.notification.update(categoryCount,
                        notificationDotsShowCount, notificationDotsColor, true);
            } else {
                viewHolder.notification.setVisibility(View.GONE);
            }
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            private AdaptiveIconView getIcon(View view) {
                if (view instanceof AdaptiveIconView) {
                    return (AdaptiveIconView) view;
                }

                if (view instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) view;

                    for (int index = 0, count = group.getChildCount(); index < count; index++) {
                        AdaptiveIconView icon = getIcon(group.getChildAt(index));

                        if (icon != null) {
                            return icon;
                        }
                    }
                }

                return null;
            }

            @Override
            public void onClick(View view) {
                if (!folder.isAdded() && !folder.isRemoving()) {
                    Vibrations.getInstance().vibrate();

                    List<View> excluded = new ArrayList<>();

                    FragmentManager fragmentManager = folderList.getParentFragmentManager();
                    RecyclerView.LayoutManager layoutManager = viewHolder.recycler.getLayoutManager();

                    if (layoutManager != null) {
                        FragmentTransaction transaction = fragmentManager.beginTransaction();

                        for (int position = 0;
                             position < viewHolder.adapter.getItemCount() &&
                                     position < FolderListItemAdapter.HARD_LIMIT;
                             position++) {

                            View group = layoutManager.findViewByPosition(position);
                            excluded.add(group);

                            AdaptiveIconView icon = getIcon(group);
                            if (icon != null) {
                                transaction.addSharedElement(icon, icon.getTransitionName());
                                excluded.add(icon);
                            }
                        }

                        if (UiUtils.areAnimationsOn() && UiUtils.areTransitionsOn()) {
                            folder.setSharedElementEnterTransition(new SharedElementTransition(excluded));
                            folder.setEnterTransition(new FragmentTransition(true, excluded));

                            folderList.setExitTransition(new FragmentTransition(false, excluded));
                            folderList.setReenterTransition(new FragmentTransition(true));
                        } else {
                            folder.setSharedElementEnterTransition(null);
                            folder.setEnterTransition(null);

                            folderList.setExitTransition(null);
                            folderList.setReenterTransition(null);
                        }

                        folder.setSharedElementReturnTransition(null);

                        transaction.setReorderingAllowed(true);
                        transaction.addToBackStack(Categories.FOLDER_STACK_ID);

                        transaction.hide(folderList)
                                .add(R.id.categories, folder);
                        transaction.commit();

                        folder.updateCategory(category.identifier);
                    }
                }
            }
        };

        viewHolder.itemView.setOnClickListener(clickListener);
        viewHolder.recycler.setOnClickListener(clickListener);

        viewHolder.updateCategory(category);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        categoryManager.addOnCategoryUpdateListener(listener);

        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        categoryManager.removeOnCategoryUpdateListener(listener);
        try {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(notificationDotsReceiver);
        } catch (Exception ignored) {
        }
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    protected int getLayout(int viewType) {
        return R.layout.folder;
    }

    @Override
    protected Supplier<ViewHolder> getHolderSupplier(int viewType) {
        return ViewHolder::new;
    }

    @Override
    public long getItemId(int position) {
        return categoryManager.get(position).identifier.hashCode();
    }

    @Override
    public int getTotalItemCount() {
        return categoryManager.size();
    }
}