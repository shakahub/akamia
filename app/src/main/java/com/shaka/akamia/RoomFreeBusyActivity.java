/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     RoomFreeBusyActivity
 * Purpose:     This activity will create RoomFreeBusyFragment
 * Created by:  John Hou
 * Created on:  9/9/2015
 */
package com.shaka.akamia;

import android.support.v4.app.Fragment;

public class RoomFreeBusyActivity extends SingleFragmentActivity
        implements RoomFreeBusyFragment.Callbacks {
    public static final String EXTRA_DEVICE_ADDRESS = "meetingroom.beacon_address";
    public static final String EXTRA_DEVICE_NAME = "meetingroom.name";

    @Override
    protected Fragment createFragment() {
        String address = getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS);
        String name = getIntent().getStringExtra(EXTRA_DEVICE_NAME);
        return RoomFreeBusyFragment.newInstance(address, name);
    }
}