package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    static ArrayList<String> memorablePlaces = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter<String> arrayAdapter;
    //Necesito el static para que este en ambas actividades

    public void goNext (View view) {

        Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
        startActivity(intent);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);


        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
        ArrayList<String> latitudes = new ArrayList<String>();
        ArrayList<String> longitudes = new ArrayList<String>();

        memorablePlaces.clear();
        locations.clear();
        latitudes.clear();
        longitudes.clear();

        try {
            memorablePlaces = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("memorablePlaces", ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lats", ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lons", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (Exception e){
            e.printStackTrace();
        }

        if (memorablePlaces.size() > 0 && longitudes.size() > 0 && latitudes.size() > 0) {
            if (memorablePlaces.size() == longitudes.size() && memorablePlaces.size() == latitudes.size()) {
                for (int i = 0; i < memorablePlaces.size(); i++) {
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i))));
                }
            }
        }

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, memorablePlaces);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent lookForLocationIntent = new Intent(getApplicationContext(),MapsActivity.class);
                lookForLocationIntent.putExtra("placeNumber", i);
                startActivity(lookForLocationIntent);
            }
        });
    }
}
