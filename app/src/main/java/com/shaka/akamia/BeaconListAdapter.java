/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     BeaconListAdapter
 * Purpose:     List for storing all found beacon information and corresponding view
 * Created by:  John Hou
 * Created on:  4/22/2015
 */
package com.shaka.akamia;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
//import android.os.ParcelUuid;
//import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BeaconListAdapter extends BaseAdapter {
    private static String TAG = "BeaconListAdapter";

    private static final int SHORTENED_LOCAL_NAME = 0x08;
    private static final int COMPLETE_LOCAL_NAME = 0x09;

    private ArrayList<BluetoothDevice> mUnregDevices;
    private ArrayList<BluetoothDevice> mDevices;
    private ArrayList<String> mDevicesId;
    private ArrayList<String> mDevicesName;
    private ArrayList<Integer> mRSSIs;
    private ArrayList<String> mRecord;
    private LayoutInflater mInflater;


    public BeaconListAdapter(Activity par) {
        mUnregDevices = new ArrayList<>();
        mDevices = new ArrayList<>();
        mDevicesId = new ArrayList<>();
        mDevicesName = new ArrayList<>();
        mRSSIs = new ArrayList<>();
        mRecord = new ArrayList<>();
        mInflater = par.getLayoutInflater();
    }

    public void addOrUpdateDevice(BluetoothDevice device, int rssi, Map map, Boolean isNewFound) {
        if (isNewFound) {
            if (map == null || map.size() <= 0) {
                //unregistered device
                if (!mUnregDevices.contains(device))
                    mUnregDevices.add(device);
            } else {
                //Parse device information fetched from server
                MapUtil mu = new MapUtil(map);
                String sid = mu.getValueByKey("id").toString();
                String name = mu.getValueByKey("name").toString();
                String beaconSubjectType = mu.getValueByKey("beaconSubjectType").toString();
                String location = mu.getValueByKey("location").toString();


                String info = "Type: " + beaconSubjectType + "\n" +
                        "Located at: " + location;

                //Add this device into the list
                mDevices.add(device);
                mDevicesId.add(sid);
                mDevicesName.add(name);
                mRSSIs.add(rssi);
                mRecord.add(info);

                notifyDataSetChanged();
            }
        } else {
            updateDevice(device, rssi);
            notifyDataSetChanged();
        }
    }

    public void updateDevice(BluetoothDevice device, int rssi) {
        if (mDevices.contains(device)) {
            int position = mDevices.indexOf(device);
            mRSSIs.set(position, rssi);
        }
    }

    public BluetoothDevice getDevice(int index) {
        return mDevices.get(index);
    }

    /**
     * get device's name from Complete Local Name or Shortened Local Name field
     * in advertisement packet. Usually it should be done by BluetoothDevice.getName().
     * However, some phones skip that.
     */
    public static String getDeviceName(byte[] data) {
        String name = null;
        int fieldLength, fieldName;
        int packetLength = data.length;
        for (int index = 0; index < packetLength; index++) {
            fieldLength = data[index];
            if (fieldLength == 0)
                break;
            fieldName = data[++index];

            if (fieldName == COMPLETE_LOCAL_NAME || fieldName == SHORTENED_LOCAL_NAME) {
                name = decodeLocalName(data, index + 1, fieldLength - 1);
                break;
            }
            index += fieldLength - 1;
        }

        if (name == null || name.length() <= 0) name="Unknown Device";
        return name;
    }

    /**
     * Decodes the local name
     */
    public static String decodeLocalName(final byte[] data, final int start, final int length) {
        try {
            return new String(data, start, length, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            Log.e(TAG, "Unable to convert the complete local name to UTF-8", e);
            return null;
        } catch (final IndexOutOfBoundsException e) {
            Log.e(TAG, "Error when reading complete local name", e);
            return null;
        }
    }

    public void removeDevice(int index) {
        mDevices.remove(index);
        mDevicesId.remove(index);
        mDevicesName.remove(index);
        mRSSIs.remove(index);
        mRecord.remove(index);

        notifyDataSetChanged();
    }

    public void clearList() {
        if (mUnregDevices.size() !=0)
            mUnregDevices.clear();
        if (mDevices.size() !=0)
            mDevices.clear();
        if (mDevicesId.size() != 0)
            mDevicesId.clear();
        if (mDevicesName.size() != 0)
            mDevicesName.clear();
        if (mRSSIs.size() != 0)
            mRSSIs.clear();
        if (mRecord.size() != 0)
            mRecord.clear();

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return getDevice(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getRegId(int position) { return mDevicesId.get(position); }


    public int getRegStatus(BluetoothDevice device) {
        if (mUnregDevices.contains(device))
            return -1;

        if (mDevices.contains(device))
            return 1;

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get already available view or create new if necessary
        FieldReferences fields;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.fragment_beacon_item, null);
            fields = new FieldReferences();
            fields.deviceAddress = (TextView)convertView.findViewById(R.id.btAddress);
            fields.deviceName    = (TextView)convertView.findViewById(R.id.btName);
            fields.deviceRssi    = (TextView)convertView.findViewById(R.id.btRssi);
            fields.deviceRecord  = (TextView)convertView.findViewById(R.id.btRecord);
            fields.progressBar   = (ProgressBar)convertView.findViewById(R.id.progress);
            convertView.setTag(fields);
        } else {
            fields = (FieldReferences) convertView.getTag();
        }

        // set proper values into the view
        BluetoothDevice device = mDevices.get(position);
        int rssi = mRSSIs.get(position);
        String info = mRecord.get(position);
        String name = mDevicesName.get(position);
        String address = mDevicesId.get(position);

        fields.deviceName.setText(name);
        fields.deviceAddress.setText(address);
        fields.deviceRecord.setText(info);
        fields.deviceRssi.setText("RSSI: " + Integer.toString(rssi));
        if (rssi * (-1) > 100 )
            fields.progressBar.setProgress(0);
        else
            fields.progressBar.setProgress(100 + rssi + 57);

        return convertView;
    }

    /*
    public String printScanRecord (byte[] scanRecord) {
        // Simply print all raw bytes
        try {
            String decodedRecord = new String(scanRecord,"UTF-8");
            return ByteArrayToString(scanRecord);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        // Parse data bytes into individual records
        //List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);

        // Print individual records
        //if (records.size() > 0) {
        //    return TextUtils.join(",", records);
        //}
        return "";
    }

    /*
    public static String ByteArrayToString(byte[] ba)
    {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }

    public static class AdRecord {

        public AdRecord(int length, int type, byte[] data) {
            String decodedRecord = "";
            try {
                decodedRecord = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
            List<AdRecord> records = new ArrayList<AdRecord>();

            int index = 0;
            while (index < scanRecord.length) {
                int length = scanRecord[index++];
                //Done once we run out of records
                if (length == 0) break;

                int type = scanRecord[index];
                //Done if our record isn't a valid type
                if (type == 0) break;

                byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

                records.add(new AdRecord(length, type, data));
                //Advance
                index += length;
            }

            return records;
        }
    }
    */

    private class FieldReferences {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
        TextView deviceRecord;
        ProgressBar progressBar;
    }
}