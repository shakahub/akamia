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

import com.shaka.akamia.objects.CalendarEvent;

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

    public void onEmptyViewLongPress(Calendar time, String roomName, String roomEmail) {
        Intent i = new Intent(this, BookEventActivity.class);

        i.putExtra(BookEventActivity.EXTRA_START_TIME, time.getTimeInMillis());
        i.putExtra(BookEventActivity.EXTRA_ROOM_NAME, roomName);
        i.putExtra(BookEventActivity.EXTRA_ROOM_EMAIL, roomEmail);

        startActivity(i);
    }

    public void onEventEdit(CalendarEvent ce, String roomName, String account) {
        Intent i = new Intent(this, EditEventActivity.class);

        i.putExtra(EditEventActivity.EXTRA_CALENDAR_EVENT, ce);
        i.putExtra(EditEventActivity.EXTRA_ROOM_NAME, roomName);
        i.putExtra(EditEventActivity.EXTRA_USER_ACCOUNT, account);

        startActivity(i);
    }
}