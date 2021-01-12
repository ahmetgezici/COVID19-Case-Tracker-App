package com.ahmetgezici.covid19app.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.ahmetgezici.covid19app.R;
import com.ahmetgezici.covid19app.model.CovidData;
import com.ahmetgezici.covid19app.util.CountryAndFlag;
import com.ahmetgezici.covid19app.view.CovidWidget;
import com.ahmetgezici.covid19app.view.MainActivity;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ForeGroundService extends Service {

    private final static String TAG = "aaa";


    static Timer timer, completeTimer;

    static WebView webView;

    static Handler handler;
    static Runnable runnable, handlerRunnable;

    private ServiceHandler serviceHandler;

    boolean completeDataOK, completeDataOK2;


    private final class ServiceHandler extends Handler {

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {

            Locale locale = new Locale("tr-TR");
            Configuration config = new Configuration(getResources().getConfiguration());
            Locale.setDefault(locale);
            config.setLocale(locale);

            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

            final Notification notification = getNotification("Veriler Alınıyor");
            startForeground(msg.arg1, notification);

            timer = new Timer();
            completeTimer = new Timer();

            final Intent foregroundIntent = new Intent(getApplicationContext(), ForeGroundService.class);

            final RemoteViews views = new RemoteViews(getPackageName(), R.layout.covid_widget);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                views.setOnClickPendingIntent(R.id.repeatLayout, PendingIntent.getForegroundService(getApplicationContext(), 0, foregroundIntent, 0));
                views.setOnClickPendingIntent(R.id.repeatButton, PendingIntent.getForegroundService(getApplicationContext(), 0, foregroundIntent, 0));

            } else {

                views.setOnClickPendingIntent(R.id.repeatLayout, PendingIntent.getService(getApplicationContext(), 0, foregroundIntent, 0));
                views.setOnClickPendingIntent(R.id.repeatButton, PendingIntent.getService(getApplicationContext(), 0, foregroundIntent, 0));

            }
            views.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), 0));

            //////////////////////////////////////////////////////////////////////////////////

            timer = new Timer();
            timer.schedule(new TimerTask() {

                int timerCounter = 0;

                @Override
                public void run() {

                    try {

                        OkHttpClient okHttpClient = new OkHttpClient();
                        Response response = okHttpClient.newCall(
                                new Request.Builder()
                                        .url("https://covid19.saglik.gov.tr/")
                                        .build()).execute();

                        if (response.code() == 200) {

                            getAllData(ForeGroundService.this);
                            timer.cancel();
                        }

                    } catch (Exception e) {

                        if (timerCounter >= 4) {

                            views.setViewVisibility(R.id.progressBar, View.GONE);
                            views.setViewVisibility(R.id.repeatButton, View.VISIBLE);
                            views.setTextViewText(R.id.repeatText, "Bağlantı Sorunu");

                            timer.cancel();

                            CovidWidget.control = false;
                            stopService(foregroundIntent);
                        }

                        Log.e(TAG, "İnternet Yok");

                        views.setTextViewText(R.id.updateState, getCurrentDate() + " - İnternet Bağlantısı Yok");

                        ComponentName theWidget = new ComponentName(ForeGroundService.this, CovidWidget.class);
                        AppWidgetManager manager = AppWidgetManager.getInstance(ForeGroundService.this);
                        manager.updateAppWidget(theWidget, views);
                    }

                    timerCounter++;
                }
            }, 0, 15 * 1000);
        }
    }

    final Handler mHandler = new Handler();

    @Override
    public void onCreate() {

        final Intent foregroundIntent = new Intent(getApplicationContext(), ForeGroundService.class);

        HandlerThread thread = new HandlerThread(" ", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        Looper serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        final RemoteViews view = new RemoteViews(getPackageName(), R.layout.covid_widget);

        view.setViewVisibility(R.id.progressBar, View.VISIBLE);
        view.setViewVisibility(R.id.repeatButton, View.GONE);
        view.setTextViewText(R.id.repeatText, "Yükleniyor..");

        ComponentName theWidget = new ComponentName(ForeGroundService.this, CovidWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(ForeGroundService.this);
        manager.updateAppWidget(theWidget, view);

        handler = new Handler(Looper.getMainLooper());

        handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timer != null && completeTimer != null) {

                    mHandler.removeCallbacks(runnable);

                    final RemoteViews views = new RemoteViews(getPackageName(), R.layout.covid_widget);

                    views.setViewVisibility(R.id.progressBar, View.GONE);
                    views.setViewVisibility(R.id.repeatButton, View.VISIBLE);
                    views.setTextViewText(R.id.repeatText, "Bağlantı Sorunu");
                    views.setTextViewText(R.id.updateState, getCurrentDate() + " - İnternet Bağlantısı Yok");

                    ComponentName theWidget = new ComponentName(ForeGroundService.this, CovidWidget.class);
                    AppWidgetManager manager = AppWidgetManager.getInstance(ForeGroundService.this);
                    manager.updateAppWidget(theWidget, views);

                    timer.cancel();
                    completeTimer.cancel();

                    CovidWidget.control = false;

                    getApplicationContext().stopService(foregroundIntent);
                }
            }
        };

        handler.postDelayed(handlerRunnable, 3 * 60 * 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!CovidWidget.control) {

            CovidWidget.control = true;

            Toast.makeText(this, "Service Başladı", Toast.LENGTH_SHORT).show();

            Message msg = serviceHandler.obtainMessage();
            msg.arg1 = startId;

            if (intent != null) {

                msg.setData(intent.getExtras());
                serviceHandler.sendMessage(msg);

            } else {

                Toast.makeText(ForeGroundService.this, "Intent null", Toast.LENGTH_SHORT).show();

                final Intent foregroundIntent = new Intent(getApplicationContext(), ForeGroundService.class);

                final RemoteViews views = new RemoteViews(getPackageName(), R.layout.covid_widget);

                views.setViewVisibility(R.id.progressBar, View.GONE);
                views.setViewVisibility(R.id.repeatButton, View.VISIBLE);
                views.setTextViewText(R.id.repeatText, "Yenile");
                views.setTextViewText(R.id.updateState, getCurrentDate());

                ComponentName theWidget = new ComponentName(ForeGroundService.this, CovidWidget.class);
                AppWidgetManager manager = AppWidgetManager.getInstance(ForeGroundService.this);
                manager.updateAppWidget(theWidget, views);

                CovidWidget.control = false;

                getApplicationContext().stopService(foregroundIntent);
            }

        } else {
            Log.e(TAG, "Timera girmedi");
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        Toast.makeText(this, "Service Bitti", Toast.LENGTH_SHORT).show();

        if (timer != null && completeTimer != null) {
            handler.removeCallbacks(handlerRunnable);
            timer.cancel();
            completeTimer.cancel();

            System.gc();
        }

    }

    private void getAllData(final Context context) {

        runnable = new Runnable() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void run() {

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                completeDataOK = false;
                completeDataOK2 = false;

                final Intent foregroundIntent = new Intent(context, ForeGroundService.class);

                final ComponentName theWidget = new ComponentName(ForeGroundService.this, CovidWidget.class);
                final AppWidgetManager manager = AppWidgetManager.getInstance(ForeGroundService.this);

                final RemoteViews views = new RemoteViews(getPackageName(), R.layout.covid_widget);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    views.setOnClickPendingIntent(R.id.repeatLayout, PendingIntent.getForegroundService(context, 0, foregroundIntent, 0));
                    views.setOnClickPendingIntent(R.id.repeatButton, PendingIntent.getForegroundService(context, 0, foregroundIntent, 0));

                } else {
                    views.setOnClickPendingIntent(R.id.repeatLayout, PendingIntent.getService(context, 0, foregroundIntent, 0));
                    views.setOnClickPendingIntent(R.id.repeatButton, PendingIntent.getService(context, 0, foregroundIntent, 0));
                }
                views.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), 0));

                completeTimer = new Timer();
                completeTimer.scheduleAtFixedRate(new TimerTask() {

                    int timeOut = 0;

                    @Override
                    public void run() {
                        Log.e(TAG, timeOut + " - " + completeDataOK + " " + completeDataOK2);

                        if (completeDataOK && completeDataOK2) {

                            views.setViewVisibility(R.id.progressBar, View.GONE);
                            views.setViewVisibility(R.id.repeatButton, View.VISIBLE);
                            views.setTextViewText(R.id.repeatText, "Yenile");

                            views.setTextViewText(R.id.updateState, "Son güncelleme: " + getCurrentDate());

                            manager.updateAppWidget(theWidget, views);

                            completeTimer.cancel();
                            CovidWidget.control = false;
                            stopService(foregroundIntent);

                        } else {

                            if (timeOut >= 60) {

                                Log.e(TAG, String.valueOf(timeOut));

                                views.setViewVisibility(R.id.progressBar, View.GONE);
                                views.setViewVisibility(R.id.repeatButton, View.VISIBLE);
                                views.setTextViewText(R.id.repeatText, "Zaman Aşımı Tekrar Dene");

                                views.setTextViewText(R.id.updateState, "Son güncelleme: " + getCurrentDate() + " - Zaman Aşımı");

                                manager.updateAppWidget(theWidget, views);

                                completeTimer.cancel();
                                CovidWidget.control = false;

                                stopService(foregroundIntent);
                            }
                        }

                        timeOut++;
                    }
                }, 0, 1000);


                /////////////////////////////////////////////////////////////////////


                DecimalFormat decimalFormat = new DecimalFormat("#.#");

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                        .addHeader("Accept-Language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7")
                        .addHeader("Cache-Control", "max-age=0")
                        .addHeader("Connection", "keep-alive")
                        .addHeader("Cookie", "_ga=GA1.3.1473542917.1603394605; _gid=GA1.3.477400369.1603484133; _gat=1")
                        .addHeader("Host", "covid19.saglik.gov.tr")
                        .addHeader("Sec-Fetch-Dest", "document")
                        .addHeader("Sec-Fetch-Mode", "navigate")
                        .addHeader("Sec-Fetch-Site", "none")
                        .addHeader("Sec-Fetch-User", "?1")
                        .addHeader("Upgrade-Insecure-Requests", "1")
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36")
                        .url("https://covid19.saglik.gov.tr/")
                        .build();

                String jsonData;

                try (Response response = client.newCall(request).execute()) {

                    String body = response.body().string();
                    String temp = body.substring(body.indexOf("var sondurumjson") + 20);
                    jsonData = temp.substring(0, temp.lastIndexOf(";") - 1);

//                    Log.e(TAG, jsonData);

                    Gson gson = new Gson();
                    CovidData covidData = gson.fromJson(jsonData, CovidData.class);

                    int deaths = Integer.parseInt(covidData.toplamVefat.replace(".", ""));
                    float cases = Float.parseFloat(covidData.toplamHasta.replace(".", ""));
                    int recovs = Integer.parseInt(covidData.toplamIyilesen.replace(".", ""));

                    float deathPercent = (deaths * 100) / cases;
                    float casePercent = ((cases - (recovs + deaths)) * 100) / cases;
                    float recovPercent = (recovs * 100) / cases;


                    views.setTextViewText(R.id.death, covidData.toplamVefat);
                    views.setTextViewText(R.id.deathPercent, "(%" + decimalFormat.format(deathPercent) + ")");
                    views.setTextViewText(R.id.deathDaily, "(+" + covidData.gunlukVefat + ")");

                    views.setTextViewText(R.id.mCase, covidData.toplamHasta);
                    views.setTextViewText(R.id.casePercent, "(%" + decimalFormat.format(casePercent) + ")");
                    views.setTextViewText(R.id.caseDaily, "(+" + covidData.gunlukVaka + ")");

                    views.setTextViewText(R.id.recov, covidData.toplamIyilesen);
                    views.setTextViewText(R.id.recovPercent, "(%" + decimalFormat.format(recovPercent) + ")");
                    views.setTextViewText(R.id.recovDaily, "(+" + covidData.gunlukIyilesen + ")");


                    String[] months = new String[]{"OCAK", "ŞUBAT", "MART", "NİSAN", "MAYIS", "HAZİRAN", "TEMMUZ", "AĞUSTOS", "EYLÜL", "EKİM", "KASIM", "ARALIK"};

                    try {

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", new Locale("tr", "TR"));
                        Date date = dateFormat.parse(covidData.tarih);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);

                        views.setTextViewText(R.id.date, "  |  " + calendar.get(Calendar.DAY_OF_MONTH) + " " + months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));

                    } catch (ParseException e) {

                        Log.e(TAG, e.getMessage());

                        views.setTextViewText(R.id.date, "  |  " + covidData.tarih);
                    }

                    manager.updateAppWidget(theWidget, views);

                    completeDataOK = true;

                } catch (IOException e) {
                    Log.e(TAG, "HATA" + e.getMessage());
                }

                ////////////////////////////////////////////////////////////////////////////////////////////////////////

                webView = new WebView(context);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.clearHistory();
                webView.clearFormData();
                webView.clearCache(true);
                webView.getSettings().setSaveFormData(false);

                webView.loadUrl("https://en.m.wikipedia.org/wiki/COVID-19_pandemic_by_country_and_territory");

                webView.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);

                        if (url.equals("https://en.m.wikipedia.org/wiki/COVID-19_pandemic_by_country_and_territory")) {

                            webView.evaluateJavascript("var toplam = '';\n" +
                                    "var sayi = 52;\n" +
                                    "var doc = document.getElementsByClassName('wikitable plainrowheaders sortable')[0].getElementsByTagName('tr');\n" +
                                    "for (var i=2; i<sayi; i++) {\n" +
                                    "toplam += doc[i].getElementsByTagName('th')[1].getElementsByTagName('a')[0].innerText+';';\n" +
                                    "}\n" +
                                    "toplam += '#';\n" +
                                    "for (var i=2; i<sayi; i++) {\n" +
                                    "toplam += doc[i].getElementsByTagName('td')[0].innerText+';';\n" +
                                    "}\n" +
                                    "toplam += '#';\n" +
                                    "for (var i=2; i<sayi; i++) {\n" +
                                    "toplam += doc[i].getElementsByTagName('td')[1].innerText+';';\n" +
                                    "}\n" +
                                    "toplam += '#';\n" +
                                    "for (var i=2; i<sayi; i++) {\n" +
                                    "toplam += doc[i].getElementsByTagName('td')[2].innerText+';';\n" +
                                    "}\n" +
                                    "toplam;", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(final String value) {

                                    if (!value.equals("null")) {

                                        String[] data = value.replace("\"", "").replace("\\n", "").split("#");
                                        String[] countries = data[0].split(";");
                                        String[] deaths = data[1].split(";");
                                        String[] cases = data[2].split(";");
                                        String[] recovs = data[3].split(";");

                                        new CountryAndFlag();

                                        for (int i = 0; i < countries.length; i++) {
                                            countries[i] = CountryAndFlag.countrySearch(countries[i]);
                                        }

                                        int[] appWidgetIds = manager.getAppWidgetIds(theWidget);

                                        Intent serviceIntent = new Intent(getApplicationContext(), ListViewService.class);
                                        serviceIntent.removeExtra(AppWidgetManager.EXTRA_APPWIDGET_ID);
                                        serviceIntent.removeExtra("countries");
                                        serviceIntent.removeExtra("cases");
                                        serviceIntent.removeExtra("deaths");
                                        serviceIntent.removeExtra("recovs");

                                        for (int appWidgetId : appWidgetIds) {

                                            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                                            serviceIntent.putExtra("countries", countries);
                                            serviceIntent.putExtra("cases", cases);
                                            serviceIntent.putExtra("deaths", deaths);
                                            serviceIntent.putExtra("recovs", recovs);

                                            Random random = new Random();
                                            serviceIntent.setType(String.valueOf(random.nextInt(10000)));
                                            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

                                            RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.covid_widget);

                                            views.setRemoteAdapter(R.id.listView, serviceIntent);
                                            views.setEmptyView(R.id.listView, R.id.listView);

                                            manager.updateAppWidget(appWidgetId, views);
                                        }
                                        stopService(serviceIntent);

                                        completeDataOK2 = true;
                                    }
                                }
                            });
                        }
                    }
                });
            }
        };

        mHandler.post(runnable);

    }

    public Notification getNotification(String message) {

        return new NotificationCompat.Builder(getApplicationContext(), "test_channel_01")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setChannelId("test_channel_01")
                .setContentTitle("Covid19 Canlı")
                .setContentText(message)
                .build();
    }

    static private String getCurrentDate() {
        Date d = new Date();
        CharSequence s = DateFormat.format("dd/MM/yyyy kk:mm:ss", d.getTime());
        return (String) s;
    }
}




