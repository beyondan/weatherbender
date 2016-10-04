package WeatherServiceProvider;

import java.util.Random;
import java.util.ServiceLoader;

/**
 * Singleton weather service class.
 * Provides daily average temperatures of the current week.
 */

public class WeatherService {
    // MON, TUE, WED, THU, FRI
    private final int NUMDAYS = 5;
    // Preset mean and std dev of the week's temperature in celsius.
    private final double TEMP_MEAN = 20.0;
    private final double TEMP_STD = 3.0;
    private final String DEGREE = " \u00b0";
    private boolean isCelsius = true;

    // Array to store daily average temperatures.
    private double[] tempArray;

    private Random randomGenerator;
    private static WeatherService service = null;

    private WeatherService() {
        tempArray = new double[NUMDAYS];
        randomGenerator = new Random(System.currentTimeMillis());

        for(int i=0; i < NUMDAYS; i++) {
            tempArray[i] = randomGenerator.nextGaussian()*TEMP_STD + TEMP_MEAN;
        }
    }

    public static synchronized WeatherService getInstance() {
        if(service == null) {
            return new WeatherService();
        }
        return service;
    }

    public double get(int dayIndex) {
        return tempArray[dayIndex];
    }

    public double[] getAll() {
        return tempArray;
    }

    public String getUnit() {
        if(isCelsius) {
            return DEGREE + "C";
        }
        else {
            return DEGREE + "F";
        }
    }
}
