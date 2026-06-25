/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.type;

import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities.ExtractedVertexType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * SchemaVertexTypeUtilities Test.
 *
 * @author andromeda
 */
public class SchemaVertexTypeUtilitiesNGTest {
        
    @Test
    public void getExtractedTypesFromTextTest() {
         System.out.println("getExtractedTypesFromText");
         try (final MockedStatic<SchemaVertexTypeUtilities> mockedSchemaVertexTypeUtilities = mockStatic(SchemaVertexTypeUtilities.class, Mockito.CALLS_REAL_METHODS);
                 final MockedStatic<VertexDominanceCalculator> mockedCalculator = mockStatic(VertexDominanceCalculator.class)) {
             
             final SchemaVertexType schematype1 = mock(SchemaVertexType.class);
             final VertexDominanceCalculator mockedCalculatorDefault = mock(VertexDominanceCalculator.class);
             final Pattern mockRegex = mock(Pattern.class);             
             final Matcher mockMatcher = mock(Matcher.class);
             final String text = "test.com";
             mockedCalculator.when(() -> VertexDominanceCalculator.getDefault()).thenReturn(mockedCalculatorDefault);
             when(schematype1.getDetectionRegex()).thenReturn(mockRegex);
             when(mockRegex.matcher(Mockito.any())).thenReturn(mockMatcher);
             when(mockMatcher.find()).thenReturn(Boolean.TRUE, Boolean.FALSE);
             when(mockMatcher.group()).thenReturn(text);
             
             final List<ExtractedVertexType> extractedTypes = new ArrayList<>();
             final List<SchemaVertexType> schemaTypes = new ArrayList<>();
             schemaTypes.add(schematype1);
                          
             final Collection<SchemaVertexType> schematypesCollection = Collections.unmodifiableCollection(schemaTypes) ;
             List<ExtractedVertexType> extractedTypesFromText = SchemaVertexTypeUtilities.getExtractedTypesFromText(schematypesCollection, text, extractedTypes);
             assertTrue(extractedTypesFromText.get(0).getType() == schematype1);
         }
    }
    
    @Test
    public void extractVertexTypesTest() {
         System.out.println("extractVertexTypes");
         try (final MockedStatic<SchemaVertexTypeUtilities> mockedSchemaVertexTypeUtilities = mockStatic(SchemaVertexTypeUtilities.class, Mockito.CALLS_REAL_METHODS);
                 final MockedStatic<VertexDominanceCalculator> mockedCalculator = mockStatic(VertexDominanceCalculator.class)) {
             
             final SchemaVertexType schematype1 = mock(SchemaVertexType.class);
             final VertexDominanceCalculator mockedCalculatorDefault = mock(VertexDominanceCalculator.class);
             final Pattern mockRegex = mock(Pattern.class);             
             final Matcher mockMatcher = mock(Matcher.class);
             final String text = "test.com";
             mockedCalculator.when(() -> VertexDominanceCalculator.getDefault()).thenReturn(mockedCalculatorDefault);
             when(schematype1.getDetectionRegex()).thenReturn(mockRegex);
             when(mockRegex.matcher(Mockito.any())).thenReturn(mockMatcher);
             when(mockMatcher.find()).thenReturn(Boolean.TRUE, Boolean.FALSE);
             when(mockMatcher.group()).thenReturn(text);
             
             final List<SchemaVertexType> schemaTypes = new ArrayList<>();
             schemaTypes.add(schematype1);
             mockedSchemaVertexTypeUtilities.when(() -> SchemaVertexTypeUtilities.getTypes()).thenReturn(schemaTypes);
                          
             List<ExtractedVertexType> extractedTypesFromText = SchemaVertexTypeUtilities.extractVertexTypes(text);
             assertTrue(extractedTypesFromText.get(0).getType() == schematype1);
         }
    }
    
