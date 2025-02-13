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
package au.gov.asd.tac.constellation.utilities.file;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link FileNameCleaner}
 * 
 * @author capricornunicorn123
 */
public class FileNameCleanerNGTest {
    
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
     * Test of cleanFileName method, of class FileNameCleaner.
     */
    @Test
    public void testCleanFileName() {
        assertEquals("myFileName.txt",FileNameCleaner.cleanFileName("myFileName/.txt"));
        assertEquals("myFileName.txt",FileNameCleaner.cleanFileName("myFileName\\.txt"));
        assertEquals("myFileName.txt",FileNameCleaner.cleanFileName("myFileName:.txt"));
        assertEquals("myFileName.txt",FileNameCleaner.cleanFileName("myFileName*.txt"));
        assertEquals("myFileName.txt",FileNameCleaner.cleanFileName("myFileName?.txt"));
        assertEquals("myFileName.txt",FileNameCleaner.cleanFileName("myFileName\".txt"));
        assertEquals("myFileName.txt",FileNameCleaner.cleanFileName("myFileName<.txt"));
        assertEquals("myFileName.txt",FileNameCleaner.cleanFileName("myFileName>.txt"));
        assertEquals("myFileName.txt",FileNameCleaner.cleanFileName("myFileName|.txt"));
    }   
}
