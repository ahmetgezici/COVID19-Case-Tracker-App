package com.ahmetgezici.covid19app.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.ahmetgezici.covid19app.R;
import com.ahmetgezici.covid19app.databinding.ActivityMainBinding;
import com.ahmetgezici.covid19app.util.MenuListFragment;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;

public class MainActivity extends AppCompatActivity {

    String TAG = "aaa";

    ActivityMainBinding binding;

    MasterFragment masterFragment = new MasterFragment();
    FragmentManager manager = getSupportFragmentManager();

    @SuppressLint({"SetTextI18n", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ///////////////////////////////////////////////

        manager.beginTransaction()
                .replace(R.id.layout, masterFragment, masterFragment.getTag())
                .commit();

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


        binding.repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.progressBar.setVisibility(View.VISIBLE);
                binding.repeatButton.setVisibility(View.GONE);
                binding.repeatTxt.setText("Yükleniyor..");

                MasterFragment fragment = new MasterFragment();

                manager.beginTransaction()
                        .replace(R.id.layout, fragment, fragment.getTag())
                        .commit();

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAndRemoveTask();
    }
}

