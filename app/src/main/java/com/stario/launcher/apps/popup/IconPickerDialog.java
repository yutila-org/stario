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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.stario.launcher.R;
import com.stario.launcher.apps.IconPackManager;
import com.stario.launcher.apps.LauncherApplication;
import com.stario.launcher.themes.ThemedActivity;
import com.stario.launcher.ui.dialogs.ActionDialog;

import java.util.ArrayList;
import java.util.List;

public class IconPickerDialog extends ActionDialog {
    private final LauncherApplication application;
    private final IconPackManager manager;
    private final Runnable dismissCallback;
    private IconPickerPagerAdapter pagerAdapter;
    private EditText searchField;

    public IconPickerDialog(@NonNull ThemedActivity activity, LauncherApplication application,
                            Runnable dismissCallback) {
        super(activity);

        this.application = application;
        this.manager = IconPackManager.from(activity);
        this.dismissCallback = dismissCallback;
    }

    void onIconSelected(IconPackManager.IconPack pack, String drawableName) {
        manager.setIconPackPreference(application.getInfo().packageName, pack, drawableName);
        dismiss();

        if (dismissCallback != null) {
            dismissCallback.run();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    protected View inflateContent(LayoutInflater inflater) {
        View root = inflater.inflate(R.layout.pop_up_icon_picker, null);

        ViewPager pager = root.findViewById(R.id.pager);
        SmartTabLayout tabLayout = root.findViewById(R.id.tabs);
        searchField = root.findViewById(R.id.search);

        List<IconPackManager.IconPack> packs = new ArrayList<>();
        for (int i = 0; i < manager.getCount(); i++) {
            packs.add(manager.getPack(i));
        }

        pagerAdapter = new IconPickerPagerAdapter(activity, packs, this::onIconSelected);
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                searchField.setText("");
            }
        });

        tabLayout.setViewPager(pager);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pagerAdapter != null) {
                    pagerAdapter.filter(pager.getCurrentItem(),
                            s != null ? s.toString() : "");
                }
            }
        });

        pager.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    getBehavior().setDraggable(true);
                    break;
                default:
                    getBehavior().setDraggable(false);
                    break;
            }
            return false;
        });

        return root;
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
