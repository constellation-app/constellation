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

import au.gov.asd.tac.constellation.views.layers.LayersViewController;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class LayersViewPaneNGTest {
    
    public LayersViewPaneNGTest() {
    }
    private static final Logger LOGGER = Logger.getLogger(LayersViewPaneNGTest.class.getName());

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public void tearDownClass() throws Exception {
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
     * Test of setLayers method, of class LayersViewPane.
     */
    @Test
    public void testSetLayers() {
    }

    /**
     * Test of setDefaultLayers method, of class LayersViewPane.
     */
    @Test
    public void testSetDefaultLayers() {
    }

    /**
     * Test of setEnabled method, of class LayersViewPane.
     */
    @Test
    public void testSetEnabled() throws InterruptedException {
        System.out.println("testSetEnabled");
        LayersViewPane lvp = new LayersViewPane(LayersViewController.getDefault());
        LayersViewPane spiedLvp = spy(lvp);
        doCallRealMethod().when(spiedLvp).setEnabled(Mockito.anyBoolean());
        doNothing().when(spiedLvp).setCenter(Mockito.any());
        
        spiedLvp.setEnabled(true);
        
        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            System.out.println("Queued platform task for test");
            latch.countDown();
        });

        latch.await();
        
        verify(spiedLvp).setEnabled(Mockito.eq(true));
        verify(spiedLvp).setCenter(Mockito.same(spiedLvp.layersViewVBox));
    }
    
    /**
     * Test of setEnabled method, of class LayersViewPane.
     */
    @Test
    public void testSetEnabledFalse() throws InterruptedException {
        System.out.println("testSetEnabledFalse");
        LayersViewPane lvp = new LayersViewPane(LayersViewController.getDefault());
        LayersViewPane spiedLvp = spy(lvp);
        doCallRealMethod().when(spiedLvp).setEnabled(Mockito.anyBoolean());
        doNothing().when(spiedLvp).setCenter(Mockito.any());
        
        spiedLvp.setEnabled(false);
        
        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            System.out.println("Queued platform task for test");
            latch.countDown();
        });

        latch.await();
        
        verify(spiedLvp).setEnabled(Mockito.eq(false));
        verify(spiedLvp).setCenter(Mockito.same(spiedLvp.noGraphPane));
    }
    
}
