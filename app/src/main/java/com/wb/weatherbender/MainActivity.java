package com.wb.weatherbender;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import WeatherServiceProvider.WeatherService;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    // Adapter that returns a fragment for each of the days with relevant temperature data.
    private DaysPagerAdapter mDaysPagerAdapter;

    // ViewPager for smooth transition between fragments (aka tabs).
    private ViewPager mViewPager;

    private SensorManager mSensorManager;
    private Sensor mThermometer;

    private TextView ambientTempText;

    private WeatherService weatherService = WeatherService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mThermometer = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        ambientTempText = (TextView) findViewById(R.id.ambient_temperature);

        // Set up the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the days adapter.
        mDaysPagerAdapter = new DaysPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the days adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mDaysPagerAdapter);

        // Create tab buttons for each of the day fragments.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mThermometer != null) {
            mSensorManager.registerListener(this, mThermometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mThermometer != null) {
            mSensorManager.unregisterListener(this);
        }
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        ambientTempText.setText("Ambient temperature: " + sensorEvent.values[0] + weatherService.getUnit());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    /**
     * A fragment that represents each tab.
     */
    public static class ViewPagerFragment extends Fragment {
        // Fragment argument representing the day of the week it's responsible for.
        private static final String ARG_DAY_INDEX = "day_index";
        private WeatherService weatherService = WeatherService.getInstance();
        private double ambientTemp = 0.0;

        public ViewPagerFragment() {

        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
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
            TextView currentTempText = (TextView) rootView.findViewById(R.id.current_temperature);
            int dayIndex = getArguments().getInt(ARG_DAY_INDEX);
            currentTempText.setText("" + Math.round(weatherService.get(dayIndex)) + weatherService.getUnit());
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class DaysPagerAdapter extends FragmentPagerAdapter {

        private String[] DAYS = {"MON", "TUE", "WED", "THU", "FRI"};

        public DaysPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ViewPagerFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return DAYS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            try{
                return DAYS[position];
            }
            catch(ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }
}
