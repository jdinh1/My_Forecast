package com.tuturucoding.myforecast.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuturucoding.myforecast.R;
import com.tuturucoding.myforecast.adapters.DayAdapter;
import com.tuturucoding.myforecast.weather.Day;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DailyForecastActivity extends ListActivity {
    private Day[] mDays;
    boolean isNight;
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
        isNight = intent.getBooleanExtra(MainActivity.NIGHT,isNight);
        changeLayoutCheck();    // check for night time
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);
        mLocationLabel.setText(location);

        DayAdapter adapter = new DayAdapter(this, mDays);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String dayOfTheWeek = mDays[position].getDayOfTheWeek();
        String condition = mDays[position].getSummary();
        String highTemp = mDays[position].getTemperatureMax() + "";
        String message = String.format("On %s the high will be %s and it will be %s",
                dayOfTheWeek,
                highTemp,
                condition);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void changeLayoutCheck() {
        // Change background if time has passed 4:00PM
        if (isNight) {
            RelativeLayout dailyLayout = (RelativeLayout) findViewById(R.id.dailyLayout);
            dailyLayout.setBackgroundResource(R.drawable.bg_gradient_night);
        }
    }
}
