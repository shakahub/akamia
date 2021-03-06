/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     MapUtil
 * Purpose:     Map analysis
 * Created by:  John Hou
 * Created on:  9/3/2015
 */
package com.shaka.akamia.util;

import com.shaka.akamia.objects.Attendee;
import com.shaka.akamia.objects.CalendarEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;

public class MapUtil {
    ArrayList<Map.Entry> arrayList = new ArrayList<>();
    ArrayList<String> list = new ArrayList<>();
    ArrayList<CalendarEvent> eventList = new ArrayList<>();

    long lstart = 0;
    long lend = 0;

    public MapUtil() {
        init();
    }

    public MapUtil(Map src) {
        init();

        for(Object o : src.entrySet()) {
            Map.Entry entry = (Map.Entry)o;
            arrayList.add(entry);
        }

        /*
        //same as above but just use Iterator. Compiler will give a warning message for this
        //since while can be replaced by foreach.
        Iterator iter = src.entrySet().iterator();

        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            arrayList.add(entry);
        }
        */
    }

    private void init() {
        if (list != null && list.size() > 0)
            list.clear();

        if (eventList != null && eventList.size() > 0)
            eventList.clear();
    }

    public ArrayList<String> convertToFreeBusyList(Map src) {

        for(Object obj : src.entrySet()) {
            Map.Entry entry = (Map.Entry)obj;
            String key = entry.getKey().toString();
            if (key.equals("busy")) {
                LinkedList ll = (LinkedList) entry.getValue();
                int times = ll.size();
                for (int i = 0; i < times; i++) {
                    Map map = (Map) ll.get(i);
                    for (Object o : map.entrySet()) {
                        entry = (Map.Entry)o;
                        key = entry.getKey().toString();

                        if (key.equals("end")) {
                            LinkedHashMap lh = (LinkedHashMap) entry.getValue();

                            for(Object o2 : lh.entrySet()) {
                                Map.Entry entry2 = (Map.Entry)o2;
                                if (entry2.getKey().equals("value")) {
                                    lend = (long) entry2.getValue();
                                }
                                /*
                                if (entry2.getKey().equals("dateOnly")) {
                                    //do nothing
                                } */
                                if (entry2.getKey().equals("timeZoneShift")) {
                                    lend = lend + (long) entry2.getValue() * 3600000;
                                }
                            }
                        }
                        if (key.equals("start")) {
                            LinkedHashMap lh = (LinkedHashMap) entry.getValue();

                            for (Object o2 : lh.entrySet()) {
                                Map.Entry entry2 = (Map.Entry)o2;
                                if (entry2.getKey().equals("value")) {
                                    lstart = (long) entry2.getValue();
                                }
                                /*
                                if (entry2.getKey().equals("dateOnly")) {
                                    //do nothing so far
                                } */
                                if (entry2.getKey().equals("timeZoneShift")) {
                                    lstart = lstart + (long) entry2.getValue() * 3600000;
                                }
                            }
                        }

                        if (lend > 0 && lstart > 0) {
                            String item = "start=" + lstart + ", end=" + lend;
                            list.add(item);

                            lend = 0;
                            lstart = 0;
                        }
                    }
                }
            }
        }

        return list;
    }

