package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    //LE ARREGLARIA UN PAR DE COSAS EN PRINCIPIO: QUE SE PUEDA ELIMINAR LAS LOCATIONS, QUE VUELVA A LA MAIN SCREEN CUANDO GUARDE LA LOCATION (NO QUE TIRE UN TOAST),
    //MISMO PROBLEMA CON LA UBICACION DEL TELEFONO (O CAMBIAS A COARSE O BUSCACS COMO SE ARREGLA), TAMPOCO ME ABRE LA UBICACION CUANDO LA APRETO

    public void updateLocationInfo (Location location) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if (addressList != null && addressList.size() > 0) {
                String address = "";

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

                MainActivity.memorablePlaces.add(address);
                MainActivity.locations.add(latLng);
                MainActivity.arrayAdapter.notifyDataSetChanged();
                Toast.makeText(MapsActivity.this, "Location saved!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
