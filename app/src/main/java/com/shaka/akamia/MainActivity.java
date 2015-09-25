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
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
//import android.widget.Toast;


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
    public void onDeviceSelected(BluetoothDevice device, String name) {
        //Toast.makeText(this, "User selected one found meeting room", Toast.LENGTH_LONG).show();

        //You can start an instance of RoomDetailActivity here
        Intent i = new Intent(this, RoomFreeBusyActivity.class);
        i.putExtra(RoomFreeBusyActivity.EXTRA_DEVICE_ADDRESS, device.getAddress());
        i.putExtra(RoomFreeBusyActivity.EXTRA_DEVICE_NAME, name);
        startActivity(i);
    }
}
