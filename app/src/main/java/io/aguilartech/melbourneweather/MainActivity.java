package io.aguilartech.melbourneweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    TextView cityText;
    CircularProgressButton button;

    String weatherInfo = "";
    String city;
    String country;
    Long[] date;
    String[] main;
    String[] description;
    String[] icon;
    String[] temp;
    String[] humidity;
    String[] cloudCover;
    String[] windSpeed;
    String[] rain;
    Integer[] dryIndex;

    long sunriseUNIX;
    long sunsetUNIX;
    double latitude = -37.91;
    double longitude = 145.01;
    int sunrise;
    int sunset;
    int time;

    ArrayList<String> itemTitle = new ArrayList<String>();
    ArrayList<String> itemDetails= new ArrayList<String>();
    ArrayList<String> itemStats= new ArrayList<String>();
    ArrayList<Integer> itemColour= new ArrayList<Integer>();
    ArrayList<String> itemIcon= new ArrayList<String>();

    public class DownloadSunsetSunrise extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);

                JSONObject cityJSON =  jsonObject.getJSONObject("sys");
                sunriseUNIX = cityJSON.getLong("sunrise");
                sunsetUNIX  = cityJSON.getLong("sunset");

            } catch (Exception e) {
                e.printStackTrace();
            }

            DownloadTask task = new DownloadTask();
            task.execute("http://api.openweathermap.org/data/2.5/forecast?lat="+latitude+"&lon="+longitude+"&appid=a03d5ef4737135b72e3b9186e2764ac0");

        }
    }

    public class DownloadTask extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            try {
                JSONObject jsonObject = new JSONObject(s);

                String cnt =  jsonObject.getString("cnt");
                Log.i("cnt",cnt);

                JSONObject cityJSON =  jsonObject.getJSONObject("city");
                city = cityJSON.getString("name");
                country  = cityJSON.getString("country");

                Toast.makeText(MainActivity.this, "Location: " + city + ", " + country, Toast.LENGTH_SHORT).show();

                String locationText = "Location: " + city + ", " + country + " Sunrise: " + UNIXtoHourofDay(sunriseUNIX)  + "AM Sunset: "
                        + Integer.toString(UNIXtoHourofDay(sunsetUNIX)-12) + "PM";
                cityText.setText(locationText);


                JSONArray list =  jsonObject.getJSONArray("list");

                date = new Long[list.length()];
                main= new String[list.length()];
                description= new String[list.length()];
                icon = new String[list.length()];
                temp= new String[list.length()];
                humidity= new String[list.length()];
                cloudCover= new String[list.length()];
                windSpeed= new String[list.length()];
                rain= new String[list.length()];
                dryIndex= new Integer[list.length()];

                for (int i=0; i < list.length(); i++) {
                    JSONObject jsonPart = list.getJSONObject(i);

                    date[i] = Long.parseLong(String.valueOf(jsonPart.getLong("dt")));

                    String weatherJSON = jsonPart.getString("weather");
                    JSONArray weatherArray = new JSONArray(weatherJSON);
                    JSONObject weather = weatherArray.getJSONObject(0);
                    main[i] =  weather.getString("main");
                    description[i] =  weather.getString("description");
                    icon[i] =  "w" + weather.getString("icon");

                    String tk = jsonPart.getJSONObject("main").getString("temp");
                    double t = Double.parseDouble(tk)-273.15;
                    temp[i] = String.format("%.0f", new BigDecimal(t));


                    String hk = jsonPart.getJSONObject("main").getString("humidity");
                    double h = Double.parseDouble(hk);
                    humidity[i] = String.format("%.0f", new BigDecimal(h));

                    try {
                         cloudCover[i] = jsonPart.getJSONObject("clouds").getString("all");
                    } catch (Exception e) {
                        cloudCover[i] = "";
                    }
                    try {
                        windSpeed[i] = jsonPart.getJSONObject("wind").getString("speed");
                    } catch (Exception e) {
                        windSpeed[i] = "";
                    }
                    try {
                        String r = jsonPart.getJSONObject("rain").getString("3h");
                        double f = Double.parseDouble(r);
                        rain[i] = String.format("%.2f", new BigDecimal(f));


                    } catch (Exception e) {
                        rain[i] = "";
                    }
                    weatherInfo += UNIXtoHumanDate(date[i]) + " " + main[i] + " " + description[i] + " " + temp[i] + "°C " + cloudCover[i] + " " + windSpeed[i] + " " + rain[i] + "\n";
                }
                makeList ();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void getWeather(View view) {

        getLocation();

        weatherInfo = "";

        button.startAnimation();

        DownloadSunsetSunrise getSunriseSunset = new DownloadSunsetSunrise();
        getSunriseSunset.execute("https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=a03d5ef4737135b72e3b9186e2764ac0");




}
    public String howFastWouldClothesDry (int i) {
        String howFast = "";
        String dryingSpeed = "slow";

        Double tempF = 0.0;
        Double humidityF = 0.0;
        Double cloudF = 0.0;
        Double windF = 0.0;
        Double RainF = 0.0;
        try {
                //  calculate temperature factor
                try {
                    double max = 40; //range: 5C - 40C
                    double min = 5;

                    tempF = round(Double.parseDouble(temp[i]) / (max-min) - (min/(max-min)),2);


                } catch (Exception e) {
                    tempF = 0.0;
                }

                //  calculate humidity factor
                try {
                    double max = 50; //range: 85 -> 50% humidty
                    double min = 85;

                    humidityF = round((Double.parseDouble(humidity[i])) / (max-min) - (min/(max-min)),2);


                } catch (Exception e) {
                    humidityF = 0.0;
                }
                //  calculate cloud cover factor
                try {

                    double max = 100;
                    double min = -25;

                    cloudF = round((100 - Double.parseDouble(cloudCover[i])) / (max-min) - (min/(max-min)),2);
                } catch (Exception e) {
                    cloudF = 1.0;
                }
                try {
                    //  calculate wind speed factor
                    double max = 10; //range 0 - 10
                    double min = 0;

                    windF = round((Double.parseDouble(windSpeed[i]) / (max-min) - (min/(max-min))),2);
                } catch (Exception e) {
                    windF = 0.0;
                }
                try {
                    //  calculate rain factor
                    double max = 1; // range 0-1
                    double min = 0;

                    RainF = round((1 - Double.parseDouble(rain[i])) / (max-min) - (min/(max-min)),2);
                } catch (Exception e) {
                    RainF = 1.0;
                }

                dryIndex[i] = (int) (tempF + humidityF + cloudF +  windF + RainF)*2;

                if (dryIndex[i] < 1) {
                    dryingSpeed  = "TOO WET";
                } else if(dryIndex[i] < 5) {
                    dryingSpeed  = "WON'T DRY WELL";
                } else if(dryIndex[i] < 10) {
                    dryingSpeed  = "OK DRYING";
                } else {
                    dryingSpeed  = "FAST DRYING";
                }

                howFast = dryingSpeed + " " + dryIndex[i] + "/10";
                return howFast;

        } catch (Exception e) {
            e.printStackTrace();
            return howFast = "don't know, error";
        }
    }


    public void makeList ()  {

        ListView itemlist = (ListView) findViewById(R.id.listView);
        itemTitle.clear();
        itemDetails.clear();
        itemStats.clear();
        itemColour.clear();
        itemIcon.clear();

        /*
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        for(int i = 0 ; i < date.length; i++) {
            try {

                time = new SimpleDateFormat("EEE, d MMM hh:mm aaa").parse(date[i]).getHours();
            } catch (Exception e) {
                time = 12;
            }
            if (time > sunrise && time < sunset) {
                Map<String, String> datum = new HashMap<String, String>(2);

                datum.put("date", date[i] + " - " + description[i] + " " + temp[i] + "°C Humidity:" + humidity[i]+"%");
                datum.put("Drying Index", howFastWouldClothesDry(i));

                data.add(datum);
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] {"date", "Drying Index"},
                new int[] {android.R.id.text1,
                        android.R.id.text2});
        */

        for(int i = 0 ; i < date.length; i++) {
            if(sunrise < UNIXtoHourofDay(date[i]) && UNIXtoHourofDay(date[i]) < sunset) {
                itemTitle.add(UNIXtoHumanDate(date[i]) + " - " + description[i].toUpperCase());
                itemDetails.add(howFastWouldClothesDry(i));
                itemStats.add(temp[i] + "°C Humidity: " + humidity[i]+"%");
                itemColour.add(getItemColour(dryIndex[i]));
                itemIcon.add(icon[i]);
            }
        }


        CustomAdapter   customAdapter = new CustomAdapter();
        itemlist.setAdapter(customAdapter);

        button.revertAnimation();

    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return itemTitle.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.day_layout,null);
            ImageView  weatherIconView = (ImageView) view.findViewById(R.id.weatherIconView);
            TextView listItemTitleView = (TextView) view.findViewById(R.id.listItemTitleView);
            TextView listItemDetailsView = (TextView) view.findViewById(R.id.listItemDetailsView);
            TextView listItemStatsView = (TextView) view.findViewById(R.id.listItemStatsView);

            listItemTitleView.setText(itemTitle.get(i));
            listItemDetailsView.setText(itemDetails.get(i));
            listItemStatsView.setText(itemStats.get(i));

            int id = getResources().getIdentifier(getPackageName()+":drawable/" + itemIcon.get(i), null, null);



            weatherIconView.setImageResource(id);

            view.setBackgroundColor(itemColour.get(i));


            return view;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        cityText = findViewById(R.id.locationView);

         button = (CircularProgressButton) findViewById(R.id.button);

        sunriseUNIX = 1533244729;
        sunsetUNIX = 1533281660;

        sunrise = UNIXtoHourofDay(sunriseUNIX);
        sunset  = UNIXtoHourofDay(sunsetUNIX);



    }

    void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location != null) {
                 latitude = round(location.getLatitude(),2);
                 longitude = round(location.getLongitude(),2);

            } else {
               // Toast.makeText(this, "location can't be found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION:
                getLocation();
                break;
        }

    }

    public int getItemColour(int index) {
        int colour = 0xff888888;  //Gray
        if (index < 1) {
            colour  = 0xffff0000;  //Red
        } else if(index < 5) {
            colour  = 0xff888888;  // Gray
        } else if(index < 10) {
            colour  = 0xff00ff00; // Green
        } else {
            colour  = 0xff888888; // Gray
        }

        return colour;
    }

    public int UNIXtoHourofDay(long unixSeconds) {
        int hourOfDay;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixSeconds*1000);
        cal.setTimeZone(java.util.TimeZone.getDefault());
        hourOfDay = cal.get(Calendar.HOUR_OF_DAY);

        return hourOfDay;

    }

    public String  UNIXtoHumanDate (long unixSeconds)  {
        String formattedDate;
        // convert seconds to milliseconds
        Date date = new java.util.Date(unixSeconds*1000L);

        Calendar today = Calendar.getInstance();
        Calendar dateasCal = Calendar.getInstance();
        dateasCal.setTime(date);

        SimpleDateFormat sdf = new java.text.SimpleDateFormat("h aaa EEE, d MMM ");
        sdf.setTimeZone(java.util.TimeZone.getDefault());

        // the format of your date
        if(today.get(Calendar.DAY_OF_YEAR) == dateasCal.get(Calendar.DAY_OF_YEAR)) {
             sdf = new java.text.SimpleDateFormat("h aaa");
              formattedDate = sdf.format(date) + " TODAY";
             return formattedDate;
        } else if((today.get(Calendar.DAY_OF_YEAR)+1) == dateasCal.get(Calendar.DAY_OF_YEAR)) {
             sdf = new java.text.SimpleDateFormat("h aaa");
             formattedDate = sdf.format(date) + " TOMORROW";
             return formattedDate;
        }
        // give a timezone reference for formatting (see comment at the bottom)

        formattedDate = sdf.format(date);
        return formattedDate;

    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
