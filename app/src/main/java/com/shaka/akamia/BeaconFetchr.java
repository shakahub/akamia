/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     BeaconFetchr
 * Purpose:     call rest webservice to check if the found beacon is a registered device
 * Created by:  John Hou
 * Created on:  9/2/2015
 */
package com.shaka.akamia;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import android.util.Log;
import android.net.Uri;

import org.apache.http.HttpStatus;


public class BeaconFetchr {
    public static final String TAG = "BeaconFetcher";
    private static final String ENDPOINT = "http://52.25.76.65:8080/beacons/";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            InputStream in = null;

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();

                ByteArrayOutputStream out = new ByteArrayOutputStream();

                int bytesRead = 0;
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
}