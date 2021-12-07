/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.testing;

import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import java.awt.event.ActionEvent;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol695510
 */
public class ExportGlyphTexturesActionNGTest {

    private static MockedStatic<SharedDrawable> sharedDrawableStaticMock;

    public ExportGlyphTexturesActionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        sharedDrawableStaticMock = Mockito.mockStatic(SharedDrawable.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        sharedDrawableStaticMock.close();
    }

    /**
     * Test of actionPerformed method, of class ExportGlyphTexturesAction.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testActionPerformed() throws Exception {
        System.out.println("testActionPerformed");

        // TODO
        final ExportGlyphTexturesAction instance = new ExportGlyphTexturesAction();
        final ActionEvent e = null;
//        instance.actionPerformed(e);
//        sharedDrawableStaticMock.verify(() -> SharedDrawable.exportGlyphTextures(Mockito.any(File.class)), times(1));
    }
}
