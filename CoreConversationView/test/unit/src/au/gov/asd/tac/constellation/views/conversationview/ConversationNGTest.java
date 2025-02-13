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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for Conversation 
 * 
 * @author Delphinus8821
 */
public class ConversationNGTest {

    private static final Logger LOGGER = Logger.getLogger(ConversationNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
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
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }


    /**
     * Test of setPageNumber method, of class Conversation.
     */
    @Test
    public void testSetPageNumber() {
        System.out.println("setPageNumber");
        
        final int pageNumber = 2;
        final Conversation instance = new Conversation();
        instance.setPageNumber(pageNumber);
        final int expResult = instance.getPageNumber();
        assertEquals(pageNumber, expResult);
    }

    /**
     * Test of setTotalMessageCount method, of class Conversation.
     */
    @Test
    public void testSetTotalMessageCount() {
        System.out.println("setTotalMessageCount");
        
        final int totalMessageCount = 3000;
        final Conversation instance = new Conversation();
        instance.setTotalMessageCount(totalMessageCount);
        final int expResult = instance.getTotalMessageCount();
        assertEquals(totalMessageCount, expResult);
    }

    /**
     * Test of setTotalPages method, of class Conversation.
     */
    @Test
    public void testSetTotalPages() {
        System.out.println("setTotalPages");
        
        final int totalPages = 45;
        final Conversation instance = new Conversation();
        instance.setTotalPages(totalPages);
        final int expResult = instance.getTotalPages();
        assertEquals(totalPages, expResult);
    }

    /**
     * Test of setContentPerPage method, of class Conversation.
     */
    @Test
    public void testSetContentPerPage() {
        System.out.println("setContentPerPage");
        
        final int contentPerPage = 0;
        final Conversation instance = new Conversation();
        instance.setContentPerPage(contentPerPage);
        final int expResult = instance.getContentPerPage();
        assertEquals(contentPerPage, expResult);
    }

    /**
     * Test of updateMessages method, of class Conversation.
     */
    @Test
    public void testUpdateMessages() {
        System.out.println("updateMessages");
        
        try (MockedStatic<ConversationController> controllerStatic = Mockito.mockStatic(ConversationController.class)) {
            final ConversationController controller = spy(ConversationController.class);
            controllerStatic.when(ConversationController::getDefault).thenReturn(controller);
            final ConversationBox box = mock(ConversationBox.class);
            when(controller.getConversationBox()).thenReturn(box);
            doNothing().when(box).setInProgress();
            
            final GraphReadMethods graph = mock(GraphReadMethods.class);
            final Conversation instance = new Conversation();
            final List expResult = new ArrayList();
            final List result = instance.updateMessages(graph);
            verify(controller, times(2)).getConversationBox();
            verify(box).setInProgress();
            assertEquals(result, expResult);
        }
    }
}
