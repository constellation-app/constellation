/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * RecordStore Query Plugin Test.
 *
 * @author arcturus
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class RecordStoreQueryPluginNGTest extends ConstellationTest {

    /**
     * Test of getResult method, of class RecordStoreQueryPlugin.
     */
    @Test
    public void testGetResult() throws InterruptedException, PluginException {
        System.out.println("getResult");

        final RecordStoreQueryPlugin instance = new RecordStoreQueryPluginMockImpl();
        final GraphReadMethods graph = new StoreGraph();
        final PluginInteraction interaction = null;
        final PluginParameters parameters = null;

        instance.read(graph, interaction, parameters);
        instance.query(interaction, parameters);

        final RecordStore result = instance.getResult();
        assertEquals(result.size(), 2);
    }

    /**
     * Test of edit method, of class RecordStoreQueryPlugin.
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");

        final RecordStoreQueryPlugin instance = new RecordStoreQueryPluginMockImpl();
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final Graph graph = new DualGraph(schema); // only using a dual graph because of the need to pass a GraphWriteMethods graph to the edit() method.
        final PluginInteraction interaction = null;
        final PluginParameters parameters = null;

        ReadableGraph rg = graph.getReadableGraph();
        try {
            instance.read(rg, interaction, parameters);
            instance.query(interaction, parameters);
        } finally {
            rg.release();
        }

        GraphRecordStore query;

        rg = graph.getReadableGraph();
        try {
            query = GraphRecordStoreUtilities.getAll(rg, false, false);
        } finally {
            rg.release();
        }

        final WritableGraph wg = graph.getWritableGraph("", true);
        try {
            VisualConcept.VertexAttribute.X.ensure(wg);
            VisualConcept.VertexAttribute.Y.ensure(wg);
            VisualConcept.VertexAttribute.Z.ensure(wg);
            VisualConcept.GraphAttribute.CAMERA.ensure(wg);

            instance.edit(wg, interaction, parameters);
        } finally {
            wg.commit();
        }

        rg = graph.getReadableGraph();
        try {
            query = GraphRecordStoreUtilities.getTransactions(rg, false, false);
        } finally {
            rg.release();
        }

        // verify nothing has moved
        query.reset();
        query.next();
        assertEquals(query.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X), "10.0");
        assertEquals(query.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y), "10.0");
        assertEquals(query.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z), "10.0");
        assertEquals(query.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.X), "20.0");
        assertEquals(query.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.Y), "20.0");
        assertEquals(query.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.Z), "20.0");
        query.next();
        assertEquals(query.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X), "30.0");
        assertEquals(query.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y), "30.0");
        assertEquals(query.get(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z), "30.0");
        assertEquals(query.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.X), "40.0");
        assertEquals(query.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.Y), "40.0");
        assertEquals(query.get(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.Z), "40.0");
    }

    /**
     * Test of getRecordStoreType method, of class RecordStoreQueryPlugin.
     */
    @Test
    public void testGetRecordStoreType() {
        System.out.println("getRecordStoreType");

        final RecordStoreQueryPlugin instance = new RecordStoreQueryPluginMockImpl();
        final String expResult = GraphRecordStoreUtilities.SOURCE;
        final String result = instance.getRecordStoreType();
        assertEquals(result, expResult);
    }

    /**
     * Test of addValidator method, of class RecordStoreQueryPlugin.
     */
    @Test
    public void testAddValidator() {
        System.out.println("addValidator");

        final RecordStoreValidator validator = new RecordStoreValidator1MockImpl();
        final RecordStoreQueryPlugin instance = new RecordStoreQueryPluginMockImpl();

        assertTrue(instance.getValidators().isEmpty());

        instance.addValidator(validator);

        assertEquals(instance.getValidators().size(), 1);
    }

    /**
     * Test of removeValidator method, of class RecordStoreQueryPlugin.
     */
    @Test
    public void testRemoveValidator() {
        System.out.println("removeValidator");

        final RecordStoreValidator validator1 = new RecordStoreValidator1MockImpl();
        final RecordStoreValidator validator2 = new RecordStoreValidator2MockImpl();
        final RecordStoreQueryPlugin instance = new RecordStoreQueryPluginMockImpl();

        assertTrue(instance.getValidators().isEmpty());

        instance.addValidator(validator1);
        instance.addValidator(validator1);
        instance.addValidator(validator2);
        instance.addValidator(validator2);

        assertEquals(instance.getValidators().size(), 4);

        instance.removeValidator(RecordStoreValidator1MockImpl.class);

        assertEquals(instance.getValidators().size(), 2);
    }

    /**
     * Test of getValidators method, of class RecordStoreQueryPlugin.
     */
    @Test
    public void testGetValidators() {
        System.out.println("getValidators");

        final RecordStoreQueryPlugin instance = new RecordStoreQueryPluginMockImpl();
        final List<RecordStoreValidator> expResult = new ArrayList<>();
        final List<RecordStoreValidator> result = instance.getValidators();
        assertEquals(result, expResult);
    }

    private class RecordStoreQueryPluginMockImpl extends RecordStoreQueryPlugin {

        @Override
        public RecordStore query(final RecordStore query, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final RecordStore recordStore = new GraphRecordStore();
            recordStore.add();
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "foo");
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 10);
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 10);
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 10);
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "bar");
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.X, 20);
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.Y, 20);
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.Z, 20);

            recordStore.add();
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "foo2");
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 30);
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 30);
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 30);
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "bar2");
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.X, 40);
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.Y, 40);
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.Z, 40);

            return recordStore;
        }

        @Override
        public String getName() {
            return "Record Store Query Plugin Test";
        }

    }

    private class RecordStoreValidator1MockImpl extends RecordStoreValidator {

    }

    private class RecordStoreValidator2MockImpl extends RecordStoreValidator {

    }
}
