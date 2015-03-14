package no.gruppe2.shera;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by pernille.sethre on 19.02.2015.
 */
public class EventObject implements Parcelable {

    private String name, description, address, eventID;
    private long userID;
    private int maxParticipants, numParticipants, category;
    private Calendar calendar;
    private boolean adult;
    private double longitude, latitude;
    private ArrayList<Long> participants = new ArrayList<>();

    private final static int CASUAL = 1, FAMILY = 2, HOBBY = 3, SPORTS = 4, CULTURAL = 5;

    public EventObject(long u, String n, String d, String a, double la, double lo, int max, int cat, Calendar cal, boolean b) {
        eventID = "";
        userID = u;
        name = n;
        description = d;
        address = a;
        latitude = la;
        longitude = lo;
        maxParticipants = max;
        numParticipants = 0;
        category = cat;
        calendar = cal;
        adult = b;
    }

    public EventObject(String e, long u, String n, String d, String a, double lat, double lon, int max, int num, int cat, Calendar cal, boolean b, ArrayList list) {
        eventID = e;
        userID = u;
        name = n;
        description = d;
        address = a;
        latitude = lat;
        longitude = lon;
        maxParticipants = max;
        numParticipants = num;
        category = cat;
        calendar = cal;
        adult = b;
        participants = list;
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

    public String getName(){
        return name;
    }

    public void setName(String s){
        name = s;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String s){
        description = s;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String s){
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

    public int getMaxParticipants(){
        return maxParticipants;
    }

    public void setMaxParticipants(int i){
        maxParticipants = i;
    }

    public int getNumParticipants(){
        return numParticipants;
    }

    public void setNumParticipants(int i){
        numParticipants = i;
    }

    public int getCategory(){
        return category;
    }

    public void setCategory(int i){
        category = i;
    }

    public Calendar getCalendar(){
        return calendar;
    }

    public void setCalendar(Calendar cal){
        calendar = cal;
    }

    public boolean isAdult(){
        return adult;
    }

    public void setAdult(boolean b){
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
    }

    public EventObject(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<EventObject> CREATOR = new Parcelable.Creator<EventObject>() {
        public EventObject createFromParcel(Parcel in) {
            return new EventObject(in);
        }

        public EventObject[] newArray(int size) {
            return new EventObject[size];
        }
    };
}
