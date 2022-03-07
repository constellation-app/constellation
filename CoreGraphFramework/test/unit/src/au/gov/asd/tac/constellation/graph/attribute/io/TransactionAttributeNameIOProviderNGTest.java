/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.attribute.io;

import au.gov.asd.tac.constellation.graph.attribute.TransactionAttributeNameAttributeDescription;
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
public class TransactionAttributeNameIOProviderNGTest {
    
    public TransactionAttributeNameIOProviderNGTest() {
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
     * Test of getName method, of class TransactionAttributeNameIOProvider.
     */
    @Test
    public void testGetName() {
        System.out.println("TransactionAttributeNameIOProviderNGTest.testGetName");
        TransactionAttributeNameIOProvider instance = new TransactionAttributeNameIOProvider();
        assertEquals(instance.getName(), TransactionAttributeNameAttributeDescription.ATTRIBUTE_NAME);
    }
    
}
