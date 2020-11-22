package com.example.downthememorylane;

import  androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static ArrayList<String> places = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences= this.getSharedPreferences("com.example.downthememorylane",MODE_PRIVATE);
        ArrayList<String> latitudes= new ArrayList<>();
        ArrayList<String> longitudes= new ArrayList<>();

        places.clear();
        latitudes.clear();
        longitudes.clear();
        locations.clear();

        try {
            places= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<String>() )));
            latitudes= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<String>() )));
            longitudes= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<String>() )));
        }catch (Exception e){
            e.printStackTrace();
        }

        if (places.size()!=0 && latitudes.size() !=0 && longitudes.size()!=0){
            if(places.size()==latitudes.size() && places.size()==longitudes.size()){
                for (int i=0; i<latitudes.size();i++){
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
        }else {
            places.add("Add a places");
            locations.add(new LatLng(0,0));
        }

        ListView listPlaces = findViewById(R.id.listPlaces);

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);
        listPlaces.setAdapter(adapter);

        listPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), MemoryMap.class);
                intent.putExtra("Position", i);
                startActivity(intent);

            }
        });

    }
}