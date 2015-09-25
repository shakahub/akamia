/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     MainActivityFragment
 * Purpose:     Scanning beacon and display found devices in the ListView
 * Created by:  John Hou
 * Created on:  7/13/2015
 */

package com.shaka.akamia;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.shaka.beaconlibrary.BleWrapper;
import com.shaka.beaconlibrary.BleWrapperUiCallbacks;

import java.lang.Runnable;
import java.util.Map;


public class MainActivityFragment extends ListFragment {

    private static final long SCANNING_TIMEOUT = 30 * 1000; /* 30 seconds */
    private static final long MONITOR_DELAY_TIME_INTERVAL = 15000; /* 15 seconds */
    private static final int ENABLE_BT_REQUEST_ID = 1;

    private static final int UNREGISTERED = -1;
    private static final int REGISTERED = 1;
    private static final int NEW_FOUND = 0;

    private Callbacks mCallbacks;
    private Handler mHandler = new Handler();
    private BleWrapper mBleWrapper = null;
    private BeaconListAdapter mBeaconListAdapter = null;

    private boolean mScanning = false;

    public interface Callbacks {
        void onDeviceSelected(BluetoothDevice device, String name);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //create BleWrapper
        mBleWrapper = new BleWrapper(getActivity(), new BleWrapperUiCallbacks.Null() {
            @Override
            public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record) {
                handleFoundDevice(device, rssi, record);
            }
        });

        mBleWrapper.initialize();
        mBeaconListAdapter = new BeaconListAdapter(getActivity());
        setListAdapter(mBeaconListAdapter);

        setRetainInstance(true);

        if (!mBleWrapper.checkBleHardwareAvailable()) {
            bleMissing();
        }

        //start a background thread to monitor mBeaconListAdapter
        monitorFoundDevices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_list, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // on every Resume check if BT is enabled
        // (user could turn it off while app was in background etc.)
        if(! mBleWrapper.isBtEnabled()) {
            // BT is not turned on - ask user to make it enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
            // see onActivityResult to check what is the status of our request
        }

        if (mBleWrapper != null) {
            // initialize BleWrapper object
            mBleWrapper.initialize();
        }

        if (mBeaconListAdapter == null) {
            mBeaconListAdapter = new BeaconListAdapter(getActivity());
            setListAdapter(mBeaconListAdapter);
        }

        // Automatically start scanning for devices
        mScanning = true;
        // remember to add timeout for scanning to not run it forever and drain the battery
        addScanningTimeout();
        mBleWrapper.startScanning();


        getActivity().invalidateOptionsMenu();

        //When you return to the list, you will immediately see your changes.
        mBeaconListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScanning = false;
        mBleWrapper.stopScanning();
        getActivity().invalidateOptionsMenu();

        mBeaconListAdapter.clearList();
    }

    /* add or update device to the current list of devices */
    private void handleFoundDevice(final BluetoothDevice device, final int rssi,
                                   final byte[] scanRecord)
    {
        if (device != null) {
            int result = mBeaconListAdapter.getRegStatus(device);

            switch(result) {
                case UNREGISTERED:
                    break;
                case REGISTERED:
                    // adding to the UI have to happen in UI thread
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBeaconListAdapter.addOrUpdateDevice(device, rssi, null, false);
                        }
                    });
                    break;
                case NEW_FOUND:
                    //call webservice to verify the found device whether was registered or not
                    final Map jsonMap = new BeaconFetchr().fetchBeacon(device.getAddress());

                    // adding to the UI have to happen in UI thread
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBeaconListAdapter.addOrUpdateDevice(device, rssi, jsonMap, true);
                        }
                    });
                    break;
            }
        }
    }

    /* A background thread to monitor the found devices */
    public void monitorFoundDevices() {
        //start a thread
        new Thread() {
            public void run() {
                //loop till mBeaconListAdapter has been skilled.
                while(mBeaconListAdapter != null) {
                    try {
                        //Since it will update UI, runOnUiThread is called here.
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < mBeaconListAdapter.getCount(); i++) {
                                    if (!mBleWrapper.checkDeviceConnection(mBeaconListAdapter.
                                            getDevice(i).getAddress()))
                                        mBeaconListAdapter.removeDevice(i);
                                }
                            }
                        });

                        Thread.sleep(MONITOR_DELAY_TIME_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.scanning, menu);

        if (mScanning) {
            menu.findItem(R.id.scanning_start).setVisible(false);
            menu.findItem(R.id.scanning_stop).setVisible(true);
            menu.findItem(R.id.scanning_indicator)
                    .setActionView(R.layout.progress_indicator);

        } else {
            menu.findItem(R.id.scanning_start).setVisible(true);
            menu.findItem(R.id.scanning_stop).setVisible(false);
            menu.findItem(R.id.scanning_indicator).setActionView(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scanning_start:
                if(! mBleWrapper.isBtEnabled()) {
                    // BT is not turned on - ask user to make it enabled
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
                }
                mScanning = true;
                mBeaconListAdapter.clearList();
                mBleWrapper.startScanning();
                break;
            case R.id.scanning_stop:
                mScanning = false;
                mBleWrapper.stopScanning();
                break;
            case R.id.show_log_item:
                if (mBeaconListAdapter != null)
                    //startLogMonitor();
                    break;
            default:
                return super.onOptionsItemSelected(item);
        }

        getActivity().invalidateOptionsMenu();
        return true;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        BluetoothDevice device = mBeaconListAdapter.getDevice(position);
        String name = mBeaconListAdapter.getName(position);

        mCallbacks.onDeviceSelected(device, name);
    }

    /* check if user agreed to enable BT */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // user didn't want to turn on BT
        if (requestCode == ENABLE_BT_REQUEST_ID) {
            if(resultCode == Activity.RESULT_CANCELED) {
                btDisabled();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* This function is used to control when to stop scanning devices */
    private void addScanningTimeout() {
        Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if(mBleWrapper == null) return;
                mScanning = false;
                mBleWrapper.stopScanning();
                getActivity().invalidateOptionsMenu();  //refresh menu bar
            }
        };
        mHandler.postDelayed(timeout, SCANNING_TIMEOUT);
    }

    private void btDisabled() {
        Toast.makeText(getActivity(), "Sorry, Bluetooth has to be turned ON for us to work!",
                Toast.LENGTH_LONG).show();
        getActivity().finish();
    }

    private void bleMissing() {
        Toast.makeText(getActivity(), "BLE Hardware is required but not available!",
                Toast.LENGTH_LONG).show();
        getActivity().finish();
    }
}
