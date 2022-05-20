package com.example.myapplication;



import static android.app.PendingIntent.getActivity;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import android.app.AutomaticZenRule;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.logging.LogRecord;

public class Bl {

    private BluetoothLeScanner BLScanner;
    private Handler handler;
    private BluetoothManager BLManager;
    private BluetoothAdapter BLAdapter;
    private ArrayList scanResultlist;
    private boolean scanning;
    private static final long SCAN_PERIOD = 5 * 1000;
    private ScanFilter mScanFilter;
    private  ScanSettings mScanSettings;
    private Map<String,Map<String,Double>> MapIbluetoothDevices = new HashMap<String, Map<String,Double>>();


    protected  Bl (BluetoothManager LBManager){

        BLManager = LBManager;
        BLAdapter = BLManager.getAdapter();
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void init(){
        Log.e("E1","init start");
      if(BLAdapter.isEnabled()){
          BLScanner = BLAdapter.getBluetoothLeScanner();
          handler = new Handler() ;
          setScanFilter();
          setScanSettings();
      }
    }




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

    protected ScanCallback mScanCallback = new ScanCallback()
    {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
        //Log.e("E2","ScanCallback start");
        ScanRecord mScanRecord = result.getScanRecord();
        byte[] manufacturerData = mScanRecord.getManufacturerSpecificData(224);
        int mRssi = result.getRssi();
        int txPower = result.getTxPower();
        startFetch(result.getDevice());
        String tDevice =  result.getDevice().getName();
        Map<String,Double> Distance=new HashMap<String,Double>();
        Distance.put("M",  calculateDistance(txPower,mRssi) );
        MapIbluetoothDevices.put(tDevice, Distance);
        if(result.getDevice().getName()!= null)
        Log.e("Е", "Remote device name: " + result.getDevice().getName());
        }
    };

    public Map<String,Map<String,Double>> getMapIbDevices(){
        return MapIbluetoothDevices;
    }

    //сканируем SCAN_PERIOD времини потом останавливаем сканирование
    public void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    BLScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            Log.e("E3","scanLeDevice " + scanning );
                                //Arrays.asList(mScanFilter)
            BLScanner.startScan(null, mScanSettings, mScanCallback);
        } else {
            scanning = false;
            BLScanner.stopScan(leScanCallback);
        }
    }
    public static void startFetch( BluetoothDevice device ) {
        // Need to use reflection prior to API 15
        Class cl = null;
        try {
            cl = Class.forName("android.bluetooth.BluetoothDevice");
        } catch( ClassNotFoundException exc ) {
            Log.e("CTAG", "android.bluetooth.BluetoothDevice not found." );
        }
        if (null != cl) {
            Class[] param = {};
            Method method = null;
            try {
                method = cl.getMethod("fetchUuidsWithSdp", param);
            } catch( NoSuchMethodException exc ) {
                Log.e("CTAG", "fetchUuidsWithSdp not found." );
            }
            if (null != method) {
                Object[] args = {};
                try {
                    method.invoke(device, args);
                } catch (Exception exc) {
                    Log.e("CTAG", "Failed to invoke fetchUuidsWithSdp method." );
                }
            }
        }
    }




    private void setScanFilter() {
        ScanFilter.Builder mBuilder = new ScanFilter.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
        ByteBuffer mManufacturerDataMask = ByteBuffer.allocate(24);
        //1)this true - B9407F30-F5F8-466E-AFF9-25556B57FE6D ,0CF052C2-97CA-407C-84F8-B62AAC4E9020
        byte[] uuid = getIdAsByte(UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"));
        mManufacturerData.put(0, (byte)0xBE);
        mManufacturerData.put(1, (byte)0xAC);
        for (int i=2; i<=17; i++) {
            mManufacturerData.put(i, uuid[i-2]);
        }
        for (int i=0; i<=17; i++) {
            mManufacturerDataMask.put((byte)0x01);
        }
        mBuilder.setManufacturerData(224, mManufacturerData.array(), mManufacturerDataMask.array());
        mScanFilter = mBuilder.build();
    }

    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        //Определяет метод и время сканирования
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // контролирует как будет вызываться callback со ScanResult
            mBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);//каждые 200-500мс
            //как Android определяет «совпадения».
            mBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            //определяет сколько advertisement данных необходимо для совпадения.
            mBuilder.setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT);//Одного пакета достаточно.
        }
        mBuilder.setReportDelay(0L);

        mScanSettings = mBuilder.build();
    }

    private final ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    scanResultlist.add(result.getDevice());
                    //Log.e("Е", "Remote device name: " + result.getDevice().getName());
                }
            };





    public BluetoothAdapter adapter(){
        return BLAdapter;
    }

    public byte[] getIdAsByte(UUID uuid)
    {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }


}
