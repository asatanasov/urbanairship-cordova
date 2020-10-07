/*
 Copyright 2009-2017 Urban Airship Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.urbanairship.cordova;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.adobe.phonegap.push.PushPlugin;
import com.urbanairship.AirshipReceiver;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushMessage;

import java.util.List;
/**
 * Intent receiver for Urban Airship channel and push events.
 */
public class CordovaAirshipReceiver extends AirshipReceiver {

    private static final String TAG = "CordovaAirshipReceiver";

    @Override
    protected void onChannelCreated(Context context, String channelId) {
        Log.i(TAG, "Channel created. Channel ID: " + channelId + ".");
        PluginManager.shared(context).channelUpdated(channelId, true);
        PluginManager.shared(context).checkOptInStatus();
    }

    @Override
    protected void onChannelUpdated(Context context, String channelId) {
        Log.i(TAG, "Channel updated. Channel ID: " + channelId + ".");
        PluginManager.shared(context).channelUpdated(channelId, true);
        PluginManager.shared(context).checkOptInStatus();
    }

    @Override
    protected void onChannelRegistrationFailed(Context context) {
        Log.i(TAG, "Channel registration failed.");
        PluginManager.shared(context).channelUpdated(UAirship.shared().getPushManager().getChannelId(), false);
    }

    @Override
    protected void onPushReceived(@NonNull Context context, @NonNull PushMessage message, boolean notificationPosted) {
        Log.i(TAG, "Received push message. Alert: " + message.getAlert() + ". posted notification: " + notificationPosted);

        boolean isForeground = PushPlugin.isInForeground();

        if (!notificationPosted) {
            PluginManager.shared(context).pushReceived(null, message, isForeground);
        }
    }

    @Override
    protected void onNotificationPosted(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        Log.i(TAG, "Notification posted. Alert: " + notificationInfo.getMessage().getAlert() + ". NotificationId: " + notificationInfo.getNotificationId());

        boolean isForeground = PushPlugin.isInForeground();

        PluginManager.shared(context).pushReceived(notificationInfo.getNotificationId(), notificationInfo.getMessage(), isForeground);
    }

    @Override
    protected boolean onNotificationOpened(@NonNull Context context, @NonNull NotificationInfo notificationInfo) {
        Log.i(TAG, "Notification opened. Alert: " + notificationInfo.getMessage().getAlert() + ". NotificationId: " + notificationInfo.getNotificationId());

        PluginManager.shared(context).notificationOpened(notificationInfo);

        return shouldLaunchActivity(context);
    }

    @Override
    protected boolean onNotificationOpened(@NonNull Context context, @NonNull NotificationInfo notificationInfo, @NonNull ActionButtonInfo actionButtonInfo) {
        Log.i(TAG, "Notification action button opened. Button ID: " + actionButtonInfo.getButtonId() + ". NotificationId: " + notificationInfo.getNotificationId());

        PluginManager.shared(context).notificationOpened(notificationInfo, actionButtonInfo);

        return shouldLaunchActivity(context);
    }

    private boolean shouldLaunchActivity(Context context)
    {
        if(isAppOnForeground(context)) {
            // Return true here to allow Urban Airship not to auto launch the launcher activity
            return true;
        }
        else {
            // Return false here to allow Urban Airship to auto launch the launcher activity
            return false;
        }
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
