/*
 * Copyright 2010-2024 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AnalyticConcept}.
 *
 * @author cygnus_x-1
 */
public class AnalyticConceptNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    /**
     * Test whether all the attributes of AnalyticConcept are registered in the Analytic Schema
     */
    @Test
    public void testAnalyticAttributesRegistered() {
        System.out.println("analyticAttributesRegistered");
        
        final SchemaFactory schemaFactory = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
        
        final List<SchemaAttribute> registeredAttributes = new ArrayList<>();
        for (final Map<String, SchemaAttribute> graphElementAttributes : schemaFactory.getRegisteredAttributes().values()) {
            registeredAttributes.addAll(graphElementAttributes.values());
        }

        final AnalyticConcept instance = new AnalyticConcept();
        final Collection<SchemaAttribute> analyticAttributes = instance.getSchemaAttributes();
        
        for (final SchemaAttribute analyticAttribute : analyticAttributes) {
            assertTrue(registeredAttributes.contains(analyticAttribute));
        }
    }
    
    /**
     * Test to check whether all attributes in AnalyticConcept have been added to 
     * the collection of schema attributes for the concept
     */
    @Test
    public void testAttributesCorrectlyAdded() {
        System.out.println("attributesCorrectlyAdded");
        
        final AnalyticConcept instance = new AnalyticConcept();
        final Collection<SchemaAttribute> analyticAttributes = instance.getSchemaAttributes();
        
        final List<SchemaAttribute> nodeAttributes = ConceptTestUtilities.getElementTypeSpecificAttributes(analyticAttributes, GraphElementType.VERTEX);
        final List<SchemaAttribute> transactionAttributes = ConceptTestUtilities.getElementTypeSpecificAttributes(analyticAttributes, GraphElementType.TRANSACTION);
        
        final int nodeAttributeCount = ConceptTestUtilities.getFieldCount(AnalyticConcept.VertexAttribute.class, SchemaAttribute.class);
        final int transactionAttributeCount = ConceptTestUtilities.getFieldCount(AnalyticConcept.TransactionAttribute.class, SchemaAttribute.class);
        
        // ensure that all created attributes have been added to the schema attributes collection
        assertEquals(nodeAttributes.size(), nodeAttributeCount);
        assertEquals(transactionAttributes.size(), transactionAttributeCount);
        // this check will catch out any new attribute classes added to the concept
        assertEquals(analyticAttributes.size(), nodeAttributeCount + transactionAttributeCount);
    }
    
    /**
     * Test whether all the types of AnalyticConcept are registered in the Analytic Schema
     */
    @Test
    public void testAnalyticTypesRegistered() {
        System.out.println("analyticTypesRegistered");
        
        final SchemaFactory schemaFactory = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
        
        final List<SchemaVertexType> registeredVertexTypes = schemaFactory.getRegisteredVertexTypes();
        final List<SchemaTransactionType> registeredTransactionTypes = schemaFactory.getRegisteredTransactionTypes();

        final AnalyticConcept instance = new AnalyticConcept();
        
        final List<SchemaVertexType> analyticVertexTypes = instance.getSchemaVertexTypes();
        for (final SchemaVertexType analyticVertexType : analyticVertexTypes) {
            assertTrue(registeredVertexTypes.contains(analyticVertexType));
        }
        
        final List<SchemaTransactionType> analyticTransactionTypes = instance.getSchemaTransactionTypes();
        for (final SchemaTransactionType analyticTransactionType : analyticTransactionTypes) {
            assertTrue(registeredTransactionTypes.contains(analyticTransactionType));
        }
    }
    
    /**
     * Test to check whether all types in AnalyticConcept have been added to 
     * the corresponding list of schema types for the concept
     */
    @Test
    public void testTypesCorrectlyAdded() {
        System.out.println("typesCorrectlyAdded");
        
        final AnalyticConcept instance = new AnalyticConcept();
        final List<SchemaVertexType> vertexTypes = instance.getSchemaVertexTypes();
        final List<SchemaTransactionType> transactionTypes = instance.getSchemaTransactionTypes();
        
        final int nodeAttributeCount = ConceptTestUtilities.getFieldCount(AnalyticConcept.VertexType.class, SchemaVertexType.class);
        final int transactionAttributeCount = ConceptTestUtilities.getFieldCount(AnalyticConcept.TransactionType.class, SchemaTransactionType.class);
        
        // ensure that all created types have been added to the corresponding schema types list
        assertEquals(vertexTypes.size(), nodeAttributeCount);
        assertEquals(transactionTypes.size(), transactionAttributeCount);
    }

    private static final String TELEPHONE_IDENTIFIER_VALID = "+61433123456";
    private static final String TELEPHONE_IDENTIFIER_INVALID = "++3123456";
    private static final String TELEPHONE_IDENTIFIER_TEXT_VALID = "my phone number is +61433123456, give me a call.";
    private static final String TELEPHONE_IDENTIFIER_TEXT_INVALID = "my phone number is ++3123456, give me a call.";

    /**
     * Test Detection Regex for
     * {@link AnalyticConcept.VertexType#TELEPHONE_IDENTIFIER}.
     */
    @Test
    public void testTelephoneIdentifierDetectionRegex() {
        System.out.println("testTelephoneIdentifierDetectionRegex");
        final Pattern pattern = AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER.getDetectionRegex();
        final Matcher validMatcher = pattern.matcher(TELEPHONE_IDENTIFIER_TEXT_VALID);
        final Matcher invalidMatcher = pattern.matcher(TELEPHONE_IDENTIFIER_TEXT_INVALID);
        assertTrue(validMatcher.find());
        assertEquals(validMatcher.group(), "+61433123456");
        assertFalse(invalidMatcher.find());
    }

    /**
     * Test Validation Regex for
     * {@link AnalyticConcept.VertexType#TELEPHONE_IDENTIFIER}.
     */
    @Test
    public void testTelephoneIdentifierValidationRegex() {
        System.out.println("testTelephoneIdentifierValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(TELEPHONE_IDENTIFIER_VALID);
        final Matcher invalidMatcher = pattern.matcher(TELEPHONE_IDENTIFIER_INVALID);
        final Matcher textMatcher = pattern.matcher(TELEPHONE_IDENTIFIER_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String IPV4_VALID = "172.217.25.142";
    private static final String IPV4_INVALID = "172.217.025.142";
    private static final String IPV4_TEXT_VALID = "PING google.com (172.217.25.142) 56(84) bytes of data.";
    private static final String IPV4_TEXT_INVALID = "PING google.com (172.217.025.142) 56(84) bytes of data.";

    /**
     * Test Detection Regex for {@link AnalyticConcept.VertexType#IPV4}.
     */
    @Test
    public void testIpv4DetectionRegex() {
        System.out.println("testIpv4DetectionRegex");
        final Pattern pattern = AnalyticConcept.VertexType.IPV4.getDetectionRegex();
        final Matcher validMatcher = pattern.matcher(IPV4_TEXT_VALID);
        final Matcher invalidMatcher = pattern.matcher(IPV4_TEXT_INVALID);
        assertTrue(validMatcher.find());
        assertEquals(validMatcher.group(), "172.217.25.142");
        assertFalse(invalidMatcher.find());
    }

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#IPV4}.
     */
    @Test
    public void testIpv4ValidationRegex() {
        System.out.println("testIpv4ValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.IPV4.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(IPV4_VALID);
        final Matcher invalidMatcher = pattern.matcher(IPV4_INVALID);
        final Matcher textMatcher = pattern.matcher(IPV4_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String IPV6_VALID = "2001:4860:b002::68";
    private static final String IPV6_INVALID = "b002:g68";
    private static final String IPV6_TEXT_VALID = "PING ipv6.google.com(2001:4860:b002::68) 56 data bytes.";
    private static final String IPV6_TEXT_INVALID = "PING ipv6.google.com(b002:g68) 56 data bytes.";

    /**
     * Test Detection Regex for {@link AnalyticConcept.VertexType#IPV6}.
     */
    @Test
    public void testIpv6DetectionRegex() {
        System.out.println("testIpv6DetectionRegex");
        final Pattern pattern = AnalyticConcept.VertexType.IPV6.getDetectionRegex();
        final Matcher validMatcher = pattern.matcher(IPV6_TEXT_VALID);
        final Matcher invalidMatcher = pattern.matcher(IPV6_TEXT_INVALID);
        assertTrue(validMatcher.find());
        assertEquals(validMatcher.group(), "2001:4860:b002::68");
        assertFalse(invalidMatcher.find());
    }

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#IPV6}.
     */
    @Test
    public void testIpv6ValidationRegex() {
        System.out.println("testIpv6ValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.IPV6.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(IPV6_VALID);
        final Matcher invalidMatcher = pattern.matcher(IPV6_INVALID);
        final Matcher textMatcher = pattern.matcher(IPV6_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String EMAIL_ADDRESS_VALID = "DefinitelyRealPerson@company.org";
    private static final String EMAIL_ADDRESS_INVALID = "[DefinitelyRealPerson]@company.org";
    private static final String EMAIL_ADDRESS_TEXT_VALID = "I'm currently out of office, please forward any urgent emails to DefinitelyRealPerson@company.org.";
    private static final String EMAIL_ADDRESS_TEXT_INVALID = "I'm currently out of office, please forward any urgent emails to [DefinitelyRealPerson]@company.org.";

    /**
     * Test Detection Regex for
     * {@link AnalyticConcept.VertexType#EMAIL_ADDRESS}.
     */
    @Test
    public void testEmailAddressDetectionRegex() {
        System.out.println("testEmailAddressDetectionRegex");
        final Pattern pattern = AnalyticConcept.VertexType.EMAIL_ADDRESS.getDetectionRegex();
        final Matcher validMatcher = pattern.matcher(EMAIL_ADDRESS_TEXT_VALID);
        final Matcher invalidMatcher = pattern.matcher(EMAIL_ADDRESS_TEXT_INVALID);
        assertTrue(validMatcher.find());
        assertEquals(validMatcher.group(), "DefinitelyRealPerson@company.org");
        assertFalse(invalidMatcher.find());
    }

    /**
     * Test Validation Regex for
     * {@link AnalyticConcept.VertexType#EMAIL_ADDRESS}.
     */
    @Test
    public void testEmailAddressValidationRegex() {
        System.out.println("testEmailAddressValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.EMAIL_ADDRESS.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(EMAIL_ADDRESS_VALID);
        final Matcher invalidMatcher = pattern.matcher(EMAIL_ADDRESS_INVALID);
        final Matcher textMatcher = pattern.matcher(EMAIL_ADDRESS_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String HOST_NAME_VALID = "awesome.company.org";
    private static final String HOST_NAME_INVALID = "_d0d6y_.c0mp@ny~.biz";
    private static final String HOST_NAME_TEXT_VALID = "Welcome $USER to awesome.company.org.";
    private static final String HOST_NAME_TEXT_INVALID = "Welcome $US3R to _d0d6y_.c0mp@ny~.biz.";

    /**
     * Test Detection Regex for {@link AnalyticConcept.VertexType#HOST_NAME}.
     */
    @Test
    public void testHostNameDetectionRegex() {
        System.out.println("testHostNameDetectionRegex");
        final Pattern pattern = AnalyticConcept.VertexType.HOST_NAME.getDetectionRegex();
        final Matcher validMatcher = pattern.matcher(HOST_NAME_TEXT_VALID);
        final Matcher invalidMatcher = pattern.matcher(HOST_NAME_TEXT_INVALID);
        assertTrue(validMatcher.find());
        assertEquals(validMatcher.group(), "awesome.company.org");
        assertFalse(invalidMatcher.find());
    }

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#HOST_NAME}.
     */
    @Test
    public void testHostNameValidationRegex() {
        System.out.println("testHostNameValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.HOST_NAME.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(HOST_NAME_VALID);
        final Matcher invalidMatcher = pattern.matcher(HOST_NAME_INVALID);
        final Matcher textMatcher = pattern.matcher(HOST_NAME_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String URL_VALID = "https://www.company.org/home?id=1a2b3c4d5e6f";
    private static final String URL_INVALID = "ef6cc1.c0mp@ny~.biz?id=1a2b3c4d5e6f";
    private static final String URL_TEXT_VALID = "Browse to https://www.company.org/home?id=1a2b3c4d5e6f to log into your account.";
    private static final String URL_TEXT_INVALID = "Browse to ef6cc1.c0mp@ny~.biz?id=1a2b3c4d5e6f to log into your account.";

    /**
     * Test Detection Regex for {@link AnalyticConcept.VertexType#URL}.
     */
    @Test
    public void testUrlDetectionRegex() {
        System.out.println("testUrlDetectionRegex");
        final Pattern pattern = AnalyticConcept.VertexType.URL.getDetectionRegex();
        final Matcher validMatcher = pattern.matcher(URL_TEXT_VALID);
        final Matcher invalidMatcher = pattern.matcher(URL_TEXT_INVALID);
        assertTrue(validMatcher.find());
        assertEquals(validMatcher.group(), "https://www.company.org/home?id=1a2b3c4d5e6f");
        assertFalse(invalidMatcher.find());
    }

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#URL}.
     */
    @Test
    public void testUrlValidationRegex() {
        System.out.println("testUrlValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.URL.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(URL_VALID);
        final Matcher invalidMatcher = pattern.matcher(URL_INVALID);
        final Matcher textMatcher = pattern.matcher(URL_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String COUNTRY_VALID = "Saint Barthélemy";
    private static final String COUNTRY_INVALID = "'Straya!";
    private static final String COUNTRY_TEXT_VALID = "Welcome to Australia, we hope you enjoy your stay...";

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#COUNTRY}.
     */
    @Test
    public void testCountryValidationRegex() {
        System.out.println("testCountryValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.COUNTRY.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(COUNTRY_VALID);
        final Matcher invalidMatcher = pattern.matcher(COUNTRY_INVALID);
        final Matcher textMatcher = pattern.matcher(COUNTRY_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String GEOHASH_VALID = "xq609w9n";
    private static final String GEOHASH_INVALID = "xq-09w9n";
    private static final String GEOHASH_TEXT_VALID = "The location is xq609w9n, i'll see you there.";

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#GEOHASH}.
     */
    @Test
    public void testGeohashValidationRegex() {
        System.out.println("testGeohashValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.GEOHASH.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(GEOHASH_VALID);
        final Matcher invalidMatcher = pattern.matcher(GEOHASH_INVALID);
        final Matcher textMatcher = pattern.matcher(GEOHASH_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String MGRS_VALID = "55SFV9371406274";
    private static final String MGRS_INVALID = "55OFV9371406274";
    private static final String MGRS_TEXT_VALID = "The location is 55SFV9371406274, i'll see you there.";

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#MGRS}.
     */
    @Test
    public void testMgrsValidationRegex() {
        System.out.println("testMgrsValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.MGRS.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(MGRS_VALID);
        final Matcher invalidMatcher = pattern.matcher(MGRS_INVALID);
        final Matcher textMatcher = pattern.matcher(MGRS_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String MD5_VALID = "974724855beee3a55e93d0b47b1ee3d9";
    private static final String MD5_INVALID = "974724855beee3a55e93d0b47b1eg3d9";
    private static final String MD5_TEXT_VALID = "974724855beee3a55e93d0b47b1ee3d9 *my-awesome-file.bin";
    private static final String MD5_TEXT_INVALID = "974724855beee3a55e93d0b47b1eg3d9 *my-dodgy-file.bin";

    /**
     * Test Detection Regex for {@link AnalyticConcept.VertexType#MD5}.
     */
    @Test
    public void testMd5DetectionRegex() {
        System.out.println("testMd5DetectionRegex");
        final Pattern pattern = AnalyticConcept.VertexType.MD5.getDetectionRegex();
        final Matcher validMatcher = pattern.matcher(MD5_TEXT_VALID);
        final Matcher invalidMatcher = pattern.matcher(MD5_TEXT_INVALID);
        assertTrue(validMatcher.find());
        assertEquals(validMatcher.group(), "974724855beee3a55e93d0b47b1ee3d9");
        assertFalse(invalidMatcher.find());
    }

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#MD5}.
     */
    @Test
    public void testMd5ValidationRegex() {
        System.out.println("testMd5ValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.MD5.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(MD5_VALID);
        final Matcher invalidMatcher = pattern.matcher(MD5_INVALID);
        final Matcher textMatcher = pattern.matcher(MD5_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String SHA1_VALID = "785bdcc8564701634bbfdb87c2228670297bf713";
    private static final String SHA1_INVALID = "785bdcg8564701634bbfdb87c2228670297bf713";
    private static final String SHA1_TEXT_VALID = "785bdcc8564701634bbfdb87c2228670297bf713 *my-awesome-file.bin";
    private static final String SHA1_TEXT_INVALID = "785bdcg8564701634bbfdb87c2228670297bf713 *my-dodgy-file.bin";

    /**
     * Test Detection Regex for {@link AnalyticConcept.VertexType#SHA1}.
     */
    @Test
    public void testSha1DetectionRegex() {
        System.out.println("testSha1DetectionRegex");
        final Pattern pattern = AnalyticConcept.VertexType.SHA1.getDetectionRegex();
        final Matcher validMatcher = pattern.matcher(SHA1_TEXT_VALID);
        final Matcher invalidMatcher = pattern.matcher(SHA1_TEXT_INVALID);
        assertTrue(validMatcher.find());
        assertEquals(validMatcher.group(), "785bdcc8564701634bbfdb87c2228670297bf713");
        assertFalse(invalidMatcher.find());
    }

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#SHA1}.
     */
    @Test
    public void testSha1ValidationRegex() {
        System.out.println("testSha1ValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.SHA1.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(SHA1_VALID);
        final Matcher invalidMatcher = pattern.matcher(SHA1_INVALID);
        final Matcher textMatcher = pattern.matcher(SHA1_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }

    private static final String SHA256_VALID = "da48ad09eb2bdce5bf182fb6bba1922448a87a2a8c7fed737743e119e2bd0817";
    private static final String SHA256_INVALID = "da48ad09eb2bdce5bf182fb6bba1922448a87g2a8c7fed737743e119e2bd0817";
    private static final String SHA256_TEXT_VALID = "da48ad09eb2bdce5bf182fb6bba1922448a87a2a8c7fed737743e119e2bd0817 *my-awesome-file.bin";
    private static final String SHA256_TEXT_INVALID = "da48ad09eb2bdce5bf182fb6bba1922448a87g2a8c7fed737743e119e2bd0817 *my-dodgy-file.bin";

    /**
     * Test Detection Regex for {@link AnalyticConcept.VertexType#SHA256}.
     */
    @Test
    public void testSha256DetectionRegex() {
        System.out.println("testSha256DetectionRegex");
        final Pattern pattern = AnalyticConcept.VertexType.SHA256.getDetectionRegex();
        final Matcher validMatcher = pattern.matcher(SHA256_TEXT_VALID);
        final Matcher invalidMatcher = pattern.matcher(SHA256_TEXT_INVALID);
        assertTrue(validMatcher.find());
        assertEquals(validMatcher.group(), "da48ad09eb2bdce5bf182fb6bba1922448a87a2a8c7fed737743e119e2bd0817");
        assertFalse(invalidMatcher.find());
    }

    /**
     * Test Validation Regex for {@link AnalyticConcept.VertexType#SHA256}.
     */
    @Test
    public void testSha256ValidationRegex() {
        System.out.println("testSha256ValidationRegex");
        final Pattern pattern = AnalyticConcept.VertexType.SHA256.getValidationRegex();
        final Matcher validMatcher = pattern.matcher(SHA256_VALID);
        final Matcher invalidMatcher = pattern.matcher(SHA256_INVALID);
        final Matcher textMatcher = pattern.matcher(SHA256_TEXT_VALID);
        assertTrue(validMatcher.matches());
        assertFalse(invalidMatcher.matches());
        assertFalse(textMatcher.matches());
    }
}
