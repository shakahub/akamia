/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     CalendarEvent
 * Purpose:     Calendar event object
 * Created by:  John Hou
 * Created on:  9/22/2015
 */
package com.shaka.akamia.objects;

import android.support.annotation.NonNull;
import android.util.ArrayMap;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CalendarEvent implements Serializable {

    public CalendarEvent() {
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Calendar getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Calendar startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Calendar getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Calendar endDateTime) {
        this.endDateTime = endDateTime;
    }

    public ArrayList<Attendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(ArrayList<Attendee> attendees) {
        this.attendees = attendees;
    }

    String event_id;
    String summary;
    String description;
    String location;
    String creator;
    Calendar startDateTime;
    Calendar endDateTime;
    ArrayList<Attendee> attendees;

    public String getTimeTitle() {
        Calendar st = this.getStartDateTime();
        Calendar et = this.getEndDateTime();

        SimpleDateFormat weekdayNameFormat= new SimpleDateFormat("EEE", Locale.getDefault());
        String weekday = weekdayNameFormat.format(st.getTime());

        return String.format("%s, %s %02d, %02d:%02d%s - %02d:%02d%s",
                weekday, st.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US),
                st.get(Calendar.DAY_OF_MONTH), st.get(Calendar.HOUR_OF_DAY), st.get(Calendar.MINUTE),
                st.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm", et.get(Calendar.HOUR_OF_DAY),
                et.get(Calendar.MINUTE), et.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm");
    }

    public String getAttendeesShortString() {
        int i = 0, j = 0;
        StringBuilder sb = new StringBuilder("");

        for(Attendee att : this.attendees) {
            if (i >= 5)
                break;

            if (att.getDisplayName() != null) {
                sb.insert(0, att.getDisplayName() + ", ");
                i++;
            } else {
                if (att.getEmail() != null) {
                    if (j == 0) {
                        sb.append(att.getEmail());
                        j++;
                    } else {
                        sb.append(", ");
                        sb.append(att.getEmail());
                    }
                    i++;
                }
            }
        }

        j = this.attendees.size() - i;

        if ( j != 0) {
            sb.append(" + ");
            sb.append(Integer.toString(j));
        }

        return sb.toString();
    }

    public HashMap<String, Object> isOrganizer(ArrayList<String> arrayList) {
        HashMap<String, Object> hm = new HashMap<>();

        for(Attendee att : this.attendees) {
            String email = att.getEmail().toLowerCase();
            if (arrayList.contains(email) && att.isOrganizer()) {
                hm.put("result", true);
                hm.put("email", email);
                break;
            }
        }

        return hm;
    }

    static public ArrayList<String> getTimeZoneList() {
        ArrayList<String> arrayList = new ArrayList<>();

        String [] ids = TimeZone.getAvailableIDs();

        for(String id : ids) {
            arrayList.add(displayTimeZone(TimeZone.getTimeZone(id)));
        }

        return arrayList;
    }

    static public String displayTimeZone(TimeZone tz) {
        long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset());

        minutes = Math.abs(minutes);

        return String.format("%s (GMT %d:%02d)", tz.getID(), hours, minutes);
    }
}
