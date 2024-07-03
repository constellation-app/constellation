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
package au.gov.asd.tac.constellation.utilities.font;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author groombridge34a
 */
public class BidirectionalTextUtilitiesNGTest {
    
    private static final String LTR_TEXT = "left to right text";
    private static final String RTL_TEXT1 = "لك نأ كل حضوأ نأ دب ال نكل";
    private static final String RTL_TEXT2 = "أ كل حضوأ نأ دب ال نكل";
    
    /**
     * Passing null to Bidi returns null.
     */
    @Test
    public void testNull() {
        assertNull(BidirectionalTextUtilities.doBidi(null));
    }
    
    /**
     * Passing left-to-right text to Bidi returns the same text back.
     */
    @Test
    public void testLtr() {
        assertEquals(BidirectionalTextUtilities.doBidi(LTR_TEXT), LTR_TEXT);
    }
    
    /**
     * Passing right-to-left text to Bidi rearranges the text. Constructing an
     * input with a newline is necessary to hit both paths through the output
     * String construction.
     */
    @Test
    public void testRtl() {
        String expected = "لكن لا بد أن أوضح لك أن كل" + "\n" + "لكن لا بد أن أوضح لك أ";
        assertEquals(
                BidirectionalTextUtilities.doBidi(RTL_TEXT1 + "\n" + RTL_TEXT2), 
                expected);
    }
}
