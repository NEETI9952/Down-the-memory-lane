package com.example.downthememorylane;

import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MemoryMap extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    LocationManager locationManager;
    LocationListener locationListener;
    private GoogleMap mMap;

    public void goToMap(Location location,String title){
        if(location!=null){
            LatLng userLocation= new LatLng(location.getLatitude(),location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,10));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                goToMap(lastKnownLocation,"Your Location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent= getIntent();
        if(intent.getIntExtra("Position",0)==0){
            //Zoom users Location
            locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    goToMap(location,"your location");
                }
            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastKnownLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                goToMap(lastKnownLocation,"Your Location");
            }else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }

        }else{
            Location placeLocation= new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("Position",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("Position",0)).longitude);

            goToMap(placeLocation,MainActivity.places.get(intent.getIntExtra("Position",0)));
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder= new Geocoder(getApplicationContext(), Locale.getDefault());
        String address ="";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if(addressList !=null &&addressList.size()>0){
                Log.i("Address",addressList.get(0).getAddressLine(0));
                Toast.makeText(MemoryMap.this, addressList.get(0).getAddressLine(0) , Toast.LENGTH_SHORT).show();
                address += addressList.get(0).getAddressLine(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
//                Toast.makeText(MapsActivity.this, "My Current Location:" + userLocation, Toast.LENGTH_SHORT).show();

//            if(addressList !=null && addressList.size()>0){
//                if(addressList.get(0).getThoroughfare()!=null) {
//                    address += addressList.get(0).getThoroughfare() + " ";
//                    if (addressList.get(0).getSubThoroughfare() != null) {
//                        address += addressList.get(0).getSubThoroughfare() + " ";
//                    }
//                    Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
//                    Log.i("Address",addressList.get(0).getAddressLine(0));
//                }
//            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//            if (addressList != null && addressList.size() > 0) {
//                address += addressList.get(0).getAddressLine(0);
//            }
//            Log.i("Address", addressList.get(0).getAddressLine(0));
//            Toast.makeText(MemoryMap.this, addressList.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        if (address.equals("")) {
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm yyyy-MM-dd",Locale.getDefault());
            address += sdf.format(new Date());
        }
//        Toast.makeText(this,address,Toast.LENGTH_LONG).show();

        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.adapter.notifyDataSetChanged();

        SharedPreferences sharedPreferences= this.getSharedPreferences("com.example.downthememorylane",MODE_PRIVATE);

        try {
            ArrayList<String> latitudes= new ArrayList<>();
            ArrayList<String> longitudes= new ArrayList<>();

            for (LatLng coordinates:MainActivity.locations){
                latitudes.add(Double.toString(coordinates.latitude));
                longitudes.add(Double.toString(coordinates.longitude));
            }

            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
            sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();

        }catch (Exception e){
            e.printStackTrace();

        }

        Toast.makeText(this,"Location saved",Toast.LENGTH_SHORT).show();

    }
}