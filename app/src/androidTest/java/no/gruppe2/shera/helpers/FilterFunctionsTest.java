package no.gruppe2.shera.helpers;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

import no.gruppe2.shera.dto.Event;

public class FilterFunctionsTest {

    FilterFunctions filterFunctions = new FilterFunctions();

    Event e = new Event();

    @Test
    public void testIsDateWithinRange() throws Exception {

        Calendar cal1 = Calendar.getInstance();

        e.setCalendar(cal1);

        cal1.set(Calendar.MONTH, cal1.get(Calendar.MONTH) + 1);

        Assert.assertTrue(filterFunctions.isDateWithinRange(e, cal1));

        Calendar cal2 = Calendar.getInstance();

        e.setCalendar(cal1);

        cal2.setTimeInMillis(cal2.getTimeInMillis() - 86400000);

        Assert.assertFalse(filterFunctions.isDateWithinRange(e, cal2));

    }

    @Test
    public void testIsEventOfSelectedCategory() throws Exception {

        e.setCategory(1);

        Assert.assertTrue(filterFunctions.isEventOfSelectedCategory(e, 1));
        Assert.assertFalse(filterFunctions.isEventOfSelectedCategory(e, 3));

        e.setCategory(3);

        Assert.assertTrue(filterFunctions.isEventOfSelectedCategory(e, 3));
        Assert.assertFalse(filterFunctions.isEventOfSelectedCategory(e, 2));

    }
}