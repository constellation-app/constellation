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
 * Test class for LayerTitlePane
 * 
 * @author Delphinus8821
 */
public class LayerTitlePaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(LayerTitlePaneNGTest.class.getName());

    public LayerTitlePaneNGTest() {
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
     * Test of setQuery method, of class LayerTitlePane.
     * 
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testSetQueryVertex() throws InterruptedException {
        final int layerId = 2;
        final String layerName = "layerName";
        final BitMaskQuery query = new BitMaskQuery(new Query(GraphElementType.VERTEX, ""), layerId, "");
        LayerTitlePane ltp = new LayerTitlePane(layerId, layerName, query);
        LayerTitlePane spiedLtp = spy(ltp);

        doCallRealMethod().when(spiedLtp).setQuery(Mockito.any());
        spiedLtp.setQuery(query);

        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            System.out.println("Queued platform task for test");
            latch.countDown();
        });

        latch.await();
        
        verify(spiedLtp).setQuery(Mockito.eq(query));
    }

    /**
     * Test of setQuery method, of class LayerTitlePane.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testSetQueryTransaction() throws InterruptedException {
        final int layerId = 2;
        final String layerName = "layerName";
        final BitMaskQuery query = new BitMaskQuery(new Query(GraphElementType.TRANSACTION, ""), layerId, "");
        LayerTitlePane ltp = new LayerTitlePane(layerId, layerName, query);
        LayerTitlePane spiedLtp = spy(ltp);

        doCallRealMethod().when(spiedLtp).setQuery(Mockito.any());
        spiedLtp.setQuery(query);

        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            System.out.println("Queued platform task for test");
            latch.countDown();
        });

        latch.await();

        verify(spiedLtp).setQuery(Mockito.eq(query));
    }

    /**
     * Test of setSelected method, of class LayerTitlePane.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testSetSelected() throws InterruptedException {
        final int layerId = 2;
        final String layerName = "layerName";
        final boolean value = true;
        final BitMaskQuery query = new BitMaskQuery(new Query(GraphElementType.TRANSACTION, ""), layerId, "");
        LayerTitlePane ltp = new LayerTitlePane(layerId, layerName, query);
        LayerTitlePane spiedLtp = spy(ltp);
        
        doCallRealMethod().when(spiedLtp).setSelected(Mockito.anyBoolean());
        spiedLtp.setSelected(value);

        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            System.out.println("Queued platform task for test");
            latch.countDown();
        });

        latch.await();

        verify(spiedLtp).setSelected(Mockito.eq(true));
    }

    /**
     * Test of setDescription method, of class LayerTitlePane.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testSetDescription() throws InterruptedException {
        final int layerId = 2;
        final String layerName = "layerName";
        final String description = "description";
        final BitMaskQuery query = new BitMaskQuery(new Query(GraphElementType.TRANSACTION, ""), layerId, "");
        LayerTitlePane ltp = new LayerTitlePane(layerId, layerName, query);
        LayerTitlePane spiedLtp = spy(ltp);

        doCallRealMethod().when(spiedLtp).setDescription(Mockito.anyString());
        spiedLtp.setDescription(description);

        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            System.out.println("Queued platform task for test");
            latch.countDown();
        });

        latch.await();

        verify(spiedLtp).setDescription(Mockito.eq(description));
    }
    
}
