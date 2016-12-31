package com.example.abdullah.weatherapp;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.abdullah.weatherapp.data.Preferences;
import com.example.abdullah.weatherapp.utilities.MyDateUtils;
import com.example.abdullah.weatherapp.utilities.WeatherUtils;

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.AdapterViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private final Context context;
    private Cursor cursor;
    private boolean mUseTodayLayout;

    private final AdapterOnClickHandler onClickHandler;

    public ViewAdapter(@NonNull Context context, AdapterOnClickHandler clickHandler){
        onClickHandler = clickHandler;
        this.context = context;
        mUseTodayLayout = context.getResources().getBoolean(R.bool.use_today_layout);
    }

    public interface AdapterOnClickHandler{
        void onClick(long date);
    }

    @Override
    public AdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;

        switch (viewType) {

//          COMPLETED (12) If the view type of the layout is today, use today layout
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_today;
                break;
            }

//          COMPLETED (13) If the view type of the layout is future day, use future day layout
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item;
                break;
            }

//          COMPLETED (14) Otherwise, throw an IllegalArgumentException
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        view.setFocusable(true);
        return new AdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewAdapter.AdapterViewHolder holder, int position) {
        cursor.moveToPosition(position);

        int viewType = getItemViewType(position);
        int weatherId = cursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);

        int weatherImageId;

        switch (viewType) {
//          COMPLETED (15) If the view type of the layout is today, display a large icon
            case VIEW_TYPE_TODAY:
                weatherImageId = WeatherUtils
                        .getLargeArtResourceIdForWeatherCondition(weatherId);
                String tempUnit;
                if(Preferences.isMetric(context))
                    tempUnit = context.getResources().getString(R.string.temperature_celsius);
                else
                    tempUnit = context.getResources().getString(R.string.temperature_farenheit);
                holder.tempUnit.setText(tempUnit);
                break;

//          COMPLETED (16) If the view type of the layout is today, display a small icon
            case VIEW_TYPE_FUTURE_DAY:
                weatherImageId = WeatherUtils
                        .getSmallArtResourceIdForWeatherCondition(weatherId);
                break;

//          COMPLETED (17) Otherwise, throw an IllegalArgumentException
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }


        long dateInMillis = cursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        String dateString = MyDateUtils.getFriendlyDateString(context,dateInMillis,false);
        holder.dateView.setText(dateString);

        String description = WeatherUtils.getStringForWeatherCondition(context,weatherId);
        String descriptionA11y = context.getString(R.string.a11y_forecast, description);

         /* Set the text and content description (for accessibility purposes) */
        holder.descriptionView.setText(description);
        holder.descriptionView.setContentDescription(descriptionA11y);

        holder.iconView.setImageResource(weatherImageId);

        double highInCelsius = cursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
         /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        String highString = WeatherUtils.formatTemperature(context, highInCelsius);
         /* Create the accessibility (a11y) String from the weather description */
        String highA11y = context.getString(R.string.a11y_high_temp, highString);

         /* Set the text and content description (for accessibility purposes) */
        holder.highTempView.setText(highString);
        holder.highTempView.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/
         /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = cursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
         /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        String lowString = WeatherUtils.formatTemperature(context, lowInCelsius);
        String lowA11y = context.getString(R.string.a11y_low_temp, lowString);

         /* Set the text and content description (for accessibility purposes) */
        holder.lowTempView.setText(lowString);
        holder.lowTempView.setContentDescription(lowA11y);



    }

    @Override
    public int getItemCount() {
        if (null == cursor) return 0;
        return cursor.getCount();
    }

    @Override
    public int getItemViewType(int position){
        if (mUseTodayLayout && position == 0) {
            return VIEW_TYPE_TODAY;
//      COMPLETED (11) Otherwise, return the ID for future day viewType
        } else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }

    void swapCursor(Cursor newCursor) {
        cursor = newCursor;
        notifyDataSetChanged();
    }

    class AdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;
        final TextView tempUnit;

        //      COMPLETED (5) Add an ImageView for the weather icon
        final ImageView iconView;

        AdapterViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.weather_icon);
            dateView = (TextView) view.findViewById(R.id.date);
            descriptionView = (TextView) view.findViewById(R.id.weather_description);
            highTempView = (TextView) view.findViewById(R.id.high_temperature);
            lowTempView = (TextView) view.findViewById(R.id.low_temperature);
            tempUnit = (TextView)view.findViewById(R.id.temperatureUnit);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            long dateInMillis = cursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            onClickHandler.onClick(dateInMillis);
        }
    }
}
