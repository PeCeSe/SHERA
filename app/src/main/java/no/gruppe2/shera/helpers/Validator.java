package no.gruppe2.shera.helpers;


import java.util.Calendar;

public class Validator {
    private final int DEFAULTLATLONG = 200;

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

    public boolean isLatDefault(double lat) {
        return lat == DEFAULTLATLONG;
    }

    public boolean isLongDefault(double lng) {
        return lng == DEFAULTLATLONG;
    }

    public boolean isUserIDGreaterThanZero(long userID) {
        return userID > 0;
    }
}
