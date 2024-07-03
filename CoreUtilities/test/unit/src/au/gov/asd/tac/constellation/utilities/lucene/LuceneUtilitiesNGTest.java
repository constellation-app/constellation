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
package au.gov.asd.tac.constellation.utilities.lucene;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Lucene Utilities Test
 *
 * @author arcturus
 */
public class LuceneUtilitiesNGTest {

    public LuceneUtilitiesNGTest() {
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

    @Test
    public void escapeLuceneWithRegex() {
        final String text = "abc*";
        final String result = LuceneUtilities.escapeLucene(text);
        assertEquals(result, "abc\\*");
    }

    @Test
    public void escapeLuceneWithCurlyBrackets() {
        final String text = "{}";
        final String result = LuceneUtilities.escapeLucene(text);
        assertEquals(result, "\\{\\}");
    }

    @Test
    public void escapeLuceneWithSpecialCharacters() {
        final String text = "/+-&|!(){}[]^\"~*?:\\";
        final String result = LuceneUtilities.escapeLucene(text);
        assertEquals(result, "\\/\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\\\");
    }

    @Test
    public void escapeLuceneWithNull() {
        final String text = null;
        final String result = LuceneUtilities.escapeLucene(text);
        assertEquals(result, null);
    }
}
