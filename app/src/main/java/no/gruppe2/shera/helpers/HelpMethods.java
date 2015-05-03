package no.gruppe2.shera.helpers;

import android.content.Context;
import android.widget.Toast;

import java.util.Calendar;

/*
This class contains a selection of methods used several places in the application. By extracting
them here, we don't have to write them several times in the code, and avoid redundancy.
 */
public class HelpMethods {

    public String leadingZeroesDate(Calendar calendar) {
        String date = "";
        date += String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "-"
                + (String.format("%02d", calendar.get(Calendar.MONTH) + 1)) + "-"
                + calendar.get(Calendar.YEAR);
        return date;
    }

    public String leadingZeroesTime(Calendar calendar) {
        String time = "";
        time += String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":"
                + String.format("%02d", calendar.get(Calendar.MINUTE));
        return time;
    }

    public void createToast(String s, Context context) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
