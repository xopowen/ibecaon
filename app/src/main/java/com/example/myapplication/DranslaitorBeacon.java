package com.example.myapplication;

import android.content.Context;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

public class DranslaitorBeacon {

    DranslaitorBeacon( Context Context){
        Beacon beacon = new Beacon.Builder()
                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa7")
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x004c)
                .setTxPower(-59)
                .build();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(Context, beaconParser);

        beaconTransmitter.startAdvertising(beacon);

    }


}
