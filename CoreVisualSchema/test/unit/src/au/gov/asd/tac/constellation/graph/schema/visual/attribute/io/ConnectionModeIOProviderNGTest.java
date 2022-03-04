/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ConnectionModeAttributeDescription;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class ConnectionModeIOProviderNGTest {
    
    public ConnectionModeIOProviderNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getName method, of class ConnectionModeIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("ConnectionModeIOProviderNGTest.getName");
        
        ConnectionModeIOProvider instance = new ConnectionModeIOProvider();
        String result = instance.getName();
        assertEquals(result, ConnectionModeAttributeDescription.ATTRIBUTE_NAME);
    }
}
