package no.gruppe2.shera;

import java.util.Calendar;

/**
 * Created by pernille.sethre on 19.02.2015.
 */
public class EventObject {

    private String name, description, address, userID;
    private int maxParticipants, numParticipants, category;
    private Calendar calendar;
    private boolean adult;

    private final static int CASUAL = 1, FAMILY = 2, HOBBY = 3, SPORTS = 4, CULTURAL = 5;

    public EventObject(String u, String n, String d, String a, int max, int cat, Calendar cal, boolean b){
        userID = u;
        name = n;
        description = d;
        address = a;
        maxParticipants = max;
        numParticipants = 0;
        category = cat;
        calendar = cal;
        adult = b;
    }

    public EventObject(String u, String n, String d, String a, int max, int num, int cat, Calendar cal, boolean b){
        userID = u;
        name = n;
        description = d;
        address = a;
        maxParticipants = max;
        numParticipants = num;
        category = cat;
        calendar = cal;
        adult = b;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String s){
        userID = s;
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
}
