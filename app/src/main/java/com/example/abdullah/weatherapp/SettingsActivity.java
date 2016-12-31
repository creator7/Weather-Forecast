package com.example.abdullah.weatherapp;

import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by Abdullah on 12/10/2016.
 */

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);


        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
