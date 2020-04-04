/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.utilities.genericjsonio;

import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIO;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mmattner
 */
public class JsonIONGTest {
   
    @Test
    public void testDecode1() {
        final String s = "myname at 2014-08-06 08:49:14 EST";
        final String result = JsonIO.decode(JsonIO.encode(s));
        Assert.assertEquals(result, s);
    }

    @Test
    public void testDecode2() {
        final String s = "~!@#$%^&*()_+";
        final String result = JsonIO.decode(JsonIO.encode(s));
        Assert.assertEquals(result, s);
    }

    @Test
    public void testDecode3() {
        final String s = ":;\\|<>[]{}/?";
        final String result = JsonIO.decode(JsonIO.encode(s));
        Assert.assertEquals(result, s);
    }

    @Test
    public void testDecodeBad1() {
        final String result = JsonIO.decode(SeparatorConstants.UNDERSCORE);
        Assert.assertNull(result);
    }

    @Test
    public void testDecodeBad2() {
        final String result = JsonIO.decode("_12");
        Assert.assertNull(result);
    }

    @Test
    public void testDecodeBad3() {
        final String result = JsonIO.decode("_123q");
        Assert.assertNull(result);
    } 
}