    @Test
    public void getTypeNameTest() {
         System.out.println("getTypeName");
         try (final MockedStatic<SchemaVertexTypeUtilities> mockedSchemaVertexTypeUtilities = mockStatic(SchemaVertexTypeUtilities.class, Mockito.CALLS_REAL_METHODS)) {
             final SchemaVertexType schematype1 = mock(SchemaVertexType.class);
             
             mockedSchemaVertexTypeUtilities.when(() -> SchemaVertexTypeUtilities.getDefaultType()).thenReturn(schematype1);
             // no name
             SchemaVertexType result = SchemaVertexTypeUtilities.getType(null, null);
             assertTrue(result == schematype1);
             mockedSchemaVertexTypeUtilities.verify( () -> SchemaVertexTypeUtilities.getDefaultType(), times(1));
             // some name
             result = SchemaVertexTypeUtilities.getType("some_text", null);
             assertTrue(result == schematype1);
             mockedSchemaVertexTypeUtilities.verify( () -> SchemaVertexTypeUtilities.getDefaultType(), times(2));
         }            
    }

    @Test
    public void getTypeOrBuildNewTest() {
        System.out.println("getTypeOrBuildNew");
        try (final MockedStatic<SchemaVertexTypeUtilities> mockedSchemaVertexTypeUtilities = mockStatic(SchemaVertexTypeUtilities.class, Mockito.CALLS_REAL_METHODS)) {
            final SchemaVertexType defaultSchematype = mock(SchemaVertexType.class);
            final SchemaVertexType schematype1 = mock(SchemaVertexType.class);
            when(defaultSchematype.getName()).thenReturn("default");
            when(schematype1.getName()).thenReturn("test_name");
            mockedSchemaVertexTypeUtilities.when(() -> SchemaVertexTypeUtilities.getDefaultType()).thenReturn(defaultSchematype);
            mockedSchemaVertexTypeUtilities.when(() -> SchemaVertexTypeUtilities.getType("test_name")).thenReturn(schematype1);

            // returns default
            SchemaVertexType result = SchemaVertexTypeUtilities.getTypeOrBuildNew("default");
            assertTrue(result == defaultSchematype);

            // test_name
            result = SchemaVertexTypeUtilities.getTypeOrBuildNew("test_name");
            assertTrue(result == schematype1);
            // it called get Type twice in body of method
            mockedSchemaVertexTypeUtilities.verify(() -> SchemaVertexTypeUtilities.getType(Mockito.anyString()), times(2));
        }
    }

    @Test
    public void containsTypeTest() {
        System.out.println("containsType");
        try (final MockedStatic<SchemaVertexTypeUtilities> mockedSchemaVertexTypeUtilities = mockStatic(SchemaVertexTypeUtilities.class, Mockito.CALLS_REAL_METHODS)) {
            final SchemaVertexType schematype1 = mock(SchemaVertexType.class);
            final SchemaVertexType schematype2 = mock(SchemaVertexType.class);

            final List<SchemaVertexType> schemaTypes = new ArrayList<>();
            schemaTypes.add(schematype1);

            final Collection<SchemaVertexType> schematypesCollection = Collections.unmodifiableCollection(schemaTypes);
            mockedSchemaVertexTypeUtilities.when(() -> SchemaVertexTypeUtilities.getTypes()).thenReturn(schematypesCollection);

            // returns default
            assertFalse(SchemaVertexTypeUtilities.containsType(schematype2));
            assertTrue(SchemaVertexTypeUtilities.containsType(schematype1));

        }
    }
    
    @Test
    public void containsTypeNameTest() {
        System.out.println("containsTypeName");
        try (final MockedStatic<SchemaVertexTypeUtilities> mockedSchemaVertexTypeUtilities = mockStatic(SchemaVertexTypeUtilities.class, Mockito.CALLS_REAL_METHODS)) {
            final SchemaVertexType schematype1 = mock(SchemaVertexType.class);
            final SchemaVertexType schematype2 = mock(SchemaVertexType.class);
            when(schematype1.getName()).thenReturn("schemaType1");
            when(schematype2.getName()).thenReturn("schemaType2");
            
            final List<SchemaVertexType> schemaTypes = new ArrayList<>();
            schemaTypes.add(schematype1);

            final Collection<SchemaVertexType> schematypesCollection = Collections.unmodifiableCollection(schemaTypes);
            mockedSchemaVertexTypeUtilities.when(() -> SchemaVertexTypeUtilities.getTypes()).thenReturn(schematypesCollection);

            // returns default
            assertFalse(SchemaVertexTypeUtilities.containsTypeName("schemaType2"));
            assertTrue(SchemaVertexTypeUtilities.containsTypeName("schemaType1"));

        }
    }
    
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
}
