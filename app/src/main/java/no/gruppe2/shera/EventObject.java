package no.gruppe2.shera;

import java.util.Calendar;

/**
 * Created by pernille.sethre on 19.02.2015.
 */
public class EventObject {

    private String name, description, address;
    private int maxParticipants, numParticipants, category;
    private Calendar calendar;
    private boolean adult;

    private final static int CASUAL = 10, FAMILY = 20, HOBBY = 30, SPORTS = 40, CULTURAL = 50;

    public EventObject(String n, String d, String a, int max, int cat, Calendar cal, boolean b){
        name = n;
        description = d;
        address = a;
        maxParticipants = max;
        numParticipants = 0;
        category = cat;
        calendar = cal;
        adult = b;
    }

    public EventObject(String n, String d, String a, int max, int num, int cat, Calendar cal, boolean b){
        name = n;
        description = d;
        address = a;
        maxParticipants = max;
        numParticipants = num;
        category = cat;
        calendar = cal;
        adult = b;
    }

    public String getCategoryString(){
        switch (category){
            case 10: return "Casual";
            case 20: return "Family";
            case 30: return "Hobby";
            case 40: return "Sports";
            case 50: return "Cultural";
        }
        return null;
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
