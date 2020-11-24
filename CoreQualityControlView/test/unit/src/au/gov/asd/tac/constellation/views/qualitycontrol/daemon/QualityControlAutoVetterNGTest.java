/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.qualitycontrol.daemon;

import au.gov.asd.tac.constellation.graph.Graph;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Quality Control Auto Vetter Test
 *
 * @author arcturus
 */
public class QualityControlAutoVetterNGTest {

    public QualityControlAutoVetterNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of updateQualityControlState method, of class
     * QualityControlAutoVetter.
     */
    @Test
    public void testUpdateQualityControlStateWithNoGraph() {
        final Graph graph = null;

        final QualityControlState stateBefore = QualityControlAutoVetter.getInstance().getQualityControlState();
        QualityControlAutoVetter.updateQualityControlState(graph);
        final QualityControlState stateAfter = QualityControlAutoVetter.getInstance().getQualityControlState();

        assertEquals(stateBefore, stateAfter);
    }

    /**
     * Test of getInstance method, of class QualityControlAutoVetter.
     */
    @Test
    public void testGetInstance() {
        final QualityControlAutoVetter instance1 = QualityControlAutoVetter.getInstance();
        final QualityControlAutoVetter instance2 = QualityControlAutoVetter.getInstance();
        assertEquals(instance1, instance2);
    }

}
