package no.gruppe2.shera.helpers;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Calendar;

public class ValidatorTest extends TestCase {

    private Validator val = new Validator();

    @Test
    public void testIsEmpty() throws Exception {

        assertTrue(val.isEmpty(""));
        assertFalse(val.isEmpty("Hello World!"));
    }

    @Test
    public void testIsEmptyInt() throws Exception {
        assertTrue(val.isEmpty(0));
        assertFalse(val.isEmpty(5));
    }

    @Test
    public void testIsMaxLargerThanNum() throws Exception {
        assertTrue(val.isMaxLargerThanNum(5, 4));
        assertFalse(val.isMaxLargerThanNum(4, 5));
    }

    @Test
    public void testIsDateInFuture() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);

        assertTrue(val.isDateInFuture(cal));

        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);

        assertFalse(val.isDateInFuture(cal));
    }
}