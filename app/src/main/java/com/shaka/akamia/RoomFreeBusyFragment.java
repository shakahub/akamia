/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     RoomFreeBusyFragment
 * Purpose:     Show the arranged meeting schedule for the specific room
 * Created by:  John Hou
 * Created on:  9/9/2015
 */
package com.shaka.akamia;

import android.app.Activity;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shaka.weekview.DateTimeInterpreter;
import com.shaka.weekview.WeekView;
import com.shaka.weekview.WeekViewEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoomFreeBusyFragment extends Fragment implements WeekView.MonthChangeListener,
        WeekView.EventClickListener, WeekView.EventLongPressListener {

    private static final String ARG_PARAM1 = "mac_address";
    private static final String ARG_PARAM2 = "room_name";

    private String mParam1;
    private String mParam2;
    private ArrayList<String> mArrayList;
    private ArrayList<CalendarEvent> mCalendarEventList;

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;

    Callbacks callbacks;


    public interface Callbacks {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1 which is the device mac address.
     * @param param2 Parameter 2 which is the meeting room's name
     * @return A new instance of fragment RoomFreeBusyFragment.
     */
    public static RoomFreeBusyFragment newInstance(String param1, String param2) {
        RoomFreeBusyFragment fragment = new RoomFreeBusyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public RoomFreeBusyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        getActivity().setTitle(mParam2);

        setRetainInstance(true);

        new FetchSchedule().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment_room_schedule for this fragment
        View v = inflater.inflate(R.layout.fragment_room_weekview, container, false);

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) v.findViewById(R.id.weekView);

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        setupDateTimeInterpreter(false);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.weekview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        setupDateTimeInterpreter(id == R.id.action_week_view);

        switch (id) {
            case R.id.action_book_time:
                Toast.makeText(getActivity(), "This service has not provided yet.", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_day_view:
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(1);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_three_day_view:
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_THREE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(3);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_week_view:
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_WEEK_VIEW;
                    mWeekView.setNumberOfVisibleDays(7);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        // Populate the week view with some events.
        List<WeekViewEvent> events = new ArrayList<>();

        /*
        //If free-busy list is available
        if (mArrayList != null) {
            long id = 1;
            for(String s : mArrayList) {
                Map map = new ParseToMap().parse2(s);

                MapUtil mu = new MapUtil(map);

                Calendar startTime = Calendar.getInstance();
                long lstart = Long.parseLong(mu.getValueByKey("start").toString());
                startTime.setTimeInMillis(lstart);

                Calendar endTime = Calendar.getInstance();
                long lend = Long.parseLong(mu.getValueByKey("end").toString());
                endTime.setTimeInMillis(lend);

                if ((startTime.get(Calendar.YEAR) == newYear &&
                        startTime.get(Calendar.MONTH) == newMonth) ||
                        (endTime.get(Calendar.YEAR) == newYear &&
                                endTime.get(Calendar.MONTH) == newMonth)) {
                    WeekViewEvent event =
                            new WeekViewEvent(id, getEventTitle(startTime, endTime),
                                    startTime, endTime);

                    int i = events.size()%4;

                    switch (i) {
                        case 0:
                            event.setColor(getResources().getColor(R.color.event_color_01));
                            break;
                        case 1:
                            event.setColor(getResources().getColor(R.color.event_color_02));
                            break;
                        case 2:
                            event.setColor(getResources().getColor(R.color.event_color_03));
                            break;
                        case 3:
                            event.setColor(getResources().getColor(R.color.event_color_04));
                            break;
                    }

                    events.add(event);
                    id ++;
                }
            }
        }
        */

        //If calendar event list is available
        if (mCalendarEventList != null) {
            for(CalendarEvent ce : mCalendarEventList) {

                if ((ce.getStartDateTime().get(Calendar.YEAR) == newYear &&
                        ce.getStartDateTime().get(Calendar.MONTH) == newMonth) ||
                        (ce.getEndDateTime().get(Calendar.YEAR) == newYear &&
                                ce.getEndDateTime().get(Calendar.MONTH) == newMonth)) {

                    WeekViewEvent event = new WeekViewEvent(Long.parseLong(ce.getEvent_id()),
                            getEventTitle(ce), ce.getStartDateTime(), ce.getEndDateTime());

                    int i = mCalendarEventList.size()%4;

                    switch (i) {
                        case 0:
                            event.setColor(getResources().getColor(R.color.event_color_01));
                            break;
                        case 1:
                            event.setColor(getResources().getColor(R.color.event_color_02));
                            break;
                        case 2:
                            event.setColor(getResources().getColor(R.color.event_color_03));
                            break;
                        case 3:
                            event.setColor(getResources().getColor(R.color.event_color_04));
                            break;
                    }

                    events.add(event);
                }

                ArrayList<Attendee> attendees = ce.getAttendees();
                for(Attendee att : attendees) {
                    if (att.getDisplayName() != null) {
                        getActivity().setTitle(mParam2 + " - " + att.getDisplayName());
                        break;
                    }
                }
            }
        }

        return events;
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     * @param shortDate True if the date values should be short.
     */
    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat= new SimpleDateFormat("EEE",Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    private String getEventTitle(CalendarEvent event) {
        return String.format("%02d:%02d - %02d:%02d\n%s",
                event.getStartDateTime().get(Calendar.HOUR_OF_DAY),
                event.getStartDateTime().get(Calendar.MINUTE),
                event.getEndDateTime().get(Calendar.HOUR_OF_DAY),
                event.getEndDateTime().get(Calendar.MINUTE),
                event.getSummary());
    }

    private String getEventTitle(Calendar s, Calendar e) {
        return String.format("%02d:%02d - %02d:%02d",
                s.get(Calendar.HOUR_OF_DAY), s.get(Calendar.MINUTE),
                e.get(Calendar.HOUR_OF_DAY), e.get(Calendar.MINUTE));
    }

    /**
     * Call Web Service to fetch google calendar information based on this specific room
     */
    private class FetchSchedule extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void...params) {
            //return new BeaconFetchr().fetchRoomFreeBusyInfo(mParam1);
            return new BeaconFetchr().fetchRoomEvents(mParam1);
        }

        @Override
        protected void onPostExecute(String info) {
            //Map map = null;
            LinkedList list = null;

            if (info != null && info.length() > 0) {
                /* Following code lines for free-busy service */
                //map = new ParseToMap().parse(info);
                //MapUtil mu = new MapUtil();
                //mu.convertToFreeBusyList(map);
                //mArrayList = mu.getFreeBusyList();

                list = new ParseToMap().parseToList(info);
                MapUtil mu = new MapUtil();

                mCalendarEventList = mu.convertToCalendarEventList(list);
                mWeekView.notifyDatasetChanged();
            }
        }
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(getActivity(), "Clicked " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(getActivity(), "Long pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
    }

}
