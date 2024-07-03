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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterKind;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.stage.FileChooser.ExtensionFilter;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class FileParameterTypeNGTest {
    
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
     * Test of build method, of class FileParameterType. One Parameter version
     */
    @Test
    public void testBuildOneParameter() {
        System.out.println("buildOneParameter");
        
        final PluginParameter<FileParameterValue> fileParam = FileParameterType.build("My File");
        
        assertTrue(fileParam.getParameterValue().get().isEmpty());
        assertEquals(fileParam.getId(), "My File");
        assertEquals(fileParam.getType().getId(), "file");
    }

    /**
     * Test of build method, of class FileParameterType. Two Parameter version
     */
    @Test
    public void testBuildTwoParameter() {
        System.out.println("buildTwoParameter");
        
        final List<String> files = Arrays.asList("myFile1", "myFile2", "myFile3");
        final FileParameterValue fileValue = new FileParameterValue(files);
        final PluginParameter<FileParameterValue> fileParam = FileParameterType.build("My File", fileValue);
        
        assertEquals(fileParam.getParameterValue().get().size(), 3);
        assertEquals(fileParam.getId(), "My File");
        assertEquals(fileParam.getType().getId(), "file");
    }

    /**
     * Test of setKind method, of class FileParameterType.
     */
    @Test
    public void testSetKind() {
        System.out.println("setKind");
        
        final PluginParameter<FileParameterValue> fileParam = FileParameterType.build("My File");
        assertEquals(FileParameterType.getKind(fileParam), FileParameterKind.OPEN_MULTIPLE);
        
        FileParameterType.setKind(fileParam, FileParameterKind.SAVE);
        assertEquals(FileParameterType.getKind(fileParam), FileParameterKind.SAVE);
    }

    /**
     * Test of setFileFilters method, of class FileParameterType.
     */
    @Test
    public void testSetFileFilters() {
        System.out.println("setFileFilters");
        
        final PluginParameter<FileParameterValue> fileParam = FileParameterType.build("My File");
        assertNull(FileParameterType.getFileFilters(fileParam));
        
        final ExtensionFilter filter = new ExtensionFilter("test", FileExtensionConstants.TEXT);
        FileParameterType.setFileFilters(fileParam, filter);
        assertEquals(FileParameterType.getFileFilters(fileParam), filter);
    }
    
    /**
     * Test of set method, of class FileParameterValue.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        
        final FileParameterValue fileValue = new FileParameterValue();
        assertTrue(fileValue.get().isEmpty());
        
        final List<File> files = Arrays.asList(new File("myFile1"), new File("myFile2"), new File("myFile3"));
        assertTrue(fileValue.set(files));
        assertEquals(fileValue.get().size(), 3);
        assertTrue(fileValue.set(null));
        assertTrue(fileValue.get().isEmpty());
        //value matches what is already stored
        assertFalse(fileValue.set(new ArrayList<>()));
        assertTrue(fileValue.get().isEmpty());
    }
    
    /**
     * Test of setStringValue method, of class FileParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");
        
        final FileParameterValue fileValue = new FileParameterValue();
        assertTrue(fileValue.get().isEmpty());
        
        assertTrue(fileValue.setStringValue("file1;file2;file3"));
        assertEquals(fileValue.get().size(), 3);
        // fails as it produces the same as the one already stored
        assertFalse(fileValue.setStringValue("file1;;file2;file3;"));
        assertEquals(fileValue.get().size(), 3);
    }
    
    /**
     * Test of setObjectValue method, of class FileParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final FileParameterValue fileValue = new FileParameterValue();
        assertTrue(((List<File>) fileValue.getObjectValue()).isEmpty());
        
        final List<File> files = Arrays.asList(new File("myFile1"), new File("myFile2"), new File("myFile3"));
        assertTrue(fileValue.setObjectValue(files));
        assertEquals(((List<File>) fileValue.getObjectValue()).size(), 3);
    }
    
    /**
     * Test of createCopy method, of class FileParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        final FileParameterValue fileValue = new FileParameterValue();
        final ParameterValue fileCopy = fileValue.createCopy();
        assertTrue(fileValue.equals(fileCopy));
        
        fileValue.setStringValue("file1;file2;file3");
        assertFalse(fileValue.equals(fileCopy));
    }
    
    /**
     * Test of equals method, of class FileParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final FileParameterValue fileValue = new FileParameterValue();
        fileValue.setStringValue("file1;file2;file3");
        final FileParameterValue comp1 = new FileParameterValue();
        final FileParameterValue comp2 = new FileParameterValue(Arrays.asList("file1", "file2", "file3"));
        
        assertFalse(fileValue.equals(null));
        assertFalse(fileValue.equals(true));
        assertFalse(fileValue.equals(comp1));
        assertTrue(fileValue.equals(comp2));
    }
    
    /**
     * Test of toString method, of class FileParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final FileParameterValue fileValue = new FileParameterValue();
        assertTrue(fileValue.toString().isEmpty());
        final FileParameterValue fileValue2 = new FileParameterValue(Arrays.asList("file1", "file2", "file3"));
        assertEquals(fileValue2.toString(), "file1;file2;file3");
    }
}
