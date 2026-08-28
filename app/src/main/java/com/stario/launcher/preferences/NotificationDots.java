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

package com.stario.launcher.preferences;

public final class NotificationDots {
    public static final String NOTIFICATION_DOTS_ENABLED = "notification_dots_enabled";
    public static final String NOTIFICATION_DOTS_SHOW_COUNT = "notification_dots_show_count";
    public static final String NOTIFICATION_DOTS_COLOR = "notification_dots_color";

    public static final int DEFAULT_COLOR = 0xFFE53935; // Material Red 600

    public static final String INTENT_NOTIFICATION_DOTS_CHANGED = "com.stario.NotificationDots.CHANGED";

    private NotificationDots() {}
}
