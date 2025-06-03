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
package au.gov.asd.tac.constellation.graph.interaction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.visual.VisualChangeBuilder;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLVisualProcessor;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javafx.embed.swing.JFXPanel;

/**
 *
 * @author twilight_sparkle
 */
public class GLVisualProcessorGraphTester {

    static JFXPanel p = new JFXPanel();

    private static class GLVisualProcessorDemo {

        private VisualManager manager;
        private VisualProcessor processor = null;
        private final Frame frame;

        public GLVisualProcessorDemo() {
            frame = new Frame("JOGL Tester");
            frame.setSize(1920, 1080);
            frame.setResizable(false);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    frame.dispose();
                    System.exit(0);
                }
            });
        }

        public void runDemo(final VisualProcessor processor, final VisualManager manager) {
            if (this.manager != null) {
                this.manager.stopProcessing();
                frame.remove(this.manager.getVisualComponent());
            }
            this.processor = processor;
            this.manager = manager;
            final Component canvas = manager.getVisualComponent();
            frame.add(canvas);
            canvas.requestFocus();
            manager.startProcessing();
            frame.setVisible(true);
        }

    }

    public static StoreGraph createGraph() {
        StoreGraph g = new StoreGraph();
        final int v1 = g.addVertex();
        final int v2 = g.addVertex();
        final int t1 = g.addTransaction(v1, v2, false);
        final int t2 = g.addTransaction(v1, v2, false);
        final int t3 = g.addTransaction(v1, v2, true);
        final int t4 = g.addTransaction(v2, v1, true);
        final int loop = g.addTransaction(v1, v1, false);

        final int xAttr = VisualConcept.VertexAttribute.X.ensure(g);
        final int yAttr = VisualConcept.VertexAttribute.Y.ensure(g);
        final int zAttr = VisualConcept.VertexAttribute.Z.ensure(g);
        final int bgIconAttr = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(g);
        final int iconAttr = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(g);
        final int backgroundColorAttr = VisualConcept.GraphAttribute.BACKGROUND_COLOR.ensure(g);
        final int cameraAttr = VisualConcept.GraphAttribute.CAMERA.ensure(g);
        final int blazeAttr = VisualConcept.VertexAttribute.BLAZE.ensure(g);
        final int widthAttr = VisualConcept.TransactionAttribute.WIDTH.ensure(g);
        g.setDoubleValue(widthAttr, t1, 0.8);
        g.setDoubleValue(widthAttr, t2, 1.0);
        g.setDoubleValue(widthAttr, t3, 1.2);
        g.setDoubleValue(widthAttr, t4, 1.4);
        g.setDoubleValue(widthAttr, loop, 1.6);
        g.setIntValue(xAttr, v1, -2);
        g.setIntValue(xAttr, v2, 2);
        g.setIntValue(yAttr, v1, 0);
        g.setIntValue(yAttr, v2, 0);
        g.setIntValue(zAttr, v1, -10);
        g.setIntValue(zAttr, v2, -10);
        g.setStringValue(bgIconAttr, v1, DefaultIconProvider.FLAT_SQUARE.getExtendedName());
        g.setStringValue(bgIconAttr, v2, DefaultIconProvider.FLAT_SQUARE.getExtendedName());
        g.setStringValue(iconAttr, v1, DefaultIconProvider.UNKNOWN.getExtendedName());
        g.setStringValue(iconAttr, v2, DefaultIconProvider.UNKNOWN.getExtendedName());
        g.setObjectValue(blazeAttr, v1, new Blaze(0, ConstellationColor.AZURE));
        g.setObjectValue(cameraAttr, 0, new Camera());
        g.setObjectValue(backgroundColorAttr, 0, ConstellationColor.NIGHT_SKY);
        g.setPrimaryKey(GraphElementType.VERTEX, xAttr);
        g.setPrimaryKey(GraphElementType.TRANSACTION, widthAttr);
        return g;
    }

    public static void main(String[] args) {
        final GLVisualProcessorDemo demo = new GLVisualProcessorDemo();
        final StoreGraph graph = createGraph();
        final Graph dualGraph = new DualGraph(graph, false);
        final GraphVisualAccess access = new GraphVisualAccess(dualGraph);
        final GLVisualProcessor processor = new GLVisualProcessor();
        final VisualManager visualManager = new VisualManager(access, processor);
        processor.startVisualising(visualManager);
        demo.runDemo(processor, visualManager);
        final GraphChangeListener gct = (event) -> visualManager.updateFromIndigenousChanges();
        gct.graphChanged(null);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
        }
        try {
            WritableGraph wg = dualGraph.getWritableGraph("linkmode", false);
            try {
                final int connectionModeAttr = VisualConcept.GraphAttribute.CONNECTION_MODE.ensure(wg);
                wg.setObjectValue(connectionModeAttr, 0, ConnectionMode.LINK);
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
        }
        gct.graphChanged(null);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        try {
            WritableGraph wg = dualGraph.getWritableGraph("transmode", false);
            try {
                final int connectionModeAttr = VisualConcept.GraphAttribute.CONNECTION_MODE.ensure(wg);
                wg.setObjectValue(connectionModeAttr, 0, ConnectionMode.TRANSACTION);
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
        }
        gct.graphChanged(null);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        final int[] changed = new int[]{0, 1};
        try {
            WritableGraph wg = dualGraph.getWritableGraph("blazin", false);
            try {
                final int blazeAttr = VisualConcept.VertexAttribute.BLAZE.ensure(wg);
                for (int i = 0; i < 10000; i++) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                    }
                    wg.setObjectValue(blazeAttr, 0, new Blaze(((Blaze) wg.getObjectValue(blazeAttr, 0)).getAngle() + 1, ConstellationColor.BLUE));
                    wg = wg.flush(false);
                    visualManager.addSingleChangeOperation(new VisualChangeBuilder(VisualProperty.VERTEX_BLAZE_ANGLE).forItems(changed).build());
                }
            } finally {
                wg.commit();
            }
        } catch (InterruptedException ex) {
        }
    }
}
