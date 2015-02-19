package no.gruppe2.shera;

/**
 * Created by pernille.sethre on 19.02.2015.
 */
public class EventObject {

    private String name, description, address;
    private int maxParticipants, numParticipants, category;

    private final static int CASUAL = 10, FAMILY = 20, HOBBY = 30, SPORTS = 40, CULTURAL = 50;

    public EventObject(String n, String d, String a, int max, int cat){
        name = n;
        description = d;
        address = a;
        maxParticipants = max;
        numParticipants = 0;
        category = cat;
    }

    public EventObject(String n, String d, String a, int max, int num, int cat){
        name = n;
        description = d;
        address = a;
        maxParticipants = max;
        numParticipants = num;
        category = cat;
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
}
