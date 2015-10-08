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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shaka.akamia.objects.CalendarEvent;
import com.shaka.akamia.util.BeaconFetcher;
import com.shaka.akamia.util.MapUtil;
import com.shaka.akamia.util.ParseToMap;
import com.shaka.weekview.DateTimeInterpreter;
import com.shaka.weekview.WeekView;
import com.shaka.weekview.WeekViewEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class RoomFreeBusyFragment extends Fragment implements WeekView.MonthChangeListener,
        WeekView.EventClickListener, WeekView.EmptyViewLongPressListener {

    private static final String ARG_PARAM1 = "mac_address";
    private static final String ARG_PARAM2 = "room_name";

    private String mParam1;
    private String mParam2;
    private ArrayList<CalendarEvent> mCalendarEventList;
    private ArrayList<String> mUserAccounts;

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;

    Callbacks mCallbacks;

    public interface Callbacks {
        void onEmptyViewLongPress(Calendar time, String mac, String roomName);
        void onEventEdit(CalendarEvent calendarEvent, String roomName, String account);
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

        mUserAccounts = getUserAccount();

        setRetainInstance(true);

        //new FetchSchedule().execute();
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

        // Set long press lisenter for empty view
        mWeekView.setEmptyViewLongPressListener(this);

        // This is optional
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
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        new FetchSchedule().execute();
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
            int j = 0;
            for(CalendarEvent ce : mCalendarEventList) {

                if ((ce.getStartDateTime().get(Calendar.YEAR) == newYear &&
                        ce.getStartDateTime().get(Calendar.MONTH) == newMonth) ||
                        (ce.getEndDateTime().get(Calendar.YEAR) == newYear &&
                                ce.getEndDateTime().get(Calendar.MONTH) == newMonth)) {

                    WeekViewEvent event = new WeekViewEvent(Long.parseLong(ce.getEvent_id()),
                            getEventTitle(ce), ce.getStartDateTime(), ce.getEndDateTime());

                    int i = j%4;

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
                    j++;
                }

                /*
                ArrayList<Attendee> attendees = ce.getAttendees();
                for(Attendee att : attendees) {
                    if (att.getDisplayName() != null) {
                        mRoomName = att.getDisplayName();
                        getActivity().setTitle(mParam2 + " - " + mRoomName);
                        break;
                    }
                }
                */
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

    /*
    private String getEventTitle(Calendar s, Calendar e) {
        return String.format("%02d:%02d - %02d:%02d",
                s.get(Calendar.HOUR_OF_DAY), s.get(Calendar.MINUTE),
                e.get(Calendar.HOUR_OF_DAY), e.get(Calendar.MINUTE));
    }
    */

    /**
     * Call Web Service to fetch google calendar information based on this specific room
     */
    private class FetchSchedule extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void...params) {
            //return new BeaconFetcher().fetchRoomFreeBusyInfo(mParam1);
            return new BeaconFetcher().fetchRoomEvents(mParam1);
        }

        @Override
        protected void onPostExecute(String info) {
            //Map map;
            LinkedList list;

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

    private ArrayList<String> getUserAccount() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        ArrayList<String> possibleEmailAddress = new ArrayList<>();

        Account[] accounts = AccountManager.get(getActivity()).getAccounts();
        for(Account acc : accounts) {
            if (emailPattern.matcher(acc.name).matches()) {
                if (! possibleEmailAddress.contains(acc.name.toLowerCase())) {
                    possibleEmailAddress.add(acc.name.toLowerCase());
                }
            }
        }

        return possibleEmailAddress;
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {

        final CalendarEvent ce = getClickedCalendarEvent(event.getId());

        if (ce == null)
            return;

        final Dialog dialog = new Dialog(getActivity());

        View v = getActivity().getLayoutInflater().inflate(R.layout.event_dialog,
                new LinearLayout(getActivity()));

        dialog.setContentView(v);
        dialog.setTitle(ce.getSummary());

        //Display Event Title
        TextView textView1 = (TextView)dialog.findViewById(R.id.title);
        textView1.setText(ce.getTimeTitle());

        //Display where
        TextView textView2 = (TextView)dialog.findViewById(R.id.where_text);
        textView2.setText(mParam2);

        //Display who will attend
        TextView textView3 = (TextView)dialog.findViewById(R.id.who_text);

        textView3.setText(ce.getAttendeesShortString());


        //Display linked action
        final TextView textView4 = (TextView)dialog.findViewById(R.id.actionLink);

        HashMap<String, Object> hm = ce.isOrganizer(mUserAccounts);
        final MapUtil mu = new MapUtil(hm);

        if (hm.size() == 0) {
            textView4.setText(Html.fromHtml(getActivity().getString(R.string.link_close)));
        } else {
            if ((Boolean)mu.getValueByKey("result"))
                textView4.setText(Html.fromHtml(getActivity().getString(R.string.link_edit)));
        }

        textView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView4.getText().toString().equals(
                        Html.fromHtml(getActivity().getString(R.string.link_edit)).toString())) {
                    mCallbacks.onEventEdit(ce, mParam2, (String)mu.getValueByKey("email"));
                }
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    @Nullable
    private CalendarEvent getClickedCalendarEvent(long eventId) {
        for(CalendarEvent calendarEvent : mCalendarEventList) {
            if (Long.parseLong(calendarEvent.getEvent_id()) == eventId) {
                return calendarEvent;
            }
        }

        return null;
    }

    public void onEmptyViewLongPress(Calendar time) {
        mCallbacks.onEmptyViewLongPress(time, mParam1, mParam2);
    }
}
