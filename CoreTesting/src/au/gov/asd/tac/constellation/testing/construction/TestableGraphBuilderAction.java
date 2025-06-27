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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.testing.CoreTestingPluginRegistry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author capricornunicorn123
 */
@ActionID(category = "Developer", id = "au.gov.asd.tac.constellation.testing.construction.TestableGraphBuilderAction")
@ActionRegistration(displayName = "#CTL_TestableGraphBuilderAction")
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Developer", position = 1000)
})
@NbBundle.Messages("CTL_TestableGraphBuilderAction=Testable Graph Builder")
public final class TestableGraphBuilderAction implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(TestableGraphBuilderAction.class.getName());

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph sg = new StoreGraph(schema);
        schema.newGraph(sg);
        final Graph dualGraph = new DualGraph(sg, false);

        final Future<?> f = PluginExecutor.startWith(CoreTestingPluginRegistry.TESTABLE_GRAPH_BUILDER)
                .executeWriteLater(dualGraph);

        try {
            // ensure testable graph has finished before opening the graph
            f.get();
            final String graphName = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).getLabel().trim().toLowerCase();
            GraphOpener.getDefault().openGraph(dualGraph, graphName);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Testable graph creation was interrupted", ex);
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
}