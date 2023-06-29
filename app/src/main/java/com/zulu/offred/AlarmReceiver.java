package com.zulu.offred;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // If the device is rebooted, you might want to reschedule the alarm here.
        } else {
            // Run your method here (e.g., runme())
            System.out.println("download all?");
            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

            // Check if the current time is between 6-7 AM
            if (hourOfDay >= 6 && hourOfDay < 7) {
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                dbHelper.clearDB();
                ArrayList<String> subs = new ArrayList<>();
                List<MainActivity.coms> cc = dbHelper.fetchComments("base");
                for (int i = 0; i < cc.size(); i++) {
                    String s = cc.get(i).comment;
                    subs.add(s);
                    dbHelper.startFetch(s);
                    MainActivity.fetchSub all = new MainActivity.fetchSub();
                    all.urls = "https://old.reddit.com/r/" + s + "/";
                    all.sub = s;
                    all.dbh = dbHelper;
                    all.c = context;
                    all.execute();
                }
            }
        }
    }


}