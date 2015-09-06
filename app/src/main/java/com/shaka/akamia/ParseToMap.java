/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     ParseToMap
 * Purpose:     parse input JSON format string to a map (one level)
 * Created by:  John Hou
 * Created on:  9/3/2015
 */
package com.shaka.akamia;

import android.util.Log;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ParseToMap {
    public static final String TAG = "ParseToMap";

    public Map parse(String json) {
        Map map = null;

        JSONParser parser = new JSONParser();
        ContainerFactory containerfactory = new ContainerFactory() {
            @Override
            public Map createObjectContainer() {
                return new LinkedHashMap();
            }

            @Override
            public List creatArrayContainer() {
                return new LinkedList();
            }
        };

        try {
            map = (Map)parser.parse(json, containerfactory);

        } catch (ParseException pe) {
            Log.e(TAG, "Failed to parse returned info.", pe);
        }

        return map;
    }
}