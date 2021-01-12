package com.ahmetgezici.covid19app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmetgezici.covid19app.R;
import com.ahmetgezici.covid19app.util.CountryAndFlag;


public class WorldRecyclerAdapter extends RecyclerView.Adapter<WorldRecyclerAdapter.ViewHolder> {

    private final String[] countries;
    private final String[] cases;
    private final String[] deaths;
    private final String[] recovs;

    public WorldRecyclerAdapter(String[] countries, String[] cases, String[] deaths, String[] recovs) {

        this.countries = countries;
        this.cases = cases;
        this.deaths = deaths;
        this.recovs = recovs;

        System.gc();

        new CountryAndFlag();
    }

    @NonNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.covid_widget_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.countryImage.setImageResource(CountryAndFlag.flagSearch(countries[position]));
        holder.countryName.setText(countries[position]);
        holder.countryCase.setText(cases[position]);
        holder.countryDeath.setText(deaths[position]);
        holder.countryRecov.setText(recovs[position]);
    }

    @Override
    public int getItemCount() {
        return countries.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView countryImage;
        TextView countryName, countryCase, countryDeath, countryRecov;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            countryImage = itemView.findViewById(R.id.countryImage);
            countryName = itemView.findViewById(R.id.countryName);
            countryDeath = itemView.findViewById(R.id.countryDeath);
            countryCase = itemView.findViewById(R.id.countryCase);
            countryRecov = itemView.findViewById(R.id.countryRecov);

        }
    }
}
