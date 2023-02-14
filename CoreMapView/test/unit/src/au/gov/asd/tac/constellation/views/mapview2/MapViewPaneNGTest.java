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
package au.gov.asd.tac.constellation.views.mapview2;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.mapview.providers.MapProvider;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerState;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.mockito.Mockito;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author altair1673
 */
public class MapViewPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(MapViewPaneNGTest.class.getName());


    public MapViewPaneNGTest() {

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
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getNewMarkerID method, of class MapViewPane.
     */
    @Test
    public void testGetNewMarkerID() {
        MapViewTopComponent mapViewTopComponent = Mockito.mock(MapViewTopComponent.class);

        final MapViewPane instance = new MapViewPane(mapViewTopComponent);

        Mockito.when(mapViewTopComponent.getMapViewPane()).thenCallRealMethod();
        Mockito.when(mapViewTopComponent.getNewMarkerID()).thenCallRealMethod();


        System.out.println("getNewMarkerID");
        int result = instance.getNewMarkerID();
        assertEquals(result, 1);

        result = instance.getNewMarkerID();
        assertEquals(result, 2);
    }


}
