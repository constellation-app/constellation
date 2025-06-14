/*
* Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.svg;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link SVGParser}
 * 
 * @author capricornunicorn123
 */
public class SVGParserNGTest {
    
    @BeforeClass
    public static void setUpClass() {
        // Not currently required
    }
    
    @AfterClass
    public static void tearDownClass() {
        // Not currently required
    }
    
    @BeforeMethod
    public void setUpMethod() {
        // Not currently required
    }
    
    @AfterMethod
    public void tearDownMethod() {
        // Not currently required
    }
    
    /**
     * Test of parse(), of class SVGParser.
     * @throws java.io.IOException
     */
    @Test(expectedExceptions=IOException.class)
    public void testParseException() throws IOException {
        SVGParser.parse(null);
    }
    
    /**
     * Test of parse(), of class SVGParser.
     * @throws java.io.IOException
     */
    @Test
    public void testParse() throws IOException {
        //Ensure loading template file passes
        TestingSVGFile template = TestingSVGFile.TESTING_TEMPLATE_COMPLIANT;
        final InputStream inputStream = template.getClass().getResourceAsStream(template.getFileName());
        SVGData templateSVG = SVGParser.parse(inputStream);
        SVGTestUtilities.testLoadedData(templateSVG);
    }
    
    /**
     * Test of parse(), of class SVGParser.
     * @throws java.io.IOException
     */
    @Test(expectedExceptions=IllegalStateException.class)
    public void testParseInvalidFile() throws IOException {
        //Ensure loading template file with invalid lines fails
        TestingSVGFile template = TestingSVGFile.TESTING_TEMPLATE_INVALID_JSON;
        final InputStream inputStream = template.getClass().getResourceAsStream(template.getFileName());
        SVGParser.parse(inputStream);
    }
    
    /**
     * Test of parse(), of class SVGParser.
     * @throws java.io.IOException
     */
    @Test(expectedExceptions=IllegalStateException.class)
    public void testParseMultipleRoots() throws IOException {
        //Ensure loading template file with multiple roots fails
        TestingSVGFile template = TestingSVGFile.TESTING_TEMPLATE_INVALID_MULTI_ROOT;
        final InputStream inputStream = template.getClass().getResourceAsStream(template.getFileName());
        SVGParser.parse(inputStream);
    }

    /**
     * Test of sanitisePlanText(), of class SVGParser.
     */
    @Test
    public void testSanitisePlanText() {
        List<Integer> knownBadCharacters = new ArrayList<>();
        knownBadCharacters.add(0); // NUL
        knownBadCharacters.add(12); // FF
        knownBadCharacters.add(34); // "
        knownBadCharacters.add(38); // &
        knownBadCharacters.add(39); // '
        knownBadCharacters.add(60); // <
        knownBadCharacters.add(62); // >
        
        for(int i = 0; i < 127; i++){
            String originalCharacter = Character.toString((char) i);
            String sanitisedCharacter = SVGParser.sanitisePlanText(originalCharacter);
            if (knownBadCharacters.contains(i)) {
                assertNotEquals(sanitisedCharacter, originalCharacter); 
            } else {
                assertEquals(sanitisedCharacter, originalCharacter);
            }
        }
        String fireChineseCharacter = "火";
        assertEquals(SVGParser.sanitisePlanText(fireChineseCharacter), SVGParser.NON_LATIN_CHARACTER_OMMMISION_TEXT);       
    }   
}
