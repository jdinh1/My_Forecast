package com.tuturucoding.myforecast.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

import com.tuturucoding.myforecast.R;
import com.tuturucoding.myforecast.adapters.HourAdapter;
import com.tuturucoding.myforecast.weather.Hour;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HourlyForecastActivity extends AppCompatActivity {
    private Hour[] mHours;
    boolean isNight;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_forecast);
        ButterKnife.bind(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.HOURLY_FORECAST);
        mHours = Arrays.copyOf(parcelables, parcelables.length, Hour[].class);
        isNight = intent.getBooleanExtra(MainActivity.NIGHT,isNight);
        changeLayoutCheck();    // check for night time
        HourAdapter adapter = new HourAdapter(this, mHours);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // fixed size hours array
        mRecyclerView.setHasFixedSize(true);
    }

    private void changeLayoutCheck() {
        // Change background if time has passed 4:00PM
        if (isNight) {
            RelativeLayout hourlyLayout = (RelativeLayout) findViewById(R.id.hourlyLayout);
            hourlyLayout.setBackgroundResource(R.drawable.bg_gradient_night);
        }
    }
}
