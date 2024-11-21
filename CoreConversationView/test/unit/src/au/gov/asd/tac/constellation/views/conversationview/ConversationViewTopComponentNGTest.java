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
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for ConversationViewTopComponent
 * 
 * @author Delphinus8821
 */
public class ConversationViewTopComponentNGTest {
    
    private ConversationViewTopComponent topComponent;
    private static final Logger LOGGER = Logger.getLogger(ConversationViewTopComponentNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            WaitForAsyncUtils.checkAllExceptions = false;
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {            
            FxToolkit.cleanupStages();
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of createStyle method, of class ConversationViewTopComponent.
     */
    @Test
    public void testCreateStyle() {
        System.out.println("createStyle");
        final String expResult = "resources/conversation.css";
        topComponent = new ConversationViewTopComponent();
        final String result = topComponent.createStyle();
        assertEquals(result, expResult);

    }

    /**
     * Test of createContent method, of class ConversationViewTopComponent.
     */
    @Test
    public void testCreateContent() {
        System.out.println("createContent");
        try (final MockedStatic<ConversationController> controllerStatic = Mockito.mockStatic(ConversationController.class)) {
            final ConversationController controller = spy(ConversationController.class);
            controllerStatic.when(ConversationController::getDefault).thenReturn(controller);

            final Conversation conversation = controller.getConversation();
            when(controller.getConversation()).thenReturn(conversation);
            final ConversationBox expResult = new ConversationBox(conversation);
            topComponent = new ConversationViewTopComponent();
            final ConversationBox result = topComponent.createContent();
            assertEquals(result.getConversation(), expResult.getConversation());
        }
    }

    /**
     * Test of handleNewGraph method, of class ConversationViewTopComponent.
     */
    @Test
    public void testHandleNewGraph() {
        System.out.println("handleNewGraph");
        try (final MockedStatic<ConversationController> controllerStatic = Mockito.mockStatic(ConversationController.class)) {
            final ConversationController controller = spy(ConversationController.class);
            controllerStatic.when(ConversationController::getDefault).thenReturn(controller);
            final Graph graph = mock(Graph.class);
            topComponent = new ConversationViewTopComponent();
            topComponent.handleNewGraph(graph);
            verify(ConversationController.getDefault()).updateComponent();
        }
    }
}
