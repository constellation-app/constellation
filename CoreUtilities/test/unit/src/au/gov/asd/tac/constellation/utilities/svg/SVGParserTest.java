/*
* Copyright 2010-2023 Australian Signals Directorate
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.testng.annotations.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link SVGParser}
 * 
 * @author capricornunicorn123
 */
public class SVGParserTest {
       
    public SVGParserTest() {
        
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parse method, of class SVGParser.
     * @throws java.lang.Exception
     * @throws java.io.IOException
     */
    @Test(expectedExceptions=IOException.class)
    public void testParseException() throws Exception, IOException {
        SVGParser.parse(null);
    }
    
    /**
     * Test of parse method, of class SVGParser.
     * @throws java.lang.Exception
     */
    @Test
    public void testParse() throws Exception {
        
        //Ensure loading template file passes
        TestingSVGFile template = TestingSVGFile.TESTING_TEMPLATE;
        final InputStream inputStream = template.getClass().getResourceAsStream(template.getFileName());
        SVGData templateSVG = SVGParser.parse(inputStream);
        CommonTests.testLoadedData(templateSVG);

    }

    /**
     * Test of sanitisePlanText method, of class SVGParser.
     */
    @Test
    public void testSanitisePlanText() {
        ArrayList<Integer> knownBadCharacters = new ArrayList<>();
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
    }
    
}
