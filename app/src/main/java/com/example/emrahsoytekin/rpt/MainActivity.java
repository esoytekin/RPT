package com.example.emrahsoytekin.rpt;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends Activity {

    private static String url = "http://emrahs.duckdns.org/rpitemp/get_details.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new RequestTask().execute();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            initializeValues();
            new RequestTask().execute();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private Monitor getService(String in) {
        try {
            JSONObject jsonRootObject = new JSONObject(in);
            JSONArray jsonArray = jsonRootObject.optJSONArray("monitor");
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            Monitor m = new Monitor();

            m.setTemperature(jsonObject.get("temperature").toString());

            m.setHumidity(jsonObject.get("humidity").toString());
            m.setDate(jsonObject.get("comptime").toString());
            m.setRealTime(jsonObject.get("compdate").toString());
            m.setEnabled(jsonObject.get("enabled").toString().equals("Y") ? true : false);
            m.setNotifications(jsonObject.get("notifications").toString().equals("Y") ? true : false);

            return m;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void setValues(Monitor m) {
        if (m == null)
            return;
        TextView tvTemp = (TextView) findViewById(R.id.tempVal);
        TextView tvHum = (TextView) findViewById(R.id.humValue);
        TextView tvDate = (TextView) findViewById(R.id.date);
        TextView tvRealDate = (TextView) findViewById(R.id.realDate);
        Switch swToggle = (Switch) findViewById(R.id.swToggle);
        Switch swNotif = (Switch) findViewById(R.id.swNotif);

        tvTemp.setText(m.getTemperature());
        tvHum.setText(m.getHumidity());
        tvDate.setText(m.getDate());
        tvRealDate.setText(m.getRealTime());


        swToggle.setChecked(m.getEnabled());

        swToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                new ToggleTask().execute();
            }
        });

        swNotif.setChecked(m.getNotifications());
        swNotif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                new NotifTask().execute(b);
            }
        });
    }

    private void initializeValues() {

        TextView tvTemp = (TextView) findViewById(R.id.tempVal);
        TextView tvHum = (TextView) findViewById(R.id.humValue);
        TextView tvDate = (TextView) findViewById(R.id.date);
        TextView tvRealDate = (TextView) findViewById(R.id.realDate);
        Switch swToggle = (Switch) findViewById(R.id.swToggle);
        Switch swNotif = (Switch) findViewById(R.id.swNotif);

        tvTemp.setText(R.string.temp_val);
        tvHum.setText(R.string.hum_val);
        tvDate.setText(R.string.date_val);
        tvRealDate.setText(R.string.date_val);
        swToggle.setOnCheckedChangeListener(null);
        swToggle.setChecked(false);
        swNotif.setOnCheckedChangeListener(null);
        swNotif.setChecked(false);


    }

    public String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
//                getService(result);

            } else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


    class RequestTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            return GET(url);
        }

        @Override
        protected void onPostExecute(String s) {
            Monitor m = getService(s);
            setValues(m);
        }
    }

    class ToggleTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            InputStream inputStream = null;
            String result = "";
            try {

                String url = "http://emrahs.duckdns.org/rpitemp/set_onOff.php?param=setonoff";
                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
//                getService(result);
                    new RequestTask().execute();


                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;

        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG);
            Log.d("value:", s);
        }
    }

    class NotifTask extends AsyncTask<Boolean, Void, String> {


        @Override
        protected String doInBackground(Boolean... booleans) {
            boolean val = booleans[0];
            String url = "http://emrahs.duckdns.org/rpitemp/set_onOff.php?param=notif&value="+val;
            return GET(url);
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("value:", s);
        }
    }
}