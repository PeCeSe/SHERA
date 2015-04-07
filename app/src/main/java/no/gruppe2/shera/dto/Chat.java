package no.gruppe2.shera.dto;

import java.util.Calendar;

/**
 * Created by chris.forberg on 07.04.2015.
 */
public class Chat {
    private String userName, message;
    private long userID;
    private Calendar dateTime;

    public Chat(Calendar calendar, long userID, String name, String message) {
        setDateTime(calendar);
        setUserID(userID);
        setUserName(name);
        setMessage(message);
    }

    public Calendar getDateTime() {
        return dateTime;
    }

    public void setDateTime(Calendar calendar) {
        dateTime = calendar;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long ID) {
        userID = ID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String n) {
        userName = n;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String m) {
        message = m;
    }
}
