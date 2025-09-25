/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

import au.gov.asd.tac.constellation.utilities.gui.field.framework.InfoWindowSupport.InfoWindow;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class InfoWindowSupportNGTest {

    private ConstellationInput constellationInputMock;
    private InfoWindowTest infoWindowMock;
    private static final Logger LOGGER = Logger.getLogger(InfoWindowSupportNGTest.class.getName());

    @Test
    public void infoWindowSupportTest_setWindow() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            // Makes runLater run immediately
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
                ((Runnable) iom.getArgument(0)).run();
                return null;
            });
            StackPane contentMock = mock(StackPane.class);
            infoWindowMock.setWindowContents(contentMock);
            assertEquals(infoWindowMock.testCount, 1);
        }
    }

    //@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = "Not supported yet.")
    @Test
    public void infoWindowSupportTest_changed() {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            // Makes runLater run immediately
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
                ((Runnable) iom.getArgument(0)).run();
                return null;
            });

            infoWindowMock.changed(mock());

            verify(infoWindowMock).refreshWindow();
        }
    }

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public void tearDownClass() {
        try {
            WaitForAsyncUtils.clearExceptions();
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            // Makes runLater run immediately
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
                ((Runnable) iom.getArgument(0)).run();
                return null;
            });
            constellationInputMock = mock(ConstellationInput.class);
            infoWindowMock = spy(new InfoWindowTest(constellationInputMock));
        }
    }

    @AfterMethod
    public void tearDownMethod() {
        constellationInputMock = null;
        infoWindowMock = null;
    }

    private class InfoWindowTest extends InfoWindow {

        protected int testCount = 0;

        public InfoWindowTest(final ConstellationInput parent) {
            super(parent);
        }

        @Override
        public void setWindowContents(final Node content) {
            testCount++;
        }

        @Override
        protected void refreshWindow() {
            //Do Nothing
        }
    }
}
