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

import android.content.Intent;
import android.support.v4.app.Fragment;
import java.util.Calendar;

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

    public void onEmptyViewLongPress(Calendar time, String macAddr, String roomName) {
        Intent i = new Intent(this, bookEventActivity.class);

        i.putExtra(bookEventActivity.EXTRA_START_TIME, time.getTimeInMillis());
        i.putExtra(bookEventActivity.EXTRA_DEVICE_ADDRESS, macAddr);
        i.putExtra(bookEventActivity.EXTRA_ROOM_NAME, roomName);

        startActivity(i);
    }
}