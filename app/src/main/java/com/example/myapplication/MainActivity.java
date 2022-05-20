package com.example.myapplication;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.altbeacon.beacon.BeaconManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    protected static final String TAG = "MonitoringActivity";
    private beaconMy beaconManager;
    private int REQUEST_ENABLE_BT = 1;
    private BluetoothManager BLManager;
    private Bl bl;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private boolean permissionDenied = false;
    private Map<String, Map<String,Double>> MapIbluetoothDevices;
    private Map<String, Map<String,Double>> MapFicsedData;
    private boolean FicsedData = false;
    private stateAdapter myAdapter;
    ListView listView;

    Button Button ;

    private int WhatTurnOn;

    Handler h = new Handler();
    //функция повторяющее действие каждые 10 секунд
    Runnable run = new Runnable() {

        @Override
        public void run() {
            if(FicsedData){
                testPlace();
            }
            //сканирум
            MapIbluetoothDevices = beaconManager.getMapIbDevices();
            beaconManager.stopBeaconMonitoring();

            beaconManager.startBeaconMonitoring();
            //bl.scanLeDevice();
            //обновляем map        beaconManager

            myAdapter.notifyDataSetChanged();

            h.postDelayed(this, 10 * 1000);
        }
    };




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button = findViewById(R.id.button1);
        WhatTurnOn = 1;
        switch(WhatTurnOn){
            case 1:
                initScaner();
                break;
            case 2:
                initDranslater();
                break;
        }


        //проверяем что у нашего приложения есть нужное разрешение
        enableMyLocation();
        requestPermissions();




       // BLManager = getSystemService(BluetoothManager.class);
        //класс работы с блютус
        //bl = new Bl(BLManager);

       // MapIbluetoothDevices = bl.getMapIbDevices();

        //если не включен спрашиваем разришение на включение
/*
        if (!bl.adapter().isEnabled()) {
            //это не разрешение на блютус а само разрешение по другому.requestPermissions();
            Intent enableBtIntent = new Intent(bl.adapter().ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }else{
            init();
        }
*/
    }

    private void testPlace() {
        int intecatorPlace = 0;

        for ( Map.Entry<String,  Map<String,Double>> entry : MapIbluetoothDevices.entrySet()) {
            if(  MapFicsedData.containsKey(entry.getKey())){
                Map<String,Double> dataFics =  MapFicsedData.get(entry.getKey());
                Map<String,Double> dataNotFics = MapIbluetoothDevices.get(entry.getKey());
                boolean deaposon1 = (dataFics.get("M") - .5) <= dataNotFics.get("M") ;
                boolean deaposon2 = (dataFics.get("M") + .5) >= dataNotFics.get("M") ;
                double d = dataFics.get("M") - 0.5;
                double d2 = dataFics.get("M") + 0.5;
                Log.e("MyPlace",""+deaposon1+" "+deaposon2);
                Log.e("MyPlace",""+dataFics.get("M")+" "+d+ " " +d2);
                if( deaposon2 && deaposon1 ){
                    intecatorPlace++;
                }
            }//+ "/" + entry.getValue();
        }

        int SizeMapFics = MapFicsedData.size();
        switch(SizeMapFics){
            case 1:

                if(intecatorPlace == SizeMapFics){
                    SetTrueStatus();
                }else {
                    SetFalseStatus();
                }

                break;
            default:
                if(intecatorPlace>=MapFicsedData.size()/2){
                    SetTrueStatus();
                }else{
                    SetFalseStatus();
                }
                break;
        }


    }
     public void SetTrueStatus(){
         Button.setText("Yes.This is my place");
         Button.setBackgroundResource(R.color.green);
     }

     public void  SetFalseStatus(){
         Button.setText("Not.This is not my place");
         Button.setBackgroundResource(R.color.purple_700);
     }
    public void MyPlace(View view){
        Log.e("MyPlace","Click");
        if(MapIbluetoothDevices.size() >= 1){

            MapFicsedData =  new HashMap<String, Map<String,Double>>(MapIbluetoothDevices);
            FicsedData = true;
            Button.setText("Place Fix");
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initScaner(){
        beaconManager = new beaconMy(org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this));
        MapIbluetoothDevices = beaconManager.getMapIbDevices();
        beaconManager.startBeaconMonitoring();
        //получили наш объект списка в разметке
        listView = findViewById(R.id.listIB);
        myAdapter = new stateAdapter(this, MapIbluetoothDevices);
        listView.setAdapter(myAdapter);
        init();
    }

    private void initDranslater(){
        DranslaitorBeacon DranslaitorBeacon = new DranslaitorBeacon(getApplicationContext());

    }


    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("This app needs background location access");
                            builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @TargetApi(23)
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                            PERMISSION_REQUEST_BACKGROUND_LOCATION);
                                }

                            });
                            builder.show();
                        }
                        else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Functionality limited");
                            builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                }

                            });
                            builder.show();
                        }
                    }
                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "background location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    //что должно быть запущено при запуске приложения
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void init(){

       // bl.init();
        run.run();
    }

    //запрос разрешения локации
    private void enableMyLocation() {
        //проверка на наличие разрешения
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    //проверка на получения разрешения
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        //если мы получили нужное разрешение то влкючаем блютус
        if(requestCode == REQUEST_ENABLE_BT){
            init();
        };


    }

}