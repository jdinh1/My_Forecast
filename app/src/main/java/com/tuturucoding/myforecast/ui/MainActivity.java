package com.tuturucoding.myforecast.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.tuturucoding.myforecast.R;
import com.tuturucoding.myforecast.weather.Current;
import com.tuturucoding.myforecast.weather.Day;
import com.tuturucoding.myforecast.weather.Forecast;
import com.tuturucoding.myforecast.weather.Hour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String LOCATION = "LOCATION" ;
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";

    private Forecast mForecast;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean permissionGranted = false;
    double latitude;// = 37.8267;
    double longitude;// = -122.4233;

    @BindView(R.id.timeTextView)
    TextView mTimeLabel;
    @BindView(R.id.temperatureLabel)
    TextView mTemperatureLabel;
    @BindView(R.id.humidity_value)
    TextView mHumidityValue;
    @BindView(R.id.precip_value)
    TextView mPrecipValue;
    @BindView(R.id.summary_label)
    TextView mSummaryLabel;
    @BindView(R.id.iconImageView)
    ImageView mIconImageView;
    @BindView(R.id.refreshImageView)
    ImageView mRefreshImageView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.location_label) TextView mLocationLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // hide refresh button to show the loading progress bar
        mProgressBar.setVisibility(View.INVISIBLE);

        // get current location
        mFusedLocationProviderClient = new FusedLocationProviderClient(this);
        getCurrentLocation();

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
                //getForecast(latitude, longitude);
            }
        });
    }

    private void getCurrentLocation() {
        checkGpsPermission();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return; // use default gpd coordinates
        }
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();

                            // get forecast after lon/lat is retrieved
                            getForecast(latitude, longitude);
                        }
                    }
                });

    }

    private void checkGpsPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 12); // 12 = int for response read back

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 12: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    permissionGranted = true;

                    // permission granted, get first forecast
                } else {
                    permissionGranted = false;
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = "c6e8ec40df46fd32602a39e664077e1c";

        String forcaseUrl = "https://api.darksky.net/forecast/" + apiKey +
                "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forcaseUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                            mForecast = parseForcastDetails(jsonData);

                            // run the function on main thread since view modification can't be done
                            // on seperate thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    upDateDisplay();
                                }
                            });
                        } else {
                            // display error dialog if there is any error
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.network_unavailable_message, Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void upDateDisplay() {
        Drawable drawable = ContextCompat.getDrawable(this, mForecast.getCurrent().getIconId());

        mTemperatureLabel.setText(mForecast.getCurrent().getTemperature() + "");
        mTimeLabel.setText("At " + mForecast.getCurrent().getFormattedTime() + " it will be");
        mHumidityValue.setText(mForecast.getCurrent().getHumidity() + "");
        mPrecipValue.setText(mForecast.getCurrent().getPrecipChance() + "%");
        mSummaryLabel.setText(mForecast.getCurrent().getSummary());
        mIconImageView.setImageDrawable(drawable);
        mLocationLabel.setText(mForecast.getCurrent().getLocation());

    }

    private Forecast parseForcastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        forecast.setCurrent(getCurrentDetails(jsonData));
        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for (int i = 0; i < days.length; i++) {
            JSONObject dailyData = data.getJSONObject(i);
            Day tempDay = new Day();

            tempDay.setTimezone(timezone);
            tempDay.setTime(dailyData.getLong("time"));
            tempDay.setSummary(dailyData.getString("summary"));
            tempDay.setIcon(dailyData.getString("icon"));
            tempDay.setTemperatureMax(dailyData.getDouble("temperatureMax"));

            days[i] = tempDay;

        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];
        for (int i = 0; i < data.length(); i ++) {
            JSONObject temp = data.getJSONObject(i);

            Hour tempHour = new Hour();
            tempHour.setIcon(temp.getString("icon"));
            tempHour.setTemperature(temp.getDouble("temperature"));
            tempHour.setSummary(temp.getString("summary"));
            tempHour.setTime(temp.getLong("time"));
            tempHour.setTimezone(timezone);

            hours[i] = tempHour;
        }

        return hours;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject currently = new JSONObject(forecast.getString("currently"));


        // populate weather data
        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance((currently.getDouble("precipProbability")));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);
        current.setLocation(forecast.getString("timezone"));

        return current;
    }


    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }


    private boolean isNetworkAvailable() {
        boolean isAvailable = false;
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    @OnClick (R.id.dailyButton)
    public void startDailyActivity(View view) {
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
        intent.putExtra(LOCATION, mForecast.getCurrent().getLocation());
        startActivity(intent);
    }

    @OnClick (R.id.hourlyButton)
    public void startHourlyActivity(View view) {
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
        intent.putExtra(LOCATION, mForecast.getCurrent().getLocation());
        startActivity(intent);


    }
}
