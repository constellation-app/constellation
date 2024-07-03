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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ContentAnalysisManager
 * 
 * @author Delphinus8821
 */
public class ContentAnalysisManagerNGTest {
    
    private final Graph graph = mock(Graph.class);
    private final int[] performOnElements = {0, 1, 2, 3, 4};
    private final Set<Integer> elementsOfInterest = new HashSet();
    private final int graphElementCapacity = 1;
    private final GraphElementType elementType = GraphElementType.VERTEX;
    private final int performOnAttributeID = 0;

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
     * Test of getStringListAllocator method, of class ContentAnalysisManager.
     */
    @Test
    public void testGetStringListAllocator() {
        System.out.println("getStringListAllocator");
        final List<String> list = new ArrayList();
        list.add("vertex1");
        list.add("vertex2");
        list.add("vertex3");
        final ReadableGraph rg = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(rg);
        when(rg.getStringValue(Mockito.anyInt(), Mockito.anyInt())).thenReturn("test");
        
        final ContentAnalysisManager instance = new ContentAnalysisManager(graph, performOnElements, elementsOfInterest, graphElementCapacity, elementType, performOnAttributeID);
        final ThreadAllocator result = instance.getStringListAllocator(list);
        final ThreadedPhraseAdaptor adaptor = result.nextAdaptor();
        
        assertEquals(adaptor.getNextPhrase(), "vertex1");
        assertEquals(adaptor.hasNextPhrase(), true);
        assertEquals(adaptor.getCurrentElementID(), 0);
        assertEquals(adaptor.getWorkload(), 3);
    }

    /**
     * Test of getGraphElementThreadAllocator method, of class ContentAnalysisManager.
     */
    @Test
    public void testGetGraphElementThreadAllocator() {
        System.out.println("getGraphElementThreadAllocator");
        final ReadableGraph rg = mock(ReadableGraph.class);
        when(graph.getReadableGraph()).thenReturn(rg);
        when(rg.getStringValue(Mockito.anyInt(), Mockito.anyInt())).thenReturn("test");
        
        final ContentAnalysisManager instance = new ContentAnalysisManager(graph, performOnElements, elementsOfInterest, graphElementCapacity, elementType, performOnAttributeID);
        final ThreadAllocator result = instance.getGraphElementThreadAllocator();
        final ThreadedPhraseAdaptor adaptor = result.nextAdaptor();
        adaptor.connect();
        
        assertEquals(adaptor.getNextPhrase(), "test");
        assertEquals(adaptor.hasNextPhrase(), true);
        assertEquals(adaptor.getCurrentElementID(), 0);
        assertEquals(adaptor.getWorkload(), 5);
        adaptor.disconnect();
    }
    
}
