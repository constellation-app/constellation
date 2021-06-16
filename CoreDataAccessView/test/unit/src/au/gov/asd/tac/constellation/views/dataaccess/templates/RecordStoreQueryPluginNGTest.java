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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * RecordStore Query Plugin Test.
 *
 * @author arcturus
 */
public class RecordStoreQueryPluginNGTest {

    public RecordStoreQueryPluginNGTest() {
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

    /**
     * Test of getResult method, of class RecordStoreQueryPlugin.
     */
    @Test
    public void testGetResult() {
        final RecordStoreQueryPlugin instance = new RecordStoreQueryPluginMockImpl();
        final GraphReadMethods graph = new StoreGraph();
        final PluginInteraction interaction = null;
        final PluginParameters parameters = null;
        try {
            instance.read(graph, interaction, parameters);
            instance.query(interaction, parameters);
        } catch (InterruptedException | PluginException ex) {
            fail(ex.getLocalizedMessage());
        }
        final RecordStore result = instance.getResult();
        assertEquals(result.size(), 2);
    }

    /**
     * Test of getRecordStoreType method, of class RecordStoreQueryPlugin.
     */
    @Test
    public void testGetRecordStoreType() {
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
        final RecordStoreValidator validator = new RecordStoreValidator1MockImpl();
        final RecordStoreQueryPlugin instance = new RecordStoreQueryPluginMockImpl();

        assertEquals(instance.getValidators().size(), 0);

        instance.addValidator(validator);

        assertEquals(instance.getValidators().size(), 1);
    }

    /**
     * Test of removeValidator method, of class RecordStoreQueryPlugin.
     */
    @Test
    public void testRemoveValidator() {
        final RecordStoreValidator validator1 = new RecordStoreValidator1MockImpl();
        final RecordStoreValidator validator2 = new RecordStoreValidator2MockImpl();
        final RecordStoreQueryPlugin instance = new RecordStoreQueryPluginMockImpl();

        assertEquals(instance.getValidators().size(), 0);

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
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "foo");
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 10);
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 10);
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 10);
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "bar");
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.X, 20);
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.Y, 20);
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.Z, 20);

            recordStore.add();
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL, "foo2");
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 30);
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 30);
            recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 30);
            recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.LABEL, "bar2");
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
