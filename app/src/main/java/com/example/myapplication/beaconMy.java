package com.example.myapplication;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class beaconMy  {
    private Region beaconRegion = new Region("mybecon",null,null,null);//"B9407F30-F5F8-466E-AFF9-25556B57FE6D"
    private boolean entryMessageRaised;
    private static final String TAG = "beaconMonitorNotifier";
    public static boolean insideRegion = false;
    private BeaconManager beaconManager;

    private Map<String, Map<String,Double>> MapIbluetoothDevices = new HashMap<String, Map<String,Double>>();

    public  beaconMy (BeaconManager bbeaconManager){
        beaconManager = bbeaconManager ;
        beaconManager.getBeaconParsers().clear();

        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(" m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setForegroundScanPeriod(5000);
       // beaconManager.setDebug(true);

        beaconManager.addMonitorNotifier(new MonitorNotifier() {

            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
                Log.i(TAG,"didEnterTegion"+region.getUniqueId()+"beacon detected majon,minor:"+ region.getId1()+
                        ","+region.getId3());
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
                Log.i(TAG,"didEnterTegion"+region.getUniqueId()+"beacon detected majon,minor:"+ region.getId2()+
                        ","+region.getId3());
            }
            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);

            }
        });

        for (Region region: beaconManager.getMonitoredRegions()) {
            beaconManager.stopMonitoring(region);
            beaconManager.startMonitoring(region);
        }

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(!entryMessageRaised && beacons != null && !beacons.isEmpty()) {
                    for(Beacon beacon : beacons){

                        String tDevice =  beacon.getBluetoothAddress().toString();
                        Map<String,Double> Distance=new HashMap<String,Double>();
                        //txPower          mRssi
                        Distance.put("M",  beacon.getDistance());
                        MapIbluetoothDevices.put(tDevice, Distance);

                        Log.e("didRangeBeaconsInRegion",beacon.getBluetoothAddress()+" didEnterTegion"+region.getUniqueId()+"beacon detected majon,minor:"+ beacon.getTxPower()+
                                ","+beacon.getRssi()+" distance: " +beacon.getDistance());

                    }
                   // entryMessageRaised = true;


                }
            }
        });

       // beaconManager.startMonitoring(beaconRegion);
      //  beaconManager.startRangingBeacons(beaconRegion);
    }


    public void startBeaconMonitoring(){
        Log.d("start","startMonitoring");
        beaconManager.startMonitoring(beaconRegion);
        beaconManager.startRangingBeacons(beaconRegion);

    }
    public void stopBeaconMonitoring(){
        Log.d("stop","startMonitoring");
        beaconManager.stopMonitoring(beaconRegion);
        beaconManager.stopRangingBeacons(beaconRegion);

    }

    /*
    //функция определения дальности принимает txPower rssi
    public double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }
*/
    public Map<String,Map<String,Double>> getMapIbDevices(){
        return MapIbluetoothDevices;
    }

}
