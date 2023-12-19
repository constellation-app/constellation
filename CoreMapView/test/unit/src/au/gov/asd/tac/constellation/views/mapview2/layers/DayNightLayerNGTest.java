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
package au.gov.asd.tac.constellation.views.mapview2.layers;

import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.views.mapview2.MapDetails;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.MapViewPane;
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import javafx.scene.Group;
import org.mockito.Mockito;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.logging.Logger;

/**
 *
 * @author altair1673
 */
public class DayNightLayerNGTest {

    private static final Logger LOGGER = Logger.getLogger(DayNightLayerNGTest.class.getName());

    private static final MapDetails mapDetails = new MapDetails(MapDetails.MapType.SVG, 1000, 999, 85.0511, -85.0511, -180, 180, "Full World (default)",
                           ConstellationInstalledFileLocator.locate("modules/ext/data/WorldMap1000x999.svg", "au.gov.asd.tac.constellation.views.mapview", MapView.class.getProtectionDomain()));
    

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
     * Test of getLayer method, of class DayNightLayer.
     */
    @Test
    public void testGetLayer() {
        System.out.println("getLayer");

        final MapViewTopComponent component = Mockito.mock(MapViewTopComponent.class);
        final MapViewPane mapViewPane = Mockito.spy(new MapViewPane(component));
        final MapView map = Mockito.spy(new MapView(mapViewPane, mapDetails));

        final DayNightLayer instance = new DayNightLayer(map, 5);

        final Group result = instance.getLayer();
        assertEquals(result != null, true);

    }


}
