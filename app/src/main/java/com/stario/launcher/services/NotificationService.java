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

package com.stario.launcher.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;

public class NotificationService extends NotificationListenerService {
    private static final String TAG = "NotificationService";
    public static final String NOTIFICATION_DOTS = "com.stario.NOTIFICATION_DOTS";
    public static final String NOTIFICATIONS_EVENT = "com.stario.launcher.NOTIFICATIONS_LISTENER_EVENT";
    public static final String UPDATE_NOTIFICATIONS = "com.stario.launcher.UPDATE_NOTIFICATIONS";
    public static final String TARGET_NOTIFICATION = "com.stario.launcher.TARGET_NOTIFICATION";
    public static final String NOTIFICATION_COUNT = "com.stario.launcher.NOTIFICATION_COUNT";
    private static NotificationService instance;

    public static NotificationService getInstance() {
        return instance;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        try {
            sendBroadcastForNotification(notification);
        } catch (Exception exception) {
            Log.e(TAG, "onNotificationPosted: " + exception);
        }

        super.onNotificationPosted(notification);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {
        try {
            sendBroadcastForNotification(notification);
        } catch (Exception exception) {
            Log.e(TAG, "onNotificationRemoved: " + exception);
        }

        super.onNotificationRemoved(notification);
    }

    @Override
    public void onListenerConnected() {
        instance = this;

        try {
            Intent intent = new Intent(NOTIFICATIONS_EVENT);
            intent.setPackage(getPackageName());

            intent.putExtra(TARGET_NOTIFICATION,
                    convertToNotificationMap(getActiveNotifications()));
            sendBroadcast(intent);
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(intent);
        } catch (Exception exception) {
            Log.e(TAG, "onListenerConnected: " + exception.getMessage());
        }

        super.onListenerConnected();
    }

    @Override
    public void onListenerDisconnected() {
        if (instance == this) {
            instance = null;
        }
        super.onListenerDisconnected();
    }

    @SuppressWarnings("ConstantConditions")
    public static HashMap<String, Integer> convertToNotificationMap(StatusBarNotification[] notifications) {
        HashMap<String, Integer> notificationMap = new HashMap<>();

        if (notifications != null) {
            for (StatusBarNotification notification : notifications) {
                if (notification != null && notification.getNotification() != null) {
                    String packageName = notification.getPackageName();

                    if (!((notification.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) ==
                            Notification.FLAG_GROUP_SUMMARY)) {
                        notificationMap.put(packageName,
                                notificationMap.getOrDefault(packageName, 0) + 1);
                    }
                }
            }
        }

        return notificationMap;
    }

    @SuppressLint("UnsafeImplicitIntentLaunch")
    private void sendBroadcastForNotification(StatusBarNotification notification) {
        Intent intent = new Intent(UPDATE_NOTIFICATIONS);
        intent.setPackage(getPackageName());

        int count = 0;
        StatusBarNotification[] active = getActiveNotifications();
        if (active != null && notification != null) {
            for (StatusBarNotification statusBarNotification : active) {
                if (statusBarNotification != null && statusBarNotification.getNotification() != null &&
                        statusBarNotification.getPackageName().equals(notification.getPackageName()) &&
                        !((statusBarNotification.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) ==
                                Notification.FLAG_GROUP_SUMMARY)) {
                    count++;
                }
            }
        }

        if (notification != null) {
            intent.putExtra(TARGET_NOTIFICATION, notification.getPackageName());
        }
        intent.putExtra(NOTIFICATION_COUNT, count);

        sendBroadcast(intent);
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }
}
