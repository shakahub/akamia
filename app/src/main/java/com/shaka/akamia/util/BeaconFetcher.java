/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     BeaconFetcher
 * Purpose:     call rest webservice to check if the found beacon is a registered device
 * Created by:  John Hou
 * Created on:  9/2/2015
 */
package com.shaka.akamia.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import android.util.Log;
import android.net.Uri;

import org.json.simple.JSONObject;

public class BeaconFetcher {
    public static final String TAG = "BeaconFetcher";
    private static final String REMOTE_ADDRESS = "http://52.26.217.219:8080/";
    private static final String ENDPOINT = REMOTE_ADDRESS + "beacons/";
    private static final String EVENT_POST_SERVICE = REMOTE_ADDRESS + "createEvent/";
    private static final String EVENT_DELETE_SERVICE = REMOTE_ADDRESS + "deleteEvent/";
    //private static final String CALENDAREVENTS_FREE_BUSY = "calendar-events/free-busy";
    private static final String CALENDAR_EVENTS = "calendar-events";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            InputStream in;

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();

                ByteArrayOutputStream out = new ByteArrayOutputStream();

                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }

                in.close();
                out.close();

                return out.toByteArray();
            }

            return null;
        } finally {
            connection.disconnect();
        }
    }

    String getUrl(String urlSpec) throws IOException {
        byte[] buffer = getUrlBytes(urlSpec);

        if (buffer == null)
            return null;

        return new String(buffer);
    }

    public Map fetchBeacon(String address) {
        Map map = null;

        try {
            String url = Uri.parse(ENDPOINT).buildUpon().appendEncodedPath(address).build().toString();
            String data = getUrl(url);

            if (data != null)
                map = new ParseToMap().parse(data);

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch beacon.", ioe);
        }

        return map;
    }

    /*
    public String fetchRoomFreeBusyInfo(String address) {
        String data = null;

        try {
            String url = Uri.parse(ENDPOINT).buildUpon().appendEncodedPath(address).
                    appendEncodedPath(CALENDAREVENTS_FREE_BUSY).build().toString();
            data = getUrl(url);

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch meeting room free or busy information.", ioe);
        }

        return data;
    }
    */

    public String fetchRoomEvents(String address) {
        String data = null;

        try {
            String url = Uri.parse(ENDPOINT).buildUpon().appendEncodedPath(address).
                    appendEncodedPath(CALENDAR_EVENTS).build().toString();
            data = getUrl(url);

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch meeting room events.", ioe);
        }

        return data;
    }

    public static String postBookEvent(JSONObject jsonObject) {
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(EVENT_POST_SERVICE);
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(jsonObject.toString());
            out.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result;
            while ((result = br.readLine()) != null) {
                sb.append(result);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static String postDeleteEvent(JSONObject jsonObject) {
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(EVENT_DELETE_SERVICE);
            URLConnection conn = url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(jsonObject.toString());
            out.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result;
            while ((result = br.readLine()) != null) {
                sb.append(result);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

}