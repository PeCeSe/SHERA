package no.gruppe2.shera.helpers;


import java.util.Calendar;

public class Validator {

    public boolean isNotEmpty(String s) {
        if (s.length() < 1)
            return false;
        else
            return true;
    }

    public boolean isNotEmpty(int i) {
        if (i < 1) {
            return false;
        } else
            return true;
    }

    public boolean isMaxLargerThanNum(int max, int num) {
        if (num <= max) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isDateInFuture(Calendar cal) {
        Calendar c = Calendar.getInstance();
        if (cal.after(c)) {
            return false;
        } else {
            return true;
        }
    }
}
