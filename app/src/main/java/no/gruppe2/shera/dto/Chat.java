package no.gruppe2.shera.dto;

import java.util.Calendar;

public class Chat {
    private String userName, message, eventID;
    private long userID;
    private Calendar dateTime;

    public Chat(Calendar calendar, long userID, String userName, String message, String eventID) {
        setDateTime(calendar);
        setUserID(userID);
        setUserName(userName);
        setMessage(message);
        setEventID(eventID);
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String ID) {
        eventID = ID;
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
