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
package au.gov.asd.tac.constellation.views.qualitycontrol.widget;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent.QualityCategory;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlState;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Tooltip;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DefaultQualityControlAutoButtonNGTest {

    private StoreGraph graph;
    private List<QualityControlEvent> events;
    private List<QualityControlRule> rules;

    //Dependencies (will be mocked)
    private QualityControlEvent qualityControlEvent;

    public DefaultQualityControlAutoButtonNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        rules = new ArrayList<>();
        events = new ArrayList<>();

        //mock QualityControlEvent
        qualityControlEvent = mock(QualityControlEvent.class);
        when(qualityControlEvent.getCategory()).thenReturn(QualityCategory.CRITICAL);
        when(qualityControlEvent.getReasons()).thenReturn("Reason 1, Reason2");

        events.add(qualityControlEvent);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of qualityControlChanged method, of class
     * DefaultQualityControlAutoButton, with Null State.
     */
    @Test
    public void testQualityControlChangedWithNullState() throws InterruptedException {
        System.out.println("qualityControlChanged");
        if (!GraphicsEnvironment.isHeadless()) {
            //needed to initialise the toolkit
            new JFXPanel();
            DefaultQualityControlAutoButton instance = new DefaultQualityControlAutoButton();

            String expRiskText = "Quality Category: " + QualityCategory.OK.name();
            String expStyleText = instance.DEFAULT_TEXT_STYLE + instance.BUTTON_STYLE;
            String expTooltipText = null;
            instance.qualityControlChanged(null);

            String resultRiskText = instance.getText();
            String resultStyleText = instance.getStyle();
            final Tooltip resultTooltipText = instance.getTooltip();

            assertEquals(resultRiskText, expRiskText);
            assertEquals(resultStyleText, expStyleText);
            assertEquals(resultTooltipText, expTooltipText);
        }
    }

    /**
     * Test of qualityControlChanged method, of class
     * DefaultQualityControlAutoButton, with a valid State.
     */
    @Test
    public void testQualityControlChangedWithValidState() throws InterruptedException {
        System.out.println("qualityControlChanged");
        if (!GraphicsEnvironment.isHeadless()) {
            QualityControlState state = new QualityControlState(graph.getId(), events, rules);
            //needed to initialise the toolkit
            new JFXPanel();
            DefaultQualityControlAutoButton instance = new DefaultQualityControlAutoButton();

            final String expRiskText = "Quality Category: CRITICAL";
            final String expStyleText = "-fx-text-fill: rgb(255,255,255);-fx-background-color: rgba(150,13,13,1.000000);" + instance.BUTTON_STYLE;
            final String expTooltipText = "Reason 1, Reason2";

            instance.qualityControlChanged(state);
            Thread.sleep(100);
            final String resultRiskText = instance.getText();
            final String resultStyleText = instance.getStyle();
            final Tooltip resultTooltipText = instance.getTooltip();

            assertEquals(resultRiskText, expRiskText);
            assertEquals(resultStyleText, expStyleText);
            assertEquals(resultTooltipText.getText(), expTooltipText);
        }
    }
}
