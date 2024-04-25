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
package au.gov.asd.tac.constellation.graph.node.gui;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.GraphObjectUtilities;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonReader;
import au.gov.asd.tac.constellation.graph.file.io.GraphParseException;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.utilities.gui.TextIoProgress;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * A default simple GraphOpener implementation.
 *
 * @author algol
 */
@ServiceProvider(service = GraphOpener.class)
public final class SimpleGraphOpener extends GraphOpener {

    @Override
    public void openGraph(final GraphDataObject gdo) {
        new SimpleGraphFileOpener(gdo, null, null).execute();
    }

    @Override
    public void openGraph(final Graph graph, final String name) {
        final GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject(name, true);
        new SimpleGraphFileOpener(gdo, graph, null).execute();
    }

    @Override
    public void openGraph(final Graph graph, final String name, Runnable doAfter) {
        final GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject(name, true);
        new SimpleGraphFileOpener(gdo, graph, doAfter).execute();
    }

    @Override
    public void openGraph(final Graph graph, final String name, final boolean numbered) {
        final GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject(name, numbered);
        new SimpleGraphFileOpener(gdo, graph, null).execute();
    }

    private static class SimpleGraphFileOpener extends SwingWorker<Object, Integer> {

        private final GraphDataObject gdo;
        private Graph graph;
        private final Runnable doAfter;
        private Exception gex;
        private long time;
        private static final Logger LOGGER = Logger.getLogger(SimpleGraphFileOpener.class.getName());
        

        SimpleGraphFileOpener(final GraphDataObject gdo, final Graph graph, Runnable doAfter) {
            this.gdo = gdo;
            this.graph = graph;
            this.doAfter = doAfter;
        }

        @Override
        protected Object doInBackground() throws Exception {
            final File graphFile = FileUtil.toFile(gdo.getPrimaryFile());
            if (graph == null) {
                try {
                    final long t0 = System.currentTimeMillis();
                    graph = new GraphJsonReader().readGraphZip(graphFile, new TextIoProgress(true));
                    time = System.currentTimeMillis() - t0;
                } catch (final GraphParseException ex) {
                    gex = ex;
                    LOGGER.log(Level.WARNING, "{0}", ex.getStackTrace());
                }
            }

            return null;
        }

        @Override
        protected void done() {
            if (gex != null) {
                Logger.getLogger(SimpleGraphFileOpener.class.getName()).log(Level.INFO, "Opening " + gdo.getPrimaryFile().getPath(), gex);
                String exName = gex.getClass().getCanonicalName();
                if (exName.lastIndexOf('.') != -1) {
                    exName = exName.substring(exName.lastIndexOf('.') + 1);
                }
                final NotifyDescriptor d = new NotifyDescriptor.Message(String.format("%s error opening graph:%n%s", exName, gex.getMessage()), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } else if (graph != null) {
                StatusDisplayer.getDefault().setStatusText(String.format("%s read complete (%.1fs)", gdo.getPrimaryFile().getName(), time / 1000F));
                final SimpleGraphTopComponent vtc = new SimpleGraphTopComponent(gdo, graph);
                vtc.open();
                vtc.requestActive();
            } else {
                // Do nothing
            }

            if (doAfter != null) {
                doAfter.run();
            }
        }
    }
}
