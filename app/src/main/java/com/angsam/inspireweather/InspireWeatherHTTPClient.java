/*
This is the client class for the app
Here, data is retrieved from the Google Places API and the Darksky Weather API
Pretty much this handles backend but I do also update UI in the
onPostExecute method.

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;


import androidx.preference.PreferenceManager;

import java.lang.Math;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class InspireWeatherHTTPClient extends AsyncTask<String, Void, Void> {
    //HTTP CONNECTION
    private String key = "";
    private String placesKey = "";
    private BufferedReader connectionReader;
    private StringBuffer data, locationData;
    private final String TAG = this.getClass().getSimpleName();

    //JSONPARSING
    private JSONObject dataObject, weatherObject;
    private JSONArray dailyArray, hourlyArray;
    private static HttpURLConnection connection;
    private String urlString;
    
    private WeakReference<Activity> main;
    private WeakReference<ScrollView> mainUI;

    //Hourly Forecast
    private WeakReference<TextView[]> hourlyForecastTexts;
    private WeakReference<ImageView[]> hourlyForecastImages;
    private WeakReference<TextView[]> hourlyForecastPrecip;

    //Daily Forecast
    private WeakReference<TextView[]> dailyForecastTexts;
    private WeakReference<ImageView[]> dailyForecastImages;
    private WeakReference<TextView[]> dailyForecastPrecip;

    //SI setting
    private Boolean siUnits = false;

    //Time setting
    private Boolean twentyFourHour = false;



    public InspireWeatherHTTPClient(Activity mainAct, ScrollView mainLay, TextView[] hourlyTexts, ImageView[] hourlyImages, TextView[] hourlyPrecip, TextView[] dailyTexts, ImageView[] dailyImages, TextView[] dailyPrecip){
        main = new WeakReference<>(mainAct);
        mainUI = new WeakReference<>(mainLay);

        hourlyForecastTexts = new WeakReference<>(hourlyTexts);
        hourlyForecastImages = new WeakReference<>(hourlyImages);
        hourlyForecastPrecip = new WeakReference<>(hourlyPrecip);

        dailyForecastTexts = new WeakReference<>(dailyTexts);
        dailyForecastImages = new WeakReference<>(dailyImages);
        dailyForecastPrecip = new WeakReference<>(dailyPrecip);

    }

    @Override
        protected void onPreExecute(){
            urlString = "https://api.darksky.net/forecast/";
            main.get().startActivityForResult(new Intent(main.get(), LoadingActivity.class), 100); // start loading screen
            main.get().overridePendingTransition(R.anim.snap_in, R.anim.snap_out);
    }
    @Override
        protected Void doInBackground(String... coor){
            weatherConnection(coor);
            placesConnection(coor);
            return null;
    }

    private void weatherConnection(String... coor){
        try{
            data = new StringBuffer();
            urlString = urlString + key + "/" + coor[0] + "," + coor[1];
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main.get());

            if (settings.getBoolean("si units", true)){
                urlString = urlString + "?units=si";
                siUnits = true;
            }
            if(settings.getBoolean("twentyfourclock", true)){
                twentyFourHour = true;
            }

            URL url = new URL(urlString);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            InputStream iStream;
            if(responseCode > 299){
                iStream = connection.getErrorStream();
            }
            else{
                iStream = connection.getInputStream();
            }
            connectionReader = new BufferedReader((new InputStreamReader(iStream)));
            String line;

            while((line = connectionReader.readLine()) != null){
                data.append(line);
            }

            connectionReader.close();
        }
        catch(MalformedURLException e){
            Log.e(TAG, "Connection Failed", e);
            toFail();
        } catch(IOException e){
            Log.e(TAG, "Connection Failed", e);
            toFail();
        } finally{
            connection.disconnect();
        }
    }

    private void placesConnection(String... coor){
        try{
            locationData = new StringBuffer();

            urlString = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
            urlString = urlString + coor[0] + "," + coor[1] + "&key=" +  placesKey;
            URL url = new URL(urlString);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            InputStream iStream;
            if(responseCode > 299){
                iStream = connection.getErrorStream();
            }
            else{
                iStream = connection.getInputStream();
            }
            connectionReader = new BufferedReader((new InputStreamReader(iStream)));
            String line;

            while((line = connectionReader.readLine()) != null){
                locationData.append(line);
            }
            connectionReader.close();
        }
        catch(MalformedURLException e){
            Log.e(TAG, "Connection Failed", e);
            //toFail();
        } catch(IOException e){
            Log.e(TAG, "Connection Failed", e);
            //toFail();
        } finally{
            connection.disconnect();
        }
    }







    protected void onPostExecute(Void v){
        try{
            Activity mainAct = main.get();
            TextView locationDisplay = mainAct.findViewById(R.id.location);
            TextView degreesDisplay = mainAct.findViewById(R.id.degrees);
            TextView coRainDisplay = mainAct.findViewById(R.id.chanceOfRain);
            TextView highDisplay = mainAct.findViewById(R.id.high);
            TextView lowDisplay = mainAct.findViewById(R.id.low);
            
            //Hourly forecast views

            TextView feelsLike = mainAct.findViewById(R.id.feelslike_val);
            TextView sunrise = mainAct.findViewById(R.id.sunrise_val);
            TextView sunset = mainAct.findViewById(R.id.sunset_val);
            TextView humidity = mainAct.findViewById(R.id.humidity_val);
            TextView wind = mainAct.findViewById(R.id.wind_val);
            TextView visibility = mainAct.findViewById(R.id.visibility_val);
            TextView uvIndex = mainAct.findViewById(R.id.uvindex_val);
            TextView pressure = mainAct.findViewById(R.id.pressure_val);
            TextView dewPoint = mainAct.findViewById(R.id.dewpoint_val);
            TextView cloudCover = mainAct.findViewById(R.id.cloudcover_val);
            
            
            
            
            String weather, secondaryWeather, chanceOfRain, high, low;

            String velocityUnits = "MPH";
            String distanceUnits = "MI";
            String pressureUnits = "inHg";

            if(siUnits){
                velocityUnits = "KM/H";
                distanceUnits = "KM";
                pressureUnits = "mbar";
            }

            //JSON Parsing and calculation
            dataObject = new JSONObject(data.toString());
            weatherObject = dataObject.getJSONObject("currently");

            weather = weatherObject.getString("icon");
            secondaryWeather=weatherObject.getString("summary");

            chanceOfRain = (int)Math.round(weatherObject.getDouble("precipProbability")*100) + "%";
            int deg = (int) Math.round(weatherObject.getDouble("temperature"));

            dailyArray = dataObject.getJSONObject("daily").getJSONArray("data");
            hourlyArray = dataObject.getJSONObject("hourly").getJSONArray("data");

            high = Integer.toString((int)Math.round(dailyArray.getJSONObject(0).getDouble("temperatureHigh")));
            low = Integer.toString((int)Math.round(dailyArray.getJSONObject(0).getDouble("temperatureLow")));


            //change current day UI
            degreesDisplay.setText(String.format("%d",deg));
            locationDisplay.setText(getCity());
            coRainDisplay.setText(chanceOfRain);
            highDisplay.setText(high);
            lowDisplay.setText(low);
            selectImage(weather, secondaryWeather);

            //change forecasts UI
            setHourlyForecast();
            setDailyForecast();


            //change sunrise/sunset
            long sunriseL = dailyArray.getJSONObject(0).getLong("sunriseTime");
            long sunsetL = dailyArray.getJSONObject(0).getLong("sunsetTime");


            String sunriseTime;
            String sunsetTime;

            if(twentyFourHour){
                sunriseTime = new SimpleDateFormat("kk:mm" ).format(sunriseL*1000).toUpperCase();
                sunsetTime = new SimpleDateFormat("kk:mm" ).format(sunsetL*1000).toUpperCase();
            }
            else{
                sunriseTime = new SimpleDateFormat("hh:mm a" ).format(sunriseL*1000).toUpperCase();
                sunsetTime = new SimpleDateFormat("hh:mm a" ).format(sunsetL*1000).toUpperCase();
            }



            long currentTime = weatherObject.getLong("time");

            if(currentTime < sunriseL || currentTime > sunsetL){ //set night background when night
                setNightBackground();
            }
            else{ //necessary for location changes
                setDayBackground();
            }

            System.out.println("postexecution!");

            int windSpeed = (int)weatherObject.getDouble("windSpeed");
            String windString = String.format("%d %s", windSpeed, velocityUnits);

            if(windSpeed > 0){ // setting wind direction
                int winDir =weatherObject.getInt("windBearing");
                if(winDir > 337.5 || winDir < 22.5){
                    windString = windString + " N";
                }
                else if(winDir > 292.5){
                    windString = windString + " NW";
                }
                else if(winDir > 247.5){
                    windString = windString + " W";
                }
                else if(winDir > 202.5){
                    windString = windString + " SW";
                }
                else if(winDir > 157.5){
                    windString = windString + " S";
                }
                else if(winDir > 112.5){
                    windString = windString + " SE";
                }
                else if(winDir > 67.5){
                    windString = windString + " E";
                }
                else{
                    windString = windString + " NE";
                }
            }

            //Setting details UI text

            feelsLike.setText(String.format("%d°", (int)Math.round(weatherObject.getDouble("apparentTemperature"))));
            sunrise.setText(String.format(sunriseTime));
            sunset.setText(String.format(sunsetTime));
            humidity.setText(String.format("%d%%", (int)Math.round(weatherObject.getDouble("humidity")*100)));
            wind.setText(windString);
            visibility.setText(String.format("%d %s", (int) Math.round(weatherObject.getDouble("visibility")), distanceUnits));
            dewPoint.setText(String.format("%d°", (int) Math.round(weatherObject.getDouble("dewPoint"))));
            uvIndex.setText(String.format("%d", weatherObject.getInt("uvIndex")));

            if(siUnits){
                pressure.setText(String.format("%.1f %s", weatherObject.getDouble("pressure"),pressureUnits));
            }
            else{
                pressure.setText(String.format("%.2f %s", weatherObject.getDouble("pressure")/33.864,pressureUnits));
            }
            cloudCover.setText(String.format("%d%%", (int)Math.round(weatherObject.getDouble("cloudCover")*100)));


        } catch(JSONException e){
            Log.e(TAG, "JSON PARSING FAILURE", e);
        } finally {
            if(mainUI.get().getVisibility() == View.INVISIBLE){
                mainUI.get().setVisibility(View.VISIBLE);
            }
            main.get().finishActivity(100); // end loading screen
        }
    }

    private String getCity(){
        try {
            boolean foundCity = false;
            JSONObject locationObject = new JSONObject(locationData.toString());
            JSONArray locationArray = locationObject.getJSONArray("results").getJSONObject(0).getJSONArray("address_components");

            for (int i = 0; i < locationArray.length(); i++) {
                if (locationArray.getJSONObject(i).getJSONArray("types").getString(0).equals("locality")) {
                    foundCity = true;
                    return locationArray.getJSONObject(i).getString("long_name").toUpperCase();
                }
            }
            if(!foundCity){ //return a broader location when could not get specific locality
                for (int i = 0; i < locationArray.length(); i++) {
                    if (locationArray.getJSONObject(i).getJSONArray("types").getString(0).equals("political")) {
                        return locationArray.getJSONObject(i).getString("long_name").toUpperCase();
                    }
                }
            }
        } catch(JSONException e){
            Log.e(TAG, "LOCATION JSON PARSING FAILURE", e);
        }
        return null;
    }




/*
    This method changes the image and text depending on condition.
 */
    private void selectImage(String w, String sw){
        TextView weatherTOP = main.get().findViewById(R.id.weatherTOP);
        ImageView weatherDisplay = main.get().findViewById(R.id.weatherIMG);

        switch(w){
            case("clear-day"):
                weatherDisplay.setImageResource(R.drawable.sunny);
                weatherTOP.setText("CLEAR");
                break;
            case("clear-night"):
                weatherDisplay.setImageResource(R.drawable.clear_night);
                weatherTOP.setText("CLEAR");
                break;
            case("rain"):
                if(sw.equals("Thunderstorm")){
                    weatherDisplay.setImageResource(R.drawable.thunderstorm);
                    weatherTOP.setText("THUNDERSTORM");
                }
                else{
                    weatherDisplay.setImageResource(R.drawable.rain);
                    weatherTOP.setText("RAIN");
                }
                break;
            case("wind"):
                weatherDisplay.setImageResource(R.drawable.wind);
                weatherTOP.setText("WINDY");
                break;
            case("snow"):
                weatherDisplay.setImageResource(R.drawable.snow);
                weatherTOP.setText("SNOW");
                break;
            case("partly-cloudy-day"):
                weatherDisplay.setImageResource(R.drawable.partly_cloudy);
                weatherTOP.setText("PARTLY CLOUDY");
                break;
            case("partly-cloudy-night"):
                weatherDisplay.setImageResource(R.drawable.partly_cloudy_night);
                weatherTOP.setText("PARTLY CLOUDY");
                break;
            case("cloudy"):
                weatherDisplay.setImageResource(R.drawable.cloudy);
                weatherTOP.setText("CLOUDY");
                break;
            case("sleet"):
                weatherDisplay.setImageResource(R.drawable.sleet);
                weatherTOP.setText("SLEET");
                break;
            case("fog"):
                weatherDisplay.setImageResource(R.drawable.fog);
                weatherTOP.setText("FOG");
                break;
        }
    }

    private void selectForecastImage(String w, String sw, ImageView forecastDisp, TextView precipChance, String precipChanceStr) {
        precipChance.setText(""); // for location changes
        switch(w){
            case("clear-day"):
                forecastDisp.setImageResource(R.drawable.forecast_sunny);
                break;
            case("clear-night"):
                forecastDisp.setImageResource(R.drawable.forecast_clear_night);
                break;
            case("rain"):
                precipChance.setText(precipChanceStr);

                if(sw.equals("Thunderstorm")){
                    forecastDisp.setImageResource(R.drawable.forecast_thunder);
                }
                else{
                    forecastDisp.setImageResource(R.drawable.forecast_rain);
                }
                break;
            case("snow"):
                precipChance.setText(precipChanceStr);
                forecastDisp.setImageResource(R.drawable.forecast_snow);
                break;
            case("partly-cloudy-day"):
                forecastDisp.setImageResource(R.drawable.forecast_partly_cloudy);
                break;
            case("partly-cloudy-night"):
                forecastDisp.setImageResource(R.drawable.forecast_partly_cloudy_night);
                break;
            case("cloudy"):
                forecastDisp.setImageResource(R.drawable.forecast_cloudy);
                break;
            case("sleet"):
                precipChance.setText(precipChanceStr);
                forecastDisp.setImageResource(R.drawable.forecast_sleet);
                break;
            case("wind"):
                forecastDisp.setImageResource(R.drawable.wind);
                break;
            case("fog"):
                forecastDisp.setImageResource(R.drawable.forecast_fog);
                break;
        }
    }

    private void setHourlyForecast(){
        int temp; //temperature
        String condition, secondaryCondition, chanceOfRain; //sunny, cloudy etc.
        try{
            for(int i = 0; i<24; i++) { //next hr starts at 1
                temp = (int) Math.round(hourlyArray.getJSONObject(i+1).getDouble("temperature"));

                condition = hourlyArray.getJSONObject(i+1).getString("icon");
                secondaryCondition = hourlyArray.getJSONObject(i+1).getString("summary");
                chanceOfRain = String.format("%d%%", (int) Math.round(hourlyArray.getJSONObject(i+1).getDouble("precipProbability")*100));

                hourlyForecastTexts.get()[i].setText(String.format("%d°", temp));
                selectForecastImage(condition, secondaryCondition, hourlyForecastImages.get()[i], hourlyForecastPrecip.get()[i], chanceOfRain);
            }
        }
        catch(JSONException e){
            Log.e(TAG, "JSON PARSING FAILURE", e);
        }
    }

    private void setDailyForecast(){
        int hi, lo;
        String condition, secondaryCondition, chanceOfRain;

        try{
            for(int i = 0; i<7; i++){
                hi = (int) Math.round(dailyArray.getJSONObject(i+1).getDouble("temperatureHigh"));
                lo = (int) Math.round(dailyArray.getJSONObject(i+1).getDouble("temperatureLow"));

                condition = dailyArray.getJSONObject(i+1).getString("icon");
                secondaryCondition = dailyArray.getJSONObject(i+1).getString("summary");
                secondaryCondition = secondaryCondition.substring(0, secondaryCondition.indexOf(" "));
                chanceOfRain = String.format("%d%%", (int) Math.round(dailyArray.getJSONObject(i+1).getDouble("precipProbability")*100));
                selectForecastImage(condition, secondaryCondition, dailyForecastImages.get()[i], dailyForecastPrecip.get()[i], chanceOfRain);

                dailyForecastTexts.get()[i].setText(String.format("%d°|%d°", hi, lo));
            }
        }catch(JSONException e){
            Log.e(TAG, "JSON PARSING FAILURE", e);
        }
    }

    /*

    This method will change the background to a darker gradient.

     */

    private void setNightBackground(){
        TextView locationDisplay = main.get().findViewById(R.id.location);
        TextView weatherTOP = main.get().findViewById(R.id.weatherTOP);
        ImageView background_night = main.get().findViewById(R.id.mainbackground);

        background_night.setImageResource(R.drawable.night_background);
        background_night = main.get().findViewById(R.id.mainbackgroundpt2);
        background_night.setImageResource(R.drawable.night_background);


        locationDisplay.setTextColor(0xFFFFFFFF);
        weatherTOP.setTextColor(0xFFFFFFFF);


        ImageView googleAttr = main.get().findViewById(R.id.powered_by_google);
        googleAttr.setImageResource(R.drawable.powered_by_google_on_non_white);

        ImageView darkskyAttr = main.get().findViewById(R.id.powered_by_darksky);
        darkskyAttr.setImageResource(R.drawable.poweredbydarksky_night);
    }

    /*

    This method will change the background to a lighter gradient.

     */

    private void setDayBackground(){
        TextView locationDisplay = main.get().findViewById(R.id.location);
        TextView weatherTOP = main.get().findViewById(R.id.weatherTOP);
        ImageView background_night = main.get().findViewById(R.id.mainbackground);

        background_night.setImageResource(R.drawable.background2);
        background_night = main.get().findViewById(R.id.mainbackgroundpt2);
        background_night.setImageResource(R.drawable.background2);


        locationDisplay.setTextColor(0xFF000000);
        weatherTOP.setTextColor(0xFF000000);


        ImageView googleAttr = main.get().findViewById(R.id.powered_by_google);
        googleAttr.setImageResource(R.drawable.powered_by_google_on_white);

        ImageView darkskyAttr = main.get().findViewById(R.id.powered_by_darksky);
        darkskyAttr.setImageResource(R.drawable.poweredbydarksky);
    }

    private void toFail(){
        final Intent toFail = new Intent(main.get(), ConnectionFailed.class);
        main.get().startActivity(toFail);
        main.get().finish();
    }


}
