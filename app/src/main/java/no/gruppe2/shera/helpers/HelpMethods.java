package no.gruppe2.shera.helpers;

import android.content.Context;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by chris.forberg on 17.03.2015.
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
