package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    //LE ARREGLARIA UN PAR DE COSAS EN PRINCIPIO: QUE SE PUEDA ELIMINAR LAS LOCATIONS, QUE VUELVA A LA MAIN SCREEN CUANDO GUARDE LA LOCATION (NO QUE TIRE UN TOAST),
    //MISMO PROBLEMA CON LA UBICACION DEL TELEFONO (O CAMBIAS A COARSE O BUSCACS COMO SE ARREGLA), TAMPOCO ME ABRE LA UBICACION CUANDO LA APRETO


    public void centerMap(Location location, String title){
        if(location != null){
            LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,12));
        }
    }

    public void updateLocationInfo (Location location) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if (addressList != null && addressList.size() > 0) {

                if (addressList.get(0).getThoroughfare() != null) {
                    address += addressList.get(0).getThoroughfare() + " ";
                }


                if (addressList.get(0).getPostalCode() != null) {
                    address += addressList.get(0).getPostalCode() + " ";
                }


                if (addressList.get(0).getLocality() != null) {
                    address += addressList.get(0).getLocality() + " ";
                }

                if (addressList.get(0).getAdminArea() != null) {
                    address += addressList.get(0).getAdminArea();
                }

                //Esto ahora lo tengo que llevar a la otra actividad

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (address.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
            address += sdf.format(new Date());
        }

        MainActivity.memorablePlaces.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();
        storeData();
    }

    public void storeData (){
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
        try{
            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();

            for (LatLng coord : MainActivity.locations){
                latitudes.add(Double.toString(coord.latitude));
                longitudes.add(Double.toString(coord.longitude));
            }
            sharedPreferences.edit().putString("memorablePlaces",ObjectSerializer.serialize(MainActivity.memorablePlaces)).apply();
            sharedPreferences.edit().putString("lats",ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("lons",ObjectSerializer.serialize(longitudes)).apply();
        } catch (Exception e){
            e.printStackTrace();
        }
        Toast.makeText(this, "Location saved!", Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setOnMapLongClickListener(this);


        Intent intent = getIntent();
        int placeNumber = intent.getIntExtra("placeNumber",-1);
        Log.i("tapped", Integer.toString(placeNumber));
        if (placeNumber == -1){

            Toast.makeText(this, "Long press to save location", Toast.LENGTH_SHORT).show();

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mMap.clear();
                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title("You are here"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},0);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateLocationInfo(lastKnownLocation);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,20,locationListener);
            }
        } else {

            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(placeNumber).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(placeNumber).longitude);

            centerMap(placeLocation, MainActivity.memorablePlaces.get(placeNumber));

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,20,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateLocationInfo(lastKnownLocation);
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        Location location = new Location("Location Clicked");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        updateLocationInfo(location);
    }
}
