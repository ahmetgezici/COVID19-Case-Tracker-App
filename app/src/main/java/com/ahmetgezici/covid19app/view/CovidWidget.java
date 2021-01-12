package com.ahmetgezici.covid19app.view;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.widget.RemoteViews;

import com.ahmetgezici.covid19app.R;
import com.ahmetgezici.covid19app.service.ForeGroundService;

public class CovidWidget extends AppWidgetProvider {

    static public boolean control = false;

//    public static int orientation;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        control = false;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

//        orientation = context.getResources().getConfiguration().orientation;
//        Log.e(TAG, String.valueOf(orientation));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.covid_widget);

        createChannel(context);

        Intent foregroundIntent = new Intent(context, ForeGroundService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(foregroundIntent);
            views.setOnClickPendingIntent(R.id.yenile, PendingIntent.getForegroundService(context, 0, foregroundIntent, 0));
            views.setOnClickPendingIntent(R.id.repeatButton, PendingIntent.getForegroundService(context, 0, foregroundIntent, 0));

        } else {
            context.startService(foregroundIntent);
            views.setOnClickPendingIntent(R.id.yenile, PendingIntent.getService(context, 0, foregroundIntent, 0));
            views.setOnClickPendingIntent(R.id.repeatButton, PendingIntent.getService(context, 0, foregroundIntent, 0));
        }
        views.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
        Intent foregroundIntent = new Intent(context, ForeGroundService.class);
        context.stopService(foregroundIntent);
    }

    private static void createChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel mChannel = new NotificationChannel("test_channel_01",
                    "Test Channel 1",
                    NotificationManager.IMPORTANCE_LOW);
            mChannel.enableLights(true);
            mChannel.setShowBadge(true);
            nm.createNotificationChannel(mChannel);
        }
    }
}

