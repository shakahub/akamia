/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     MapUtil
 * Purpose:     Map analysis
 * Created by:  John Hou
 * Created on:  9/3/2015
 */
package com.shaka.akamia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class MapUtil {
    ArrayList<Map.Entry> arrayList = new ArrayList<>();


    public MapUtil(Map src) {
        Iterator iter = src.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            arrayList.add(entry);
        }
    }

    public Object getValueByKey(String key) {
        for (Map.Entry entry : arrayList) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }

        return null;
    }
}
