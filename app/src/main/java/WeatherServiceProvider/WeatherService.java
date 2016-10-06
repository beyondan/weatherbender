package WeatherServiceProvider;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

/**
 * Weather service class that provides and manages randomly generated temperatures
 * of a week.
 */

public class WeatherService {
    public static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    public static final int NUMDAYS = DAYS.length;

    // Boundary temperatures for hot, warm, chill, and cold weathers.
    public static final int HOT_TEMP = 30, WARM_TEMP = 15, CHILL_TEMP = 5;

    /**
     * Preset mean and std dev of the week's temperature in celsius.
     * Used for Gaussian random generator.
     */
    //private final double TEMP_MEAN = 0.0;
    //private final double TEMP_STD = 15.0;

    // The degree symbol.
    private final String DEGREE = " \u00b0";

    // SharedPreferences name
    private static final String PREFS_NAME = "WEATHER_PREFS";

    // Used for checking if there's anything in SharedPreferences.
    private static final String PREFS_EMPTY = "IS_PREFS_EMPTY";

    // Used for saving/getting lastly set temperature unit.
    private static final String UNIT_KEY = "IS_CELSIUS";

    private Context context;

    public WeatherService(Context context) {
        this.context = context;
        populateData();
    }

    public void populateData() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        /* If SharedPreferences is empty, generate random temperature data and store them */
        boolean isEmpty = settings.getBoolean(PREFS_EMPTY, true);
        if(isEmpty) {
            SharedPreferences.Editor editor = settings.edit();

            Random randomGenerator = new Random(System.currentTimeMillis());
            for (int i = 0; i < NUMDAYS; i++) {
                //int temp = (int) Math.floor(randomGenerator.nextGaussian() * TEMP_STD + TEMP_MEAN);

                // Uniformly random integer between -20 and 40.
                int temp = randomGenerator.nextInt(60) - 20;
                editor.putInt(DAYS[i], temp).commit();
            }

            /* TEST INPUTS GO HERE. Comment the above for-loop before using this.
            editor.putInt(DAYS[0], 30).commit();
            editor.putInt(DAYS[1], 23).commit();
            editor.putInt(DAYS[2], 15).commit();
            editor.putInt(DAYS[3], 11).commit();
            editor.putInt(DAYS[4], 3).commit();*/

            editor.putBoolean(PREFS_EMPTY, false).commit();
        }
    }

    /* Automatically convert unit and return i-th day's temperature */
    public int get(int i) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isCelsius = settings.getBoolean(UNIT_KEY, true);

        int temp = settings.getInt(DAYS[i], 0);
        if(isCelsius) {
            return temp;
        }
        else {
            return convertCtoF(temp);
        }
    }

    /* Return i-th day's temperature in Celsius */
    public int getC(int i) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int temp = settings.getInt(DAYS[i], 0);
        return temp;
    }

    /* Set the temperature unit */
    public void setUnit(boolean isCelsius) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(UNIT_KEY, isCelsius).commit();
    }

    /* Return the currently set temperature unit */
    public boolean getUnit() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(UNIT_KEY, true);
    }

    /* Return the currently set temperature unit in string format */
    public String getUnitString() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isCelsius = settings.getBoolean(UNIT_KEY, true);
        if(isCelsius) {
            return DEGREE + "C";
        }
        else {
            return DEGREE + "F";
        }
    }

    /* Regenerate all random temperatures */
    public void reset() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isCelsius = settings.getBoolean(UNIT_KEY, true);
        settings.edit().clear().commit();
        settings.edit().putBoolean(UNIT_KEY,isCelsius).commit();
        populateData();
    }

    // native file in ${Project}/app/src/main/jni
    static {
        System.loadLibrary("native-lib");
    }

    public native int[] convertCtoFArray(int[] arr);
    public native int convertCtoF(int cTemp);
}
