/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author limowreck11
 */
public class AnalyticConceptNGTest {
    
    private static final String TELEPHONE_IDENTIFIER_VALID = "+61433123456";
    private static final String TELEPHONE_IDENTIFIER_INVALID = "++61433123456";
 
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
     * Test Detection Regex for {@link AnalyticConcept.VertexType#TELEPHONE_IDENTIFIER}.
     */
    @Test
    public void testTelephoneIdentifierDetectionRegex() {
        System.out.println("testTelephoneIdentifierDetectionRegex");
        final Pattern pattern = AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER.getDetectionRegex();
        final Matcher validMatcher = pattern.matcher(TELEPHONE_IDENTIFIER_VALID);
        final Matcher invalidMatcher = pattern.matcher(TELEPHONE_IDENTIFIER_INVALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
    }

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#TELEPHONE_IDENTIFIER}.
     */
    @Test
    public void testTelephoneIdentifierValidationRegex() {
        System.out.println("testTelephoneIdentifierValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(TELEPHONE_IDENTIFIER_VALID);
        final Matcher invalidMatcher = pattern.matcher(TELEPHONE_IDENTIFIER_INVALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
    }
}
