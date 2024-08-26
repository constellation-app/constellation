package au.gov.asd.tac.constellation.utilities.gui;


import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import org.testng.Assert;
import org.testng.annotations.Test;

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
/**
 *
 * @author Andromeda-224
 */
public class ScreenWindowsHelperNGTest {
    static final Logger LOGGER = Logger.getLogger(ScreenWindowsHelperNGTest.class.getName());
        
    /**
     * Test getMainWindowCentrePoint method of class ScreenWindowsHelper.
     */
    @Test
    public void testGetMainWindowCentrePoint() {
        Point testPoint = ScreenWindowsHelper.getMainWindowCentrePoint();
        
        //Test method with no windows
        Assert.assertTrue(testPoint == null);
        
        // Test method with mocked point
        final Point point = mock(Point.class);
        try (final MockedStatic<ScreenWindowsHelper> screenWindowsHelperStatic = Mockito.mockStatic(ScreenWindowsHelper.class)) {
            screenWindowsHelperStatic.when(ScreenWindowsHelper::getMainWindowCentrePoint).thenReturn(point);
            Assert.assertEquals(ScreenWindowsHelper.getMainWindowCentrePoint(), point);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "\n******* ERROR RUNNING TEST *******\n", e);
            Assert.fail();
        }
    }
}
