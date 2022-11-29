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
package au.gov.asd.tac.constellation.visual.opengl;

import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.utilities.visual.VisualChangeBuilder;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLVisualProcessor;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.JFXPanel;

/**
 *
 * @author twilight_sparkle
 */
public class GLVisualProcessorTester {

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

    public static void main(String[] args) {
        final GLVisualProcessorDemo demo = new GLVisualProcessorDemo();
        final VisualAccess access = new DummyVisualAccess();
        final GLVisualProcessor processor = new GLVisualProcessor();
        final VisualManager visualManager = new VisualManager(access, processor);
        processor.startVisualising(visualManager);
        demo.runDemo(processor, visualManager);
        final List<VisualChange> changeSet = new ArrayList<>();
        changeSet.add(new VisualChangeBuilder(VisualProperty.VERTICES_REBUILD).build());
        changeSet.add(new VisualChangeBuilder(VisualProperty.CONNECTIONS_REBUILD).build());
        changeSet.add(new VisualChangeBuilder(VisualProperty.BACKGROUND_COLOR).build());
        changeSet.add(new VisualChangeBuilder(VisualProperty.HIGHLIGHT_COLOR).build());
        changeSet.add(new VisualChangeBuilder(VisualProperty.CONNECTIONS_OPACITY).build());
        changeSet.add(new VisualChangeBuilder(VisualProperty.BLAZE_SIZE).build());
        changeSet.add(new VisualChangeBuilder(VisualProperty.DRAW_FLAGS).build());
        changeSet.add(new VisualChangeBuilder(VisualProperty.CAMERA).build());
        changeSet.add(new VisualChangeBuilder(VisualProperty.TOP_LABELS_REBUILD).build());
        changeSet.add(new VisualChangeBuilder(VisualProperty.BOTTOM_LABELS_REBUILD).build());
        changeSet.add(new VisualChangeBuilder(VisualProperty.CONNECTION_LABELS_REBUILD).build());
        visualManager.addMultiChangeOperation(changeSet);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
        }
        for (int i = 0; i < 10; i++) {
            ((DummyVisualAccess) access).zoomOut();
            visualManager.addSingleChangeOperation(new VisualChangeBuilder(VisualProperty.CAMERA).build());
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
            }
        }
    }

}
