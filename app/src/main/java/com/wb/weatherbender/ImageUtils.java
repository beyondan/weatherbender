package com.wb.weatherbender;

import android.graphics.Color;

public class ImageUtils {
    /**
     * Function to map a -20 to 40 degree (Celsius) temperature to a color.
     *
     * @param temperature (celsius)
     * @return ready to use Android Color
     *
     * Source: http://stackoverflow.com/a/16469279
     */
    public static int getRGBFromC(int temperature) {

        // Map the temperature to a 0-1 range
        double a = (temperature + 20) / (double) 60;
        a = (a < 0) ? 0 : ((a > 1) ? 1 : a);

        // Scrunch the green/cyan range in the middle
        int sign = (a < .5) ? -1 : 1;
        a = sign * Math.pow(2 * Math.abs(a - .5), .35)/2 + .5;

        // Linear interpolation between the cold and hot
        int h0 = 259;
        int h1 = 12;
        double h = (h0) * (1 - a) + (h1) * (a);
        float hue = (float) h;

        return Color.HSVToColor(new float[] {hue, 75, 90});
    }
}