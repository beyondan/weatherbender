package com.wb.weatherbender;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import WeatherServiceProvider.WeatherService;

public class ListActivity extends AppCompatActivity implements SensorEventListener {
    /* RecyclerView that holds Cards (aka list items). */
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    /* Hardware sensors: ambient temperature and accelerometer. */
    private SensorManager mSensorManager;
    private Sensor mThermometer;
    private Sensor mAccelerometer;

    // Used for detecting shake motion.
    private float mAccel, mAccelCurrent, mAccelLast;

    // Service that provides random temperatures for the week.
    private static WeatherService weatherService;

    // TextView that displays ambient temperature.
    private TextView ambientTempText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        weatherService = new WeatherService(this);
        ambientTempText = (TextView) findViewById(R.id.ambient_temperature);

        /* Set up sensors and relevant variables. */
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mThermometer = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        // Set up the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Set up RecyclerView. It always has fixed size (5 cards). */
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        /* Use a linear layout manager for the RecyclerView. */
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        /* Specify an adapter for the RecyclerView. */
        mAdapter = new RecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        // Set up the unit convert button (C <-> F)
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.unit_converter);

        /* Initialize button to display opposite unit as currently set unit. */
        boolean isCelsius = weatherService.getUnit();
        if(isCelsius) {
            fab.setImageResource(R.drawable.fahrenheit);
        }
        else {
            fab.setImageResource(R.drawable.celsius);
        }

        /* Toggle unit (C <-> F) when the button is clicked. */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isCelsius = weatherService.getUnit();
                isCelsius = !isCelsius;
                weatherService.setUnit(isCelsius);
                if(isCelsius) {
                    fab.setImageResource(R.drawable.fahrenheit);
                }
                else {
                    fab.setImageResource(R.drawable.celsius);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override /* Handle action bar item clicks here. */
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // If back button pressed on the action bar (it looks like list button in app).
        if (id == android.R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mThermometer != null) {
            mSensorManager.registerListener(this, mThermometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(mAccelerometer != null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mThermometer != null || mAccelerometer != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;

        /* Shake detector. Source: http://stackoverflow.com/a/2318356  */
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            /* If shake detected, reset all temperature data with new random values. */
            if(mAccel > 12) {
                weatherService.reset();
                mAdapter.notifyDataSetChanged();
            }
        }

        /* Ambient temperature sensor event. */
        else if(sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            int temperature = (int) sensorEvent.values[0];
            int c = ImageUtils.getRGBFromC(temperature);
            if (!weatherService.getUnit()) {
                temperature = convertCToF(temperature);
            }
            ambientTempText.setText("Ambient temperature: " + temperature
                    + weatherService.getUnitString());
            ambientTempText.setTextColor(c);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // native file in ${Project}/app/src/main/jni
    static {
        System.loadLibrary("native-lib");
    }

    public native int convertCToF(int temperature);
}
