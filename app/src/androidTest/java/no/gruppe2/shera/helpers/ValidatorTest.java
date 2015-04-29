package no.gruppe2.shera.helpers;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

public class ValidatorTest {

    private Validator val = new Validator();
    private int DEFAULTLONGLAT = 200;

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertTrue(val.isEmpty(""));
        Assert.assertFalse(val.isEmpty("Hello World!"));
    }

    @Test
    public void testIsEmptyInt() throws Exception {
        Assert.assertTrue(val.isEmpty(0));
        Assert.assertFalse(val.isEmpty(5));
    }

    @Test
    public void testIsMaxLargerThanNum() throws Exception {
        Assert.assertTrue(val.isMaxLargerThanNum(5, 4));
        Assert.assertFalse(val.isMaxLargerThanNum(4, 5));
    }

    @Test
    public void testIsDateInFuture() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);

        Assert.assertTrue(val.isDateInFuture(cal));

        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);

        Assert.assertFalse(val.isDateInFuture(cal));
    }

    @Test
    public void testIsLatDefault() throws Exception {
        Assert.assertTrue(val.isLatDefault(DEFAULTLONGLAT));
        Assert.assertFalse(val.isLatDefault(123));
        Assert.assertFalse(val.isLatDefault(1.12345));
        Assert.assertFalse(val.isLatDefault(199.999999));
    }

    @Test
    public void testIsLongDefault() throws Exception {
        Assert.assertTrue(val.isLatDefault(DEFAULTLONGLAT));
        Assert.assertFalse(val.isLatDefault(123));
        Assert.assertFalse(val.isLatDefault(1.12345));
        Assert.assertFalse(val.isLatDefault(199.999999));
    }

    @Test
    public void testisUserIDGreaterThanZero() throws Exception {
        Assert.assertFalse(val.isUserIDGreaterThanZero(0));
        Assert.assertTrue(val.isUserIDGreaterThanZero(123));
        Assert.assertFalse(val.isUserIDGreaterThanZero(-0));
        long test = Long.parseLong("12345678987654321");
        Assert.assertTrue(val.isUserIDGreaterThanZero(test));
    }
}