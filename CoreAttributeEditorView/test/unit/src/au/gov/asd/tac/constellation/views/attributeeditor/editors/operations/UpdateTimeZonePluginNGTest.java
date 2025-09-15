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
package au.gov.asd.tac.constellation.views.attributeeditor.editors.operations;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.views.attributeeditor.AttributeData;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class UpdateTimeZonePluginNGTest {
    
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
     * Test of edit method, of class UpdateTimeZonePlugin.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        
        final int tId1 = graph.addTransaction(vxId1, vxId2, true);
        
        final int datetimeVertexAttribute = TemporalConcept.VertexAttribute.DATETIME.ensure(graph);
        final int selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int datetimeTransactionAttribute = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);
        final int selectedTransactionAttribute = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        
        final ZonedDateTime originalTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
        graph.setObjectValue(datetimeVertexAttribute, vxId1, originalTime);
        graph.setObjectValue(datetimeVertexAttribute, vxId2, originalTime);
        graph.setObjectValue(datetimeTransactionAttribute, tId1, originalTime);
        
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedTransactionAttribute, tId1, true);
        
        assertEquals(graph.getObjectValue(datetimeVertexAttribute, vxId1), originalTime);
        assertEquals(graph.getObjectValue(datetimeVertexAttribute, vxId2), originalTime);
        assertEquals(graph.getObjectValue(datetimeTransactionAttribute, tId1), originalTime);
        
        final AttributeData vData = new AttributeData("datetime", null, datetimeVertexAttribute, 0, GraphElementType.VERTEX, "datetime", null, false, true);
        final UpdateTimeZonePlugin instance1 = new UpdateTimeZonePlugin(ZoneId.of("UTC-3"), vData);
        instance1.edit(graph, null, null);
        
        assertEquals(graph.getObjectValue(datetimeVertexAttribute, vxId1), ZonedDateTime.of(2020, 1, 1, 9, 0, 0, 0, ZoneId.of("UTC-3")));
        assertEquals(graph.getObjectValue(datetimeVertexAttribute, vxId2), originalTime);
        assertEquals(graph.getObjectValue(datetimeTransactionAttribute, tId1), originalTime);
        
        final AttributeData tData = new AttributeData("datetime", null, datetimeTransactionAttribute, 0, GraphElementType.TRANSACTION, "datetime", null, true, true);
        final UpdateTimeZonePlugin instance2 = new UpdateTimeZonePlugin(ZoneId.of("UTC+3"), tData);
        instance2.edit(graph, null, null);
        
        assertEquals(graph.getObjectValue(datetimeVertexAttribute, vxId1), ZonedDateTime.of(2020, 1, 1, 9, 0, 0, 0, ZoneId.of("UTC-3")));
        assertEquals(graph.getObjectValue(datetimeVertexAttribute, vxId2), originalTime);
        assertEquals(graph.getObjectValue(datetimeTransactionAttribute, tId1), ZonedDateTime.of(2020, 1, 1, 15, 0, 0, 0, ZoneId.of("UTC+3")));
    }
}
