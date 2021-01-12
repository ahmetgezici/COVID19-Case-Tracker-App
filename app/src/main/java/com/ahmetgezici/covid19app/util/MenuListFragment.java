package com.ahmetgezici.covid19app.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ahmetgezici.covid19app.R;
import com.google.android.material.navigation.NavigationView;

public class MenuListFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container,
                false);
        final NavigationView vNavigation = view.findViewById(R.id.vNavigation);
        vNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if (vNavigation.getMenu().getItem(0).getItemId() == menuItem.getItemId()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://covid19.saglik.gov.tr/"));
                    startActivity(intent);
                } else if (vNavigation.getMenu().getItem(1).getItemId() == menuItem.getItemId()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/2019%e2%80%9320_coronavirus_pandemic_by_country_and_territory"));
                    startActivity(intent);
                }
                return false;
            }
        });

        FrameLayout frameLayout = view.findViewById(R.id.github);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ahmetgezici"));
                startActivity(intent);
            }
        });
        return view;
    }
}
