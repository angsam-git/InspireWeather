/*

MIT License

Copyright (c) 2020 Angel Samuel Mendez

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 */


package com.angsam.inspireweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity{
    private TextView dateDisplay, quoteDisplay, authorDisplay, forecastDate;
    private TextView[] hourlyForecastTexts, dailyForecastTexts, hourlyForecastPrecipProb, dailyForecastPrecipProb, hourlyForecastTimes;
    private ImageView[] hourlyForecastImages, dailyForecastImages;
    private String date, nextWeekDay, days;
    private int hour, amOrPm;

    private String quoteData, quote, author;
    private JSONArray quoteArray;
    private JSONObject quoteObject;
    private Activity mainAct = this;

    private String TAG = "MAIN_ACT";
    private String longitude, latitude;
    LocationManager locationManager;
    private int LOCATION_PERMISSION_CODE = 1;

    private ImageButton settingsButton;

    private ScrollView mainLay;

    private SharedPreferences settings;

    private boolean settingsChanged, initialExec;

    SharedPreferences.OnSharedPreferenceChangeListener setChange;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLay = findViewById(R.id.main_layout);
        initialExec = false;

        //get user saved preferences
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        settingsChanged = false;

        setViews(); // set the UI views

        //get location service
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //If location is disabled, prompt user
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            promptGPS();
        }
        else{ //Otherwise attempt to retrieve the location
            getLocation();
        }

        //When preference is changed in settings screen, let this activity know
        setChange = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                settingsChanged = true;
            }
        };

        //When user changes location, update the location
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5000, new LocationListener() { //min update time 5 min, 5km min distance change
                @Override
                public void onLocationChanged(Location location) {
                    if(initialExec){
                        getLocation();
                    }
                    initialExec = true; // ignore app start location change
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            });
        }catch (SecurityException e){
            Log.e(TAG, "Getting new location failed.", e);
        }
    }

    private void setViews() { //Reference UI elements from activity_main.xml
        //main ui elements
        dateDisplay = findViewById(R.id.date);
        quoteDisplay = findViewById(R.id.quote);
        authorDisplay = findViewById(R.id.author);
        settingsButton = findViewById(R.id.settings_button);

        //When settings button is pressed, open settings page
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });


        //hourly forecast elements
        hourlyForecastTexts = new TextView[24];
        hourlyForecastImages = new ImageView[24];
        hourlyForecastPrecipProb = new TextView[24];
        hourlyForecastTimes = new TextView[24];

        for (int i = 0; i < 24; i++) {
            hourlyForecastTexts[i] = findViewById(getResources().getIdentifier(String.format("temp_hourly%d", i), "id", getPackageName()));
            hourlyForecastImages[i] = findViewById(getResources().getIdentifier(String.format("img_hourly%d", i), "id", getPackageName()));
            hourlyForecastPrecipProb[i] = findViewById(getResources().getIdentifier(String.format("precip_hourly%d", i), "id", getPackageName()));
            hourlyForecastTimes[i] = findViewById(getResources().getIdentifier(String.format("time_hourly%d", i), "id", getPackageName()));
        }


        //daily forecast elements
        dailyForecastTexts = new TextView[7];
        dailyForecastImages = new ImageView[7];
        dailyForecastPrecipProb = new TextView[7];
        for (int i = 0; i < 7; i++) {
            dailyForecastTexts[i] = findViewById(getResources().getIdentifier(String.format("temp_daily%d", i), "id", getPackageName()));
            dailyForecastImages[i] = findViewById(getResources().getIdentifier(String.format("img_daily%d", i), "id", getPackageName()));
            dailyForecastPrecipProb[i] = findViewById(getResources().getIdentifier(String.format("precip_daily%d", i), "id", getPackageName()));
        }

    }
    /*

    Start the application

     */
    private void startApp(){
        setDate();
        setQuote();
        setDays(); //forecast days

        TimerTask timerTask = new TimerTask(){
            @Override
            public void run(){ //Update UI on timer
                setTimes();
                Log.d("client:","executing...");
                new InspireWeatherHTTPClient(mainAct, mainLay, hourlyForecastTexts, hourlyForecastImages, hourlyForecastPrecipProb, dailyForecastTexts, dailyForecastImages, dailyForecastPrecipProb).execute(latitude,longitude);
                Log.d("client:","executed!");
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 0,600000); // weather api update time = 5 min -> I execute every 10 min to save calls
    }

     /*

    Setting the date display

     */

    private void setDate(){
        Calendar cal = new GregorianCalendar();
        date = new SimpleDateFormat("EEE MMM dd, yyyy" ).format(cal.getTime()).toUpperCase();
        if (settings.getBoolean("twentyfourclock", true)) {
            hour = cal.get(Calendar.HOUR_OF_DAY);
        }
        else{
            hour = cal.get(Calendar.HOUR);
        }
        amOrPm = cal.get(Calendar.AM_PM);
        dateDisplay.setText(String.format(" %s ", date));
        cal.add(Calendar.DATE,1);
        nextWeekDay = new SimpleDateFormat("EEE" ).format(cal.getTime()).toUpperCase();
        cal.add(Calendar.DATE, -1);
    }

    /*

    Setting the forecast time displays

     */

    private void setTimes(){
        hour = hour+1;
        if(settings.getBoolean("twentyfourclock", true)){ // 24 hour clock
            for(int i = 0; i < 24; i++){
                if(hour == 24){
                    hour = 0;
                }
                hourlyForecastTimes[i].setText(String.format("%d:00", hour));
                hour++;
            }
        }
        else{ //12 hour clock
            for(int i = 0; i < 24; i++){
                if(amOrPm == Calendar.AM) {
                    if (hour == 12) {
                        hourlyForecastTimes[i].setText(String.format("%dPM", hour));
                        hour = 0;
                        amOrPm = Calendar.PM;
                    }
                    else{
                        hourlyForecastTimes[i].setText(String.format("%dAM", hour));
                    }
                }
                else{
                    if (hour == 12) {
                        hourlyForecastTimes[i].setText(String.format("%dAM", hour));
                        hour = 0;
                        amOrPm = Calendar.AM;
                    }
                    else{
                        hourlyForecastTimes[i].setText(String.format("%dPM", hour));
                    }
                }
                hour++;
            }
        }
    }



    /*

    Setting the forecast dates


     */

    private void setDays(){
        days = "SUN MON TUE WED THU FRI SAT";
        days = days.substring(days.indexOf(nextWeekDay)) + " " + days.substring(0, days.indexOf(nextWeekDay));

        String[] dayArr = days.split(" ");
        for(int i=0;i<7;i++){
            forecastDate = findViewById(getResources().getIdentifier(String.format("day%d", i), "id", getPackageName()));
            forecastDate.setText(dayArr[i]);
        }
    }
    /*

    Setting the quote display

     */

    private void setQuote() {
        try {
            InputStream iStream = getAssets().open("quotes.json");
            int size = iStream.available();
            byte[] buffer = new byte[size];
            iStream.read(buffer);
            iStream.close();
            quoteData = new String(buffer);
            quoteArray = new JSONArray(quoteData);
            Random rand = new Random();
            quoteObject = quoteArray.getJSONObject(rand.nextInt(quoteArray.length()));
            quote = "\""+ quoteObject.getString("quoteText") + "\"";
            author = quoteObject.getString("quoteAuthor").toUpperCase();
            quoteDisplay.setText(quote);
            authorDisplay.setText(author);

        } catch (IOException e) {
            Log.e(TAG, "JSON PARSING Failed", e);
        } catch (JSONException e) {
            Log.e(TAG, "JSON PARSING Failed", e);
        }
    }

    /*

    New prompt dialog when android location service is disabled

     */

    private void promptGPS(){
        mainLay.setVisibility(View.INVISIBLE);
        AlertDialog.Builder gpsAlert = new AlertDialog.Builder(MainActivity.this);
        gpsAlert.setMessage("Enable Location");
        gpsAlert.setCancelable(false);
        gpsAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                finish();
            }
        });
        gpsAlert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(MainActivity.this, LocationFailed.class));
                finish();
            }
        });
        AlertDialog gpsWarning = gpsAlert.create();
        gpsWarning.show();
    }

    /*

    Check for location permission - if disabled, then request location permission
    However, if location permission is enabled but location could not acquired,
    then this activity is finished and user is allowed to retry from LocationFailed activity

     */

    private void getLocation(){
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            mainLay.setVisibility(View.INVISIBLE);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);

        }
        else{
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location passiveLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if(gpsLocation != null){
                latitude = Double.toString(gpsLocation.getLatitude());
                longitude = Double.toString(gpsLocation.getLongitude());
                startApp();
            }
            else if(networkLocation != null){
                latitude = Double.toString(networkLocation.getLatitude());
                longitude = Double.toString(networkLocation.getLongitude());
                startApp();
            }
            else if(passiveLocation != null){
                latitude = Double.toString(passiveLocation.getLatitude());
                longitude = Double.toString(passiveLocation.getLongitude());
                startApp();
            }
            else{
                startActivity(new Intent(MainActivity.this, LocationFailed.class));
                overridePendingTransition(R.anim.snap_in, R.anim.snap_out);
                finish();
            }

        }
    }

    /*

    If user denies location permission, start LocationFailed activity and end this MainActivity.

     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == LOCATION_PERMISSION_CODE){
            if(grantResults.length<=0 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                startActivity(new Intent(MainActivity.this, LocationFailed.class));
            }
            else{
                getLocation();
            }
        }
    }

    /*

    Check if a setting was updated when user returns to main page.

     */

    @Override
    protected void onResume(){
        super.onResume();
        settings.registerOnSharedPreferenceChangeListener(setChange);
        if(settingsChanged){
            getLocation();
            settingsChanged = false;
        }
    }

    /*

    Overriding default transition

     */

    @Override
    protected void onPause(){
        super.onPause();
        //PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(setChange);
        if(isFinishing()){
            overridePendingTransition(R.anim.snap_in, R.anim.snap_out);
        }
    }

}
