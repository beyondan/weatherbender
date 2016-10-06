package com.wb.weatherbender;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import WeatherServiceProvider.WeatherService;


/**************************************************
 * Suggested hardware profile:                    *
 *   -  Screen size: 5.0in                        *
 *   -  Resolution: 1080 x 1920: 420dpi           *
 *   -  API: 24                                   *
 *   -  Target: Android 7.0 (Google APIs)         *
 *   -  CPU/ABI: x86_64                           *
 *   -  Size on Disk: > 1GB                       *
 *   -  Accelerometer enabled                     *
 **************************************************/
public class MainActivity extends AppCompatActivity implements SensorEventListener{
    // Adapter that returns a fragment for each of the days with relevant temperature data.
    private DaysPagerAdapter mDaysPagerAdapter;

    // ViewPager for smooth transition between fragments (aka tabs).
    private ViewPager mViewPager;

    /* Hardware sensors: ambient temperature and accelerometer. */
    private SensorManager mSensorManager;
    private Sensor mThermometer;
    private Sensor mAccelerometer;

    // Used for detecting shake motion.
    private float mAccel, mAccelCurrent, mAccelLast;

    // Tab layout for ViewPager.
    private TabLayout tabLayout;

    // Service that provides random temperatures for the week.
    private WeatherService weatherService;

    // TextView that displays ambient temperature.
    private TextView ambientTempText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherService = new WeatherService(this);
        ambientTempText = (TextView) findViewById(R.id.ambient_temperature);

        /* Set up sensors and relevant variables. */
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mThermometer = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        /* Set up the action bar. */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the days adapter.
        mDaysPagerAdapter = new DaysPagerAdapter(getSupportFragmentManager());

        /* Set up the ViewPager with the days adapter. */
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mDaysPagerAdapter);

        /* Create tab buttons for each of the day fragments. */
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Set up the unit convert button (C <-> F)
        final FloatingActionButton convertButton = (FloatingActionButton) findViewById(R.id.unit_converter);

        /* Initialize button to display opposite unit as currently set unit. */
        boolean isCelsius = weatherService.getUnit();
        if(isCelsius) {
            convertButton.setImageResource(R.drawable.fahrenheit);
        }
        else {
            convertButton.setImageResource(R.drawable.celsius);
        }

        /* Toggle unit (C <-> F) when the button is clicked. */
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isCelsius = weatherService.getUnit();
                isCelsius = !isCelsius;
                weatherService.setUnit(isCelsius);
                if(isCelsius) {
                    convertButton.setImageResource(R.drawable.fahrenheit);
                }
                else {
                    convertButton.setImageResource(R.drawable.celsius);
                }

                // Update view pager fragments with new temperature unit.
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override  /* Handle action bar item clicks here. */
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // If back button pressed on the action bar (it looks like list button in app).
        if (id == android.R.id.home) {
            Intent i = new Intent(getApplicationContext(), ListActivity.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                mViewPager.getAdapter().notifyDataSetChanged();
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
    public void onAccuracyChanged(Sensor sensor, int i) {}


    /**
     * A fragment that represents each tab.
     */
    public static class ViewPagerFragment extends Fragment {
        // 0 = MON, 1 = TUE, ... , 4 = FRI
        private static final String ARG_DAY_INDEX = "day_index";

        public ViewPagerFragment() {}

        public static ViewPagerFragment newInstance(int index) {
            ViewPagerFragment fragment = new ViewPagerFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_DAY_INDEX, index);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            WeatherService ws = new WeatherService(container.getContext());

            // Get current tab index.
            int dayIndex = getArguments().getInt(ARG_DAY_INDEX);

            /* Set components in this tab. */
            setCurrentTempText(rootView, ws.get(dayIndex) + ws.getUnitString(), ws.getC(dayIndex));
            setBackgroundImage(rootView, ws.getC(dayIndex));

            return rootView;
        }

        /* Set all TextViews with appropriate text/textColor depending on temperature. */
        private void setCurrentTempText(View view, String s, int tempC) {
            // Today's Weather:
            TextView placeholder = (TextView) view.findViewById(R.id.placeholder);

            // (temperature) + (temperature unit)
            TextView tempText = (TextView) view.findViewById(R.id.current_temperature);

            // HOT/WARM/CHILL/FREEZING
            TextView categoryText = (TextView) view.findViewById(R.id.category);

            // Small diagonal text
            TextView smallText = (TextView) view.findViewById(R.id.small_description);

            tempText.setText(s);
            if(tempC >= WeatherService.HOT_TEMP) {
                categoryText.setText("HOT");
                smallText.setText("GAHHHHHHH");
            }
            else if(tempC >= WeatherService.WARM_TEMP) {
                categoryText.setText("WARM");
                smallText.setText("O LALALA");
            }
            else if(tempC >= WeatherService.CHILL_TEMP) {
                categoryText.setText("CHILL");
                smallText.setText("Netflix?");
            }
            else {
                categoryText.setText("FREEZING");
                smallText.setText(". . . . . x_x");
            }

            int c = ImageUtils.getRGBFromC(tempC);
            placeholder.setTextColor(c);
            categoryText.setTextColor(c);
            tempText.setTextColor(c);
            smallText.setTextColor(c);
        }

        /* Set background with appropriate animation depending on temperature */
        private void setBackgroundImage(View view, int tempC) {
            ImageView img = (ImageView) view.findViewById(R.id.backgroundAnimation);
            if(tempC >= WeatherService.HOT_TEMP) {
                img.setBackgroundResource(R.drawable.hot_background);
            }
            else if(tempC >= WeatherService.WARM_TEMP) {
                img.setBackgroundResource(R.drawable.warm_background);
            }
            else if(tempC >= WeatherService.CHILL_TEMP) {
                img.setBackgroundResource(R.drawable.chill_background);
            }
            else {
                img.setBackgroundResource(R.drawable.cold_background);
            }
            AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
            frameAnimation.start();
        }
    }

    /**
     * A FragmentPagerAdapter that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class DaysPagerAdapter extends FragmentStatePagerAdapter {
        public DaysPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ViewPagerFragment f = ViewPagerFragment.newInstance(position);
            return f;
        }

        @Override
        public int getCount() {
            return WeatherService.NUMDAYS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            try{
                // Monday -> MON, Tuesday -> TUE, etc.
                return WeatherService.DAYS[position].substring(0,3);
            }
            catch(ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    // native file in ${Project}/app/src/main/jni
    static {
        System.loadLibrary("native-lib");
    }

    public native int convertCToF(int temperature);
}
