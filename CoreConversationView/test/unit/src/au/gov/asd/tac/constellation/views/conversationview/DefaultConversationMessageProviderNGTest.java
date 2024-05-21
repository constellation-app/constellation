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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ContentConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for DefaultConversationMessageProvider
 * 
 * @author Delphinus8821
 */
public class DefaultConversationMessageProviderNGTest {
    
    private Graph graph;
    private int vxId1, vxId2, tnId1, tnId2, tnId3;

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
     * Test of getMessages method, of class DefaultConversationMessageProvider.
     */
    @Test
    public void testGetMessages() {
        System.out.println("getMessages");
        setupGraph();
        final GraphReadMethods rg = graph.getReadableGraph();
        final List<ConversationMessage> messages = new ArrayList();
        final ConversationMessage message1 = new ConversationMessage(tnId1, vxId1, ConversationSide.LEFT);
        final ConversationMessage message2 = new ConversationMessage(tnId3, vxId1, ConversationSide.LEFT);
        messages.add(message1);
        messages.add(message2);
        final DefaultConversationMessageProvider instance = new DefaultConversationMessageProvider();
        final List<ConversationMessage> resultMessages = new ArrayList();
        instance.getMessages(rg, resultMessages);
        assertEquals(messages.size(), resultMessages.size());
        
        // test getTotalMessageCount
        final int expResult = 2;
        final int result = instance.getTotalMessageCount();
        assertEquals(result, expResult);
    }
    
    
    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        try {
            final WritableGraph wg = graph.getWritableGraph("", true);
            
            final int contentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(wg);
            final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            
            vxId1 = wg.addVertex();
            vxId2 = wg.addVertex();
            tnId1 = wg.addTransaction(vxId1, vxId2, true);
            tnId2 = wg.addTransaction(vxId2, vxId1, true);
            tnId3 = wg.addTransaction(vxId1, vxId2, false);
            
            wg.setStringValue(contentAttributeId, tnId1, "first message");
            wg.setStringValue(contentAttributeId, tnId2, "second message");
            wg.setStringValue(contentAttributeId, tnId3, "third message");
            
            wg.setBooleanValue(vertexSelectedAttribute, vxId1, true);
            
            wg.commit();
            
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }
}
