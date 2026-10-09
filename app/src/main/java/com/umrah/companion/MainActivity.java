package com.umrah.companion;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView webView;
    private static final String CHANNEL_ID = "umrah_reminders";
    private static final int NOTIFICATION_PERMISSION_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();
        requestNotificationPermission();

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);

        webView.addJavascriptInterface(
                new ReminderBridge(this),
                "AndroidReminder"
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001
            );
        }

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(
                    String origin,
                    GeolocationPermissions.Callback callback) {

                callback.invoke(origin, true, false);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("http://") ||
                        url.startsWith("https://")) {

                    Intent intent =
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Umrah Reminders",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Umrah Companion reminders and important alerts"
            );

            channel.enableVibration(true);

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    public class ReminderBridge {

        private final Context context;

        ReminderBridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void scheduleReminder(
                String title,
                long triggerAtMillis,
                int reminderId) {

            if (triggerAtMillis <= System.currentTimeMillis()) {
                return;
            }

            Intent intent =
                    new Intent(context, ReminderReceiver.class);

            intent.putExtra("title", title);
            intent.putExtra("reminderId", reminderId);

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            reminderId,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    PendingIntent.FLAG_IMMUTABLE
                    );

            AlarmManager alarmManager =
                    (AlarmManager)
                            context.getSystemService(
                                    Context.ALARM_SERVICE
                            );

            if (alarmManager == null) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        !alarmManager.canScheduleExactAlarms()) {

                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                    );

                } else {

                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                    );
                }

            } else {

                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            }
        }

        @JavascriptInterface
        public void cancelReminder(int reminderId) {

            Intent intent =
                    new Intent(context, ReminderReceiver.class);

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            reminderId,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    PendingIntent.FLAG_IMMUTABLE
                    );

            AlarmManager alarmManager =
                    (AlarmManager)
                            context.getSystemService(
                                    Context.ALARM_SERVICE
                            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }

            pendingIntent.cancel();
        }
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
