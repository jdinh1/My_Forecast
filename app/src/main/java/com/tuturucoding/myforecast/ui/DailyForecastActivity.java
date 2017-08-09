package com.tuturucoding.myforecast.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

import com.tuturucoding.myforecast.R;
import com.tuturucoding.myforecast.adapters.DayAdapter;
import com.tuturucoding.myforecast.weather.Day;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DailyForecastActivity extends ListActivity {
    private Day[] mDays;

    @BindView(R.id.locationLabel)
    TextView mLocationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        ButterKnife.bind(this);

        Intent intent = getIntent();

        // retrieve data and populate listview
        String location = intent.getStringExtra(MainActivity.LOCATION);
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);
        mLocationLabel.setText(location);

        DayAdapter adapter = new DayAdapter(this, mDays);
        setListAdapter(adapter);
    }
}
