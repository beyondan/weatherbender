package com.wb.weatherbender;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import WeatherServiceProvider.WeatherService;

/**
 * Adapter for RecyclerView in ListActivity.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private String[] days = WeatherService.DAYS;
    private WeatherService weatherService;

    // Constructor that sets up WeatherService with appropriate context.
    public RecyclerViewAdapter(Context context) {
        weatherService = new WeatherService(context);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        // Monday, Tuesday, Wednesday, etc.
        holder.dayTextView.setText(" " + days[i]);

        // Temperature data text.
        holder.infoTextView.setText(weatherService.get(i)+weatherService.getUnitString());

        int tempC = weatherService.getC(i);

        /* Set small description texts according to temperature. */
        if(tempC >= WeatherService.HOT_TEMP) {
            holder.smallTextView.setText("...it's very hot");
        }
        else if(tempC >= WeatherService.WARM_TEMP) {
            holder.smallTextView.setText("...it's quite warm");
        }
        else if(tempC >= WeatherService.CHILL_TEMP) {
            holder.smallTextView.setText("...it's pretty chill");
        }
        else {
            holder.smallTextView.setText("...it's freezing");
        }

        /* Set text colors according to temperature */
        int c = ImageUtils.getRGBFromC(tempC);
        holder.dayTextView.setTextColor(c);
        holder.infoTextView.setTextColor(c);
        holder.smallTextView.setTextColor(c);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return days.length;
    }

    /* Provide a reference to the views for each data item */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dayTextView, infoTextView, smallTextView;

        public ViewHolder(View v) {
            super(v);
            dayTextView = (TextView) v.findViewById(R.id.dayOfWeek);
            infoTextView = (TextView) v.findViewById(R.id.temperature);
            smallTextView = (TextView) v.findViewById(R.id.small_description);
        }
    }
}
