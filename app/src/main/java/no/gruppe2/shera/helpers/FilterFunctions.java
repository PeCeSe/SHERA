package no.gruppe2.shera.helpers;

import android.location.Location;

import com.google.android.gms.maps.model.Circle;

import java.util.Calendar;

import no.gruppe2.shera.dto.Event;

public class FilterFunctions {

    public boolean isDateWithinRange(Event event, Calendar range) {
        return !(event.getCalendar().after(range));
    }

    public boolean isEventOfSelectedCategory(Event event, int category) {
        return event.getCategory() == category;
    }

    public boolean isEventInsideCircle(Event event, Circle circle) {
        float[] distance = new float[2];

        if (circle != null) {
            Location.distanceBetween(event.getLatitude(), event.getLongitude(),
                    circle.getCenter().latitude, circle.getCenter().longitude, distance);

            return distance[0] < circle.getRadius();
        }
        return true;
    }
}
