/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.layers.components;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.Query;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for QueryInputPane
 * 
 * @author Delphinus8821
 */
public class QueryInputPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(QueryInputPaneNGTest.class.getName());

    public QueryInputPaneNGTest() {
    }

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
        } catch (TimeoutException ex) {
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
     * Test of setQuery method, of class QueryInputPane.
     * 
     * @throws java.lang.InterruptedException
     */
//    @Test
//    public void testSetQuery() throws InterruptedException {
//        final String title = "Vertex Query: ";
//        final String queryString = "Type == 'Event'";
//        final String description = "Description";
//        final int layerID = 2;
//        final Query query = new Query(GraphElementType.VERTEX, queryString);
//        final BitMaskQuery bitMaskQuery = new BitMaskQuery(query, layerID, queryString);
//        final LayerTitlePane ltp = new LayerTitlePane(layerID, queryString, bitMaskQuery);
//        final LayerTitlePane spiedLtp = spy(ltp);
//        final QueryInputPane instance = new QueryInputPane(spiedLtp, title, description, queryString, 150, 75, true);
//        final QueryInputPane spiedInstance = spy(instance);
//
//        spiedInstance.setQuery(queryString);
//        String result = spiedInstance.getQuery();
//        assertEquals(queryString, result);
//    }

    /**
     * Test of setValidity method, of class QueryInputPane.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testSetValidity() throws InterruptedException {
        final String title = "Vertex Query: ";
        final String queryString = "Type == 'Event'";
        final String description = "Description";
        final int layerID = 2;
        final Query query = new Query(GraphElementType.VERTEX, queryString);
        final BitMaskQuery bitMaskQuery = new BitMaskQuery(query, layerID, queryString);
        final LayerTitlePane ltp = new LayerTitlePane(layerID, queryString, bitMaskQuery);
        final LayerTitlePane spiedLtp = spy(ltp);
        final QueryInputPane instance = new QueryInputPane(spiedLtp, title, description, queryString, 150, 75, true);
        final QueryInputPane spiedInstance = spy(instance);

        doCallRealMethod().when(spiedInstance).setValidity(Mockito.anyBoolean());
        spiedInstance.setValidity(true);

        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            System.out.println("Queued platform task for test");
            latch.countDown();
        });

        latch.await();

        verify(spiedInstance).setValidity(Mockito.eq(true));
    }
}
