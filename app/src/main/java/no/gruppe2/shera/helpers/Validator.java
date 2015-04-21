package no.gruppe2.shera.helpers;


import java.util.Calendar;

public class Validator {

    public boolean isEmpty(String s) {
        return s.length() <= 0;
    }

    public boolean isEmpty(int i) {
        return i <= 0;
    }

    public boolean isMaxLargerThanNum(int max, int num) {
        return num <= max;
    }

    public boolean isDateInFuture(Calendar cal) {
        Calendar c = Calendar.getInstance();
        return cal.after(c);
    }
}