    public ArrayList<CalendarEvent> convertToCalendarEventList(LinkedList src) {

        for (int i = 0; i < src.size(); i++) {
            CalendarEvent calendarEvent = new CalendarEvent();

            calendarEvent.setEvent_id(Integer.toString(i));

            Map map = (Map) src.get(i);

            for (Object obj : map.entrySet()) {
                Map.Entry entry = (Map.Entry) obj;
                if (entry.getKey().toString().equals("summary")) {
                    calendarEvent.setSummary(entry.getValue() == null ? null : entry.getValue().toString());
                    continue;
                }

                if (entry.getKey().toString().equals("description")) {
                    calendarEvent.setDescription(entry.getValue() == null ? null : entry.getValue().toString());
                    continue;
                }

                if (entry.getKey().toString().equals("location")) {
                    calendarEvent.setLocation(entry.getValue() == null ? null : entry.getValue().toString());
                    continue;
                }

                if (entry.getKey().toString().equals("creator")) {
                    LinkedHashMap lh = (LinkedHashMap) entry.getValue();
                    for (Object o : lh.entrySet()) {
                        Map.Entry entry2 = (Map.Entry)o;
                        if (entry2.getKey().equals("email")) {
                            calendarEvent.setCreator(entry.getValue() == null ? null : entry.getValue().toString());
                            break;
                        }
                    }
                    continue;
                }

                if (entry.getKey().toString().equals("startDateTime")) {
                    //Save local timezone
                    TimeZone tz = TimeZone.getDefault();
                    //Set current timezone to UTC because the time from server is in UTC timezone
                    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                    Calendar startDate = Calendar.getInstance();
                    LinkedList list = (LinkedList)entry.getValue();

                    for(int j=0; j < list.size(); j++) {
                        if (j == 0) {
                            startDate.set(Calendar.YEAR, Integer.parseInt(list.get(j).toString()));
                            continue;
                        }
                        if (j == 1) {
                            startDate.set(Calendar.MONTH, Integer.parseInt(list.get(j).toString()) - 1);
                            continue;
                        }
                        if (j == 2) {
                            startDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(list.get(j).toString()));
                            continue;
                        }
                        if (j == 3) {
                            startDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(list.get(j).toString()));
                            continue;
                        }
                        if (j == 4) {
                            startDate.set(Calendar.MINUTE, Integer.parseInt(list.get(j).toString()));
                            continue;
                        }
                        if (j == 5)
                            startDate.set(Calendar.SECOND, Integer.parseInt(list.get(j).toString()));
                    }

                    //get local timezone back
                    TimeZone.setDefault(tz);
                    //convert UTC time to local time
                    Calendar newStartDate = Calendar.getInstance();
                    newStartDate.setTimeInMillis(startDate.getTimeInMillis());

                    //set local time into the event
                    calendarEvent.setStartDateTime(newStartDate);
                    continue;
                }

                if (entry.getKey().toString().equals("endDateTime")) {
                    //Save local timezone
                    TimeZone tz = TimeZone.getDefault();
                    //Set current timezone to UTC because the time from server is in UTC timezone
                    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                    Calendar endDate = Calendar.getInstance();
                    LinkedList list = (LinkedList)entry.getValue();

                    for(int j=0; j < list.size(); j++) {
                        if (j == 0) {
                            endDate.set(Calendar.YEAR, Integer.parseInt(list.get(j).toString()));
                            continue;
                        }
                        if (j == 1) {
                            endDate.set(Calendar.MONTH, Integer.parseInt(list.get(j).toString()) - 1);
                            continue;
                        }
                        if (j == 2) {
                            endDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(list.get(j).toString()));
                            continue;
                        }
                        if (j == 3) {
                            endDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(list.get(j).toString()));
                            continue;
                        }
                        if (j == 4) {
                            endDate.set(Calendar.MINUTE, Integer.parseInt(list.get(j).toString()));
                            continue;
                        }
                        if (j == 5)
                            endDate.set(Calendar.SECOND, Integer.parseInt(list.get(j).toString()));
                    }

                    //get local timezone back
                    TimeZone.setDefault(tz);
                    //convert UTC time to local time
                    Calendar newEndDate = Calendar.getInstance();
                    newEndDate.setTimeInMillis(endDate.getTimeInMillis());

                    //set local time into the event
                    calendarEvent.setEndDateTime(newEndDate);

                    continue;
                }

                if (entry.getKey().toString().equals("eventAttendees")) {
                    ArrayList<Attendee> attendees = new ArrayList<>();

                    LinkedList list = (LinkedList)entry.getValue();

                    for(int j=0; j < list.size(); j++) {
                        Attendee attendee = new Attendee();
                        Map map2 = (Map)list.get(j);

                        for (Object o : map2.entrySet()) {
                            Map.Entry entry3 = (Map.Entry)o;
                            if (entry3.getKey().equals("displayName")) {
                                attendee.setDisplayName(entry3.getValue() == null ? null : entry3.getValue().toString());
                                continue;
                            }

                            if (entry3.getKey().equals("email")) {
                                attendee.setEmail(entry3.getValue() == null ? null : entry3.getValue().toString());
                                continue;
                            }

                            if (entry3.getKey().equals("resource")) {
                                attendee.setResource(entry3.getValue() != null && (boolean) entry3.getValue());
                                continue;
                            }

                            if (entry3.getKey().equals("responseStatus")) {
                                attendee.setResponseStatus(entry3.getValue() == null ? null : entry3.getValue().toString());
                                continue;
                            }

                            if (entry3.getKey().equals("self")) {
                                attendee.setSelf(entry3.getValue() != null && (boolean) entry3.getValue());
                                continue;
                            }

                            if (entry3.getKey().equals("organizer"))
                                attendee.setOrganizer(entry3.getValue() != null && (boolean) entry3.getValue());
                        }

                        attendees.add(attendee);
                    }

                    calendarEvent.setAttendees(attendees);
                }
            }

            eventList.add(calendarEvent);
        }

        return eventList;
    }

    public ArrayList<String> getFreeBusyList() {
        return list;
    }

    public ArrayList<CalendarEvent> getCalendarEventList() {
        return eventList;
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
