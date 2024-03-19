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
package au.gov.asd.tac.constellation.views.wordcloud.phraseanalysis;

import static org.testng.Assert.assertFalse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for PhraseAnalysisModelLoader
 * 
 * @author Delphinus8821
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class PhraseAnalysisModelLoaderNGTest extends ConstellationTest {
    
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
     * Test of loadMap method, of class PhraseAnalysisModelLoader.
     * @throws java.lang.Exception
     */
    @Test
    public void testLoadMap() throws Exception {
        System.out.println("loadMap");
        PhraseAnalysisModelLoader.loadMap();
        assertFalse(PhraseAnalysisModelLoader.getExcludedWords().isEmpty());
        assertFalse(PhraseAnalysisModelLoader.getDelimiters().isEmpty());
    }
}
