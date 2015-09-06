/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     MainActivity
 * Purpose:     This activity will create MainActivityFragment
 * Created by:  John Hou
 * Created on:  7/13/2015
 */
package com.shaka.akamia;

import android.bluetooth.BluetoothDevice;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.widget.Toast;


public class MainActivity extends SingleFragmentActivity
                implements MainActivityFragment.Callbacks {

    protected Fragment createFragment() {
        return new MainActivityFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // callback to MainActivityFragment
    public void onDeviceSelected(BluetoothDevice device, String sid) {
        Toast.makeText(this, "Ready to go", Toast.LENGTH_LONG).show();

        //You can start an instance of BeaconDetailActivity here

        //Intent i = new Intent(this. BeaconDetailActivity.class);
        //i.putExtra(BeaconDetailFragment.EXTRA_DEVICE_ADDRESS, device.getAddress());
        //startActivity(i);
    }
}
