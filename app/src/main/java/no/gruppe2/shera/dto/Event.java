package no.gruppe2.shera.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by pernille.sethre on 19.02.2015.
 */
public class Event implements Parcelable {

    private String name, description, address, eventID, photoSource;
    private long userID;
    private int maxParticipants, numParticipants, category;
    private Calendar calendar;
    private boolean adult;
    private double longitude, latitude;
    private ArrayList<Long> participants = new ArrayList<>();

    //private static final int CASUAL = 1, FAMILY = 2, HOBBY = 3, SPORTS = 4, CULTURAL = 5;

    public Event(long userid, String eventName,
                 String eventDescription, String eventAddress,
                 double eventLatitude, double eventLongitude,
                 int eventMaxParticipants, int eventCategory,
                 Calendar eventDateTime, boolean eventAdult) {
        setEventID("");
        setUserID(userid);
        setName(eventName);
        setDescription(eventDescription);
        setAddress(eventAddress);
        setLatitude(eventLatitude);
        setLongitude(eventLongitude);
        setMaxParticipants(eventMaxParticipants);
        setNumParticipants(0);
        setCategory(eventCategory);
        setCalendar(eventDateTime);
        setAdult(eventAdult);
        setPhotoSource("NOTSET");
    }

    public Event(String eventid, long userid,
                 String eventName, String eventDescription,
                 String eventAddress, double eventLatitude,
                 double eventLongitude, int eventMaxParticipants,
                 int eventNumParticipants, int eventCategory,
                 Calendar eventDateTime, boolean eventAdult,
                 ArrayList eventParticipantsList, String photoSource) {
        setEventID(eventid);
        setUserID(userid);
        setName(eventName);
        setDescription(eventDescription);
        setAddress(eventAddress);
        setLatitude(eventLatitude);
        setLongitude(eventLongitude);
        setMaxParticipants(eventMaxParticipants);
        setNumParticipants(eventNumParticipants);
        setCategory(eventCategory);
        setCalendar(eventDateTime);
        setAdult(eventAdult);
        setParticipantsList(eventParticipantsList);
        setPhotoSource(photoSource);
    }

    public String getPhotoSource() {
        return photoSource;
    }

    public void setPhotoSource(String s) {
        photoSource = s;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String s) {
        eventID = s;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long l) {
        userID = l;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String s) {
        description = s;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String s) {
        address = s;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double la) {
        latitude = la;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double lo) {
        longitude = lo;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int i) {
        maxParticipants = i;
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(int i) {
        numParticipants = i;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int i) {
        category = i;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar cal) {
        calendar = cal;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean b) {
        adult = b;
    }

    public void setCalendarWithMillis(long l) {
        calendar = new GregorianCalendar();
        calendar.setTimeInMillis(l);
    }

    public void setParticipantsList(ArrayList list) {
        participants = list;
    }

    public ArrayList getParticipantsList() {
        return participants;
    }

    public void addParticipantToList(long id) {
        participants.add(id);
        numParticipants++;
    }

    public boolean removePartisipantFromList(long id) {
        if (participants.contains(id)) {
            participants.remove(id);
            numParticipants--;
            return true;
        }
        return false;
    }


    //methods required for the object to be parcelable
    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventID);
        dest.writeLong(userID);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(maxParticipants);
        dest.writeInt(numParticipants);
        dest.writeInt(category);
        dest.writeString(calendar.getTimeInMillis() + "");
        dest.writeByte((byte) (adult ? 1 : 0));
        dest.writeList(participants);
        dest.writeString(photoSource);
    }

    private void readFromParcel(Parcel in) {
        eventID = in.readString();
        userID = in.readLong();
        name = in.readString();
        description = in.readString();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        maxParticipants = in.readInt();
        numParticipants = in.readInt();
        category = in.readInt();
        String s = in.readString();
        setCalendarWithMillis(Long.parseLong(s));
        adult = in.readByte() != 0;
        in.readList(participants, null);
        photoSource = in.readString();
    }

    public Event(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}
