package com.ahmetgezici.covid19app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmetgezici.covid19app.Adapter.WorldRecyclerAdapter;
import com.ahmetgezici.covid19app.Class.CountryAndFlag;
import com.ahmetgezici.covid19app.Class.MenuListFragment;
import com.ahmetgezici.covid19app.Model.CovidData;
import com.ahmetgezici.covid19app.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String TAG = "aaa";


    WebView webView;

    Timer completeTimer;

    LinearSmoothScroller linearSmoothScroller;

    static boolean aBoolean = true, bBoolean = true;

    boolean completeDataOK, completeDataOK2;

    boolean collapseBool = false;

    int layoutHeight = 0;
    int toplam = 0;

    ActivityMainBinding binding;

    @SuppressLint({"SetTextI18n", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ///////////////////////////////////////////////

        binding.flowingDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);

        Toolbar toolbar = binding.appToolbarr;
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.flowingDrawer.toggleMenu();
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        MenuListFragment menuFragment = (MenuListFragment) fm.findFragmentById(R.id.flowingMenuLayout);
        if (menuFragment == null) {
            menuFragment = new MenuListFragment();
            fm.beginTransaction().add(R.id.flowingMenuLayout, menuFragment).commit();
        }

        /////////////////////////////////////////////////////////////

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

        if (!isInternetAvailable()) {
            Toast.makeText(this, "İnternet Yok", Toast.LENGTH_SHORT).show();
        } else {
            aBoolean = true;
            bBoolean = true;

            completeDataOK = false;
            completeDataOK2 = false;

            completeTimer = new Timer();
            completeTimer.scheduleAtFixedRate(new TimerTask() {

                int timeOut = 0;

                @Override
                public void run() {
                    Log.e(TAG, String.valueOf(timeOut) + " - " + completeDataOK + " " + completeDataOK2);

                    if (completeDataOK && completeDataOK2) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.repeatButton.setVisibility(View.VISIBLE);
                                binding.repeatText.setText("Yenile");

                                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                                alphaAnimation.setDuration(500);

                                binding.loadingView.startAnimation(alphaAnimation);

                                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {

                                        binding.loadingView.setVisibility(View.GONE);
                                        binding.wordRecyclerView.setVisibility(View.VISIBLE);

                                        completeTimer.cancel();
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }
                                });

                            }
                        });
                    } else {
                        if (timeOut >= 60) {

                            Log.e(TAG, "Zaman Aşımı Tekrar Dene");
                            completeTimer.cancel();
                            completeTimer.purge();
                            System.exit(0);
                        }
                    }

                    timeOut++;
                }
            }, 1000, 1000);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
            } else {
                CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
                cookieSyncMngr.startSync();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                cookieManager.removeSessionCookie();
                cookieSyncMngr.stopSync();
                cookieSyncMngr.sync();
            }

            Locale locale = new Locale("tr-TR");
            Configuration config = new Configuration(getResources().getConfiguration());
            Locale.setDefault(locale);
            config.setLocale(locale);

            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());

            ////////////////////////////////////////////////////////////////////////////////////////////

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://covid19.saglik.gov.tr/")
                    .build();

            try (Response response = client.newCall(request).execute()) {

                String body = response.body().string();
                String temp = body.substring(body.indexOf("var sondurumjson") + 20);
                String jsonData = temp.substring(0, temp.lastIndexOf(";") - 1);

                Gson gson = new Gson();
                CovidData covidData = gson.fromJson(jsonData, CovidData.class);

                binding.mCase.setText(covidData.toplamHasta);
                binding.caseDaily.setText("(+" + covidData.gunlukVaka + ")");
                binding.death.setText(covidData.toplamVefat);
                binding.deathDaily.setText("(+" + covidData.gunlukVefat + ")");
                binding.recov.setText(covidData.toplamIyilesen);
                binding.recovDaily.setText("(+" + covidData.gunlukIyilesen + ")");
                binding.testDaily.setText(covidData.gunlukTest);
                binding.criticalPatient.setText(covidData.agirHastaSayisi);
                binding.patientDaily.setText(covidData.gunlukHasta);


                String[] months = new String[]{"OCAK", "ŞUBAT", "MART", "NİSAN", "MAYIS", "HAZİRAN", "TEMMUZ", "AĞUSTOS", "EYLÜL", "EKİM", "KASIM", "ARALIK"};
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", new Locale("tr", "TR"));
                    Date date = dateFormat.parse(covidData.tarih);
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);

                    Log.e(TAG, c.get(Calendar.DAY_OF_MONTH) + " " + months[c.get(Calendar.MONTH)]);

                    binding.date.setText("  |  " + c.get(Calendar.DAY_OF_MONTH) + " " + months[c.get(Calendar.MONTH)] + " " + c.get(Calendar.YEAR));
                } catch (ParseException e) {
                    Log.e(TAG, e.getMessage());

                    binding.date.setText("  |  " + covidData.tarih);
                }

                completeDataOK = true;

            } catch (IOException e) {
                e.printStackTrace();
            }

            ////////////////////////////////////////////////////////////////////////////////////////////

            webView = new WebView(this);
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
                                "var sayi = 229;\n" +
                                "var doc = document.getElementsByClassName('wikitable plainrowheaders sortable')[0].getElementsByTagName('tr');\n" +
                                "for (var i=0; i<doc.length; i++) {\n" +
                                "if(doc[i].className=='sortbottom'){\n" +
                                "sayi=i;\n" +
                                "break;\n" +
                                "}\n" +
                                "}\n" +
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
                                    final String[] countries = data[0].split(";");
                                    final String[] cases = data[1].split(";");
                                    final String[] deaths = data[2].split(";");
                                    final String[] recovs = data[3].split(";");

                                    new CountryAndFlag();

                                    for (int i = 0; i < countries.length; i++) {
                                        countries[i] = CountryAndFlag.countrySearch(countries[i]);
                                    }

                                    LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(binding.wordRecyclerView.getContext(), R.anim.anim_falldown);

                                    WorldRecyclerAdapter worldRecyclerViewAdapter = new WorldRecyclerAdapter(countries, cases, deaths, recovs);
                                    binding.wordRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                    binding.wordRecyclerView.setAdapter(worldRecyclerViewAdapter);

                                    binding.wordRecyclerView.setLayoutAnimation(layoutAnimationController);
                                    binding.wordRecyclerView.getAdapter().notifyDataSetChanged();
                                    binding.wordRecyclerView.scheduleLayoutAnimation();

                                    completeDataOK2 = true;
                                }
                            }
                        });
                    }
                }
            });

            binding.wordRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                int dp = 0;

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    toplam += dy;
                    dp = (int) (toplam / ((float) getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));

                    if (dp > 500 & aBoolean) {
                        collapse(binding.turkeyLayout, 350, 0);
                        binding.backToTop.show();
                        collapseBool = true;
                    } else if (dp <= 500 & bBoolean) {
                        expand(binding.turkeyLayout, 350, layoutHeight);
                        binding.backToTop.hide();
                        collapseBool = false;
                    }
                }
            });


            linearSmoothScroller = new LinearSmoothScroller(binding.wordRecyclerView.getContext()) {
                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return (float) 0.15;
                }
            };

            binding.backToTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    linearSmoothScroller.setTargetPosition(0);
                    binding.wordRecyclerView.getLayoutManager().startSmoothScroll(linearSmoothScroller);
                }
            });

            binding.turkeyLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                boolean aaa = true;

                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (aaa) {
                        aaa = false;

                        layoutHeight = binding.turkeyLayout.getMeasuredHeight();
                    }
                }
            });

            binding.repeatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);

                }
            });
        }
    }

    public static void expand(final View view, int duration, int targetHeight) {

        int prevHeight = view.getHeight();

        view.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.getLayoutParams().height = (int) animation.getAnimatedValue();
                view.requestLayout();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                bBoolean = true;
            }

            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                bBoolean = false;
            }
        });
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    public static void collapse(final View v, int duration, int targetHeight) {
        int prevHeight = v.getHeight();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                aBoolean = true;
            }

            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                aBoolean = false;
            }
        });

        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAndRemoveTask();
    }
}

