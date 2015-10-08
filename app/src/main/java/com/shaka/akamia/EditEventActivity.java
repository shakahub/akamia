/*
 * Copyright (c) 2015, Shaka LLC
 * All rights reserved.
 *
 * Program:     EditEventActivity
 * Purpose:     book an event
 * Created by:  John Hou
 * Created on:  10/8/2015
 */
package com.shaka.akamia;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.shaka.akamia.objects.CalendarEvent;
import com.shaka.akamia.util.BeaconFetcher;

import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EditEventActivity extends AppCompatActivity {
    public static final String EXTRA_CALENDAR_EVENT = "myCalendarEvent";
    public static final String EXTRA_ROOM_NAME = "myRoomName";
    public static final String EXTRA_USER_ACCOUNT = "myEmailAccount";

    final Context context = this;

    CalendarEvent mCalendarEvent;
    String mName;
    String mEmailAccount;

    EditText mEditText;
    Button mFromDateButton;
    Button mFromTimeButton;
    Button mToDateButton;
    Button mToTimeButton;
    Button mTimeZoneButton;

    Calendar fCalendar = Calendar.getInstance();
    Calendar tCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        mCalendarEvent = (CalendarEvent)getIntent().getSerializableExtra(EXTRA_CALENDAR_EVENT);
        mName = getIntent().getStringExtra(EXTRA_ROOM_NAME);
        mEmailAccount = getIntent().getStringExtra(EXTRA_USER_ACCOUNT);

        fCalendar = mCalendarEvent.getStartDateTime();

        //Event title
        mEditText = (EditText)findViewById(R.id.editText1);
        mEditText.setText(mCalendarEvent.getSummary());

        //from date string
        SimpleDateFormat weekdayNameFormat= new SimpleDateFormat("EEE", Locale.getDefault());
        String weekday = weekdayNameFormat.format(fCalendar.getTime());
        String from_date = String.format("%s, %02d/%02d/%04d",
                weekday, fCalendar.get(Calendar.DAY_OF_MONTH), fCalendar.get(Calendar.MONTH) + 1,
                fCalendar.get(Calendar.YEAR));

        //from time string
        String from_time = String.format("%02d:%02d", fCalendar.get(Calendar.HOUR_OF_DAY),
                fCalendar.get(Calendar.MINUTE));
        long hours = TimeUnit.MILLISECONDS.toHours(fCalendar.getTimeZone().getRawOffset());

        //Event from date
        mFromDateButton = (Button)findViewById(R.id.from_date);
        mFromDateButton.setText(from_date);
        mFromDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener t = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        fCalendar.set(Calendar.YEAR, year);
                        fCalendar.set(Calendar.MONTH, month);
                        fCalendar.set(Calendar.DAY_OF_MONTH, day);

                        SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE",
                                Locale.getDefault());
                        String weekday = weekdayNameFormat.format(fCalendar.getTime());
                        String from_date = String.format("%s, %02d/%02d/%04d",
                                weekday, fCalendar.get(Calendar.DAY_OF_MONTH),
                                fCalendar.get(Calendar.MONTH) + 1,
                                fCalendar.get(Calendar.YEAR));
                        mFromDateButton.setText(from_date);

                    }
                };
                new DatePickerDialog(context, t, fCalendar.get(Calendar.YEAR),
                        fCalendar.get(Calendar.MONTH), fCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //Event from time
        mFromTimeButton = (Button)findViewById(R.id.from_time);
        mFromTimeButton.setText(from_time);
        mFromTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        fCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        fCalendar.set(Calendar.MINUTE, minute);
                        String from_time = String.format("%02d:%02d",
                                fCalendar.get(Calendar.HOUR_OF_DAY), fCalendar.get(Calendar.MINUTE));
                        mFromTimeButton.setText(from_time);
                    }
                };
                new TimePickerDialog(context, t,
                        fCalendar.get(Calendar.HOUR_OF_DAY),
                        fCalendar.get(Calendar.MINUTE), true).show();
            }
        });

        //to date string
        tCalendar = mCalendarEvent.getEndDateTime();

        String to_date = String.format("%s, %02d/%02d/%04d", weekday,
                tCalendar.get(Calendar.DAY_OF_MONTH), tCalendar.get(Calendar.MONTH) + 1,
                tCalendar.get(Calendar.YEAR));

        //to time string
        String to_time = String.format("%02d:%02d", tCalendar.get(Calendar.HOUR_OF_DAY),
                tCalendar.get(Calendar.MINUTE));


        //Event to date
        mToDateButton = (Button)findViewById(R.id.to_date);
        mToDateButton.setText(to_date);
        mToDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener t = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        tCalendar.set(Calendar.YEAR, year);
                        tCalendar.set(Calendar.MONTH, month);
                        tCalendar.set(Calendar.DAY_OF_MONTH, day);

                        SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE",
                                Locale.getDefault());
                        String weekday = weekdayNameFormat.format(tCalendar.getTime());
                        String to_date = String.format("%s, %02d/%02d/%04d", weekday,
                                tCalendar.get(Calendar.DAY_OF_MONTH),
                                tCalendar.get(Calendar.MONTH) + 1,
                                tCalendar.get(Calendar.YEAR));
                        mToDateButton.setText(to_date);

                    }
                };
                new DatePickerDialog(context, t, tCalendar.get(Calendar.YEAR),
                        tCalendar.get(Calendar.MONTH), tCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //Event to time
        mToTimeButton = (Button)findViewById(R.id.to_time);
        mToTimeButton.setText(to_time);
        mToTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        tCalendar.set(Calendar.MINUTE, minute);
                        String to_time = String.format("%02d:%02d",
                                tCalendar.get(Calendar.HOUR_OF_DAY),
                                tCalendar.get(Calendar.MINUTE));
                        mToTimeButton.setText(to_time);
                    }
                };

                new TimePickerDialog(context, t, tCalendar.get(Calendar.HOUR_OF_DAY),
                        tCalendar.get(Calendar.MINUTE), true).show();
            }
        });

        //Timezone
        String timeZone = String.format("%s (GMT %d:%02d)", fCalendar.getTimeZone().getDisplayName(),
                hours, TimeUnit.MILLISECONDS.toMinutes(fCalendar.getTimeZone().getRawOffset())
                        -TimeUnit.HOURS.toMinutes(hours));

        mTimeZoneButton = (Button)findViewById(R.id.timezone);
        mTimeZoneButton.setText(timeZone);
        mTimeZoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);

                View view = getLayoutInflater().inflate(R.layout.timezone_list_dialog,
                        new LinearLayout(context));
                dialog.setContentView(view);

                dialog.setTitle(context.getString(R.string.timezone_dialog_title));

                ListView lv = (ListView)dialog.findViewById(R.id.timezoneList);

                final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_list_item_1, CalendarEvent.getTimeZoneList());
                lv.setAdapter(adapter);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedTimeZone = (String)parent.getAdapter().getItem(position);
                        mTimeZoneButton.setText(selectedTimeZone);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        //Location
        TextView textView = (TextView)findViewById(R.id.location);
        textView.setText(mName);

        //Account
        TextView textView1 = (TextView)findViewById(R.id.account);
        textView1.setText(mEmailAccount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                saveBookingEvent();
                finish();
                break;
            case R.id.action_delete:
                delEvent(mCalendarEvent.getEvent_id());
                finish();
                break;
            case R.id.action_cancel:
                finish();  //close current activity and go back the previous one
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unchecked")
    private void saveBookingEvent() {
        JSONObject jsonEvent = new JSONObject();
        JSONObject jsonData = new JSONObject();

        jsonData.put("title", mEditText.getText());
        jsonData.put("from_date", mFromDateButton.getText());
        jsonData.put("from_time", mFromTimeButton.getText());
        jsonData.put("to_date", mToDateButton.getText());
        jsonData.put("to_time", mToTimeButton.getText());
        jsonData.put("timezone", mTimeZoneButton.getText());

        jsonEvent.put("room_name", mName);
        jsonEvent.put("account", mEmailAccount);
        jsonEvent.put("event", jsonData);

        String result = BeaconFetcher.postBookEvent(jsonEvent);

        if (result.equals("success")) {
            Toast.makeText(context, "The event has successfully saved", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unchecked")
    private void delEvent(String eventId) {
        JSONObject jsonEvent = new JSONObject();

        jsonEvent.put("event_id", eventId);

        String result = BeaconFetcher.postDeleteEvent(jsonEvent);

        if (result.equals("success")) {
            Toast.makeText(context, "The event has been deleted", Toast.LENGTH_LONG).show();
        }
    }
}