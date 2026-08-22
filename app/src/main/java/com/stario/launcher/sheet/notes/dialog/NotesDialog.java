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

package com.stario.launcher.sheet.notes.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stario.launcher.R;
import com.stario.launcher.preferences.Entry;
import com.stario.launcher.sheet.SheetDialogFragment;
import com.stario.launcher.sheet.SheetType;
import com.stario.launcher.themes.ThemedActivity;
import com.stario.launcher.ui.Measurements;
import com.stario.launcher.ui.common.FadingEdgeLayout;

public class NotesDialog extends SheetDialogFragment {
    private static final String CONTENT_KEY = "content";
    private static final long SAVE_DELAY_MS = 300;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingSave;
    private SharedPreferences store;
    private ThemedActivity activity;
    private EditText noteContent;

    public NotesDialog() {
        super();
    }

    public NotesDialog(SheetType type) {
        super(type);
    }

    public static String getName() {
        return "Notes";
    }

    @Override
    public boolean requiresEagerInitialization() {
        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        activity = (ThemedActivity) context;
        store = activity.getApplicationContext()
                .getSharedPreferences(Entry.NOTES);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.notes, container, false);

        noteContent = root.findViewById(R.id.note_content);
        FadingEdgeLayout fader = root.findViewById(R.id.fader);

        String saved = store.getString(CONTENT_KEY, "");
        noteContent.setText(saved);
        noteContent.setSelection(saved.length());

        noteContent.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    int start = Math.max(noteContent.getSelectionStart(), 0);
                    int end = Math.max(noteContent.getSelectionEnd(), 0);
                    noteContent.getText().replace(Math.min(start, end),
                            Math.max(start, end), "\n", 0, 1);
                    return true;
                }
            }
            return false;
        });

        noteContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                scheduleSave();
            }
        });

        setOnBackPressed(() -> {
            hide(true);

            return false;
        });

        Measurements.addStatusBarListener(value -> {
            fader.setFadeSizes(value +
                            (Measurements.isLandscape() ? 0 : Measurements.getDefaultPadding()),
                    0, Measurements.getNavHeight() + Measurements.getDefaultPadding(), 0);

            root.setPadding(0, value, 0, 0);
        });

        Measurements.addNavListener(value -> {
            fader.setFadeSizes(Measurements.getSysUIHeight() +
                            (Measurements.isLandscape() ? 0 : Measurements.getDefaultPadding()),
                    0, value + Measurements.getDefaultPadding(), 0);

            noteContent.setPadding(noteContent.getPaddingLeft(), noteContent.getPaddingTop(),
                    noteContent.getPaddingRight(), value);
        });

        return root;
    }

    private void scheduleSave() {
        if (pendingSave != null) {
            handler.removeCallbacks(pendingSave);
        }

        pendingSave = () -> {
            if (noteContent != null) {
                store.edit()
                        .putString(CONTENT_KEY, noteContent.getText().toString())
                        .apply();
            }

            pendingSave = null;
        };

        handler.postDelayed(pendingSave, SAVE_DELAY_MS);
    }

    private void flushSave() {
        if (pendingSave != null) {
            handler.removeCallbacks(pendingSave);
            pendingSave.run();
        }
    }

    @Override
    public void onStop() {
        flushSave();

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (pendingSave != null) {
            handler.removeCallbacks(pendingSave);
            pendingSave = null;
        }

        noteContent = null;

        super.onDestroyView();
    }
}
