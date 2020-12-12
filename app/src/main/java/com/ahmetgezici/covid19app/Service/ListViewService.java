package com.ahmetgezici.covid19app.Service;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ahmetgezici.covid19app.Class.CountryAndFlag;
import com.ahmetgezici.covid19app.R;


public class ListViewService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new ListViewItemFactory(getApplicationContext(), intent);

    }

    static class ListViewItemFactory implements RemoteViewsFactory {

        private final Context context;
        String[] countries;
        String[] cases;
        String[] deaths;
        String[] recovs;

        ListViewItemFactory(Context context, Intent intent) {

            this.context = context;
            this.countries = intent.getStringArrayExtra("countries");
            this.cases = intent.getStringArrayExtra("cases");
            this.deaths = intent.getStringArrayExtra("deaths");
            this.recovs = intent.getStringArrayExtra("recovs");

            System.gc();

            new CountryAndFlag();
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return 50;
        }

        @Override
        public RemoteViews getViewAt(int position) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.covid_widget_listitem);
            views.setImageViewResource(R.id.countryImage, CountryAndFlag.flagSearch(countries[position]));
            views.setTextViewText(R.id.countryName, countries[position]);
            views.setTextViewText(R.id.countryDeath, cases[position]);
            views.setTextViewText(R.id.countryCase, deaths[position]);
            views.setTextViewText(R.id.countryRecov, recovs[position]);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}