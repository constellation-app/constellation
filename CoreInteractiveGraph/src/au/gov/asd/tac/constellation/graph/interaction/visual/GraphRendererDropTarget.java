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
package au.gov.asd.tac.constellation.graph.interaction.visual;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.framework.HitState;
import au.gov.asd.tac.constellation.graph.interaction.framework.HitState.HitType;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper.DropInfo;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.awt.Point;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;
import org.openide.util.Lookup;

/**
 * This class handles drag and drop on a graph as visualised by a {@link InteractiveGLVisualProcessor}.
 * <p>
 * This class implements AWT DropTargetAdapter behaviour by using lookup to get a list of all {@link GraphDropper}
 * instances every time a drop occurs, and then passing the drop event and the graph off to teh first
 * {@link GraphDropper} that accepts the data being dropped.
 *
 * @author algol
 */
public final class GraphRendererDropTarget extends DropTargetAdapter {

    private final InteractiveGLVisualProcessor processor;
    private final VisualManager manager;
    private Point dropLocation;
    private final Graph graph;

    /**
     * Create a new GraphRendererDropTarget for the specified graph.
     *
     * @param graph The {@link Graph} to provide drag and drop for.
     * @param manager The {@link VisualManager} managing the graph.
     * @param processor The {@link InteractiveGLVisualProcessor} visualising the graph.
     */
    public GraphRendererDropTarget(final Graph graph, final VisualManager manager, final InteractiveGLVisualProcessor processor) {
        this.graph = graph;
        this.manager = manager;
        this.processor = processor;
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        dropLocation = dtde.getLocation();
    }

    @Override
    public void drop(final DropTargetDropEvent dtde) {

        BiConsumer<Graph, DropInfo> dropHandler = null;

        // Accept the drop, work out whether any graph dropper will handle it, and if so mark the drop as complete.
        dtde.acceptDrop(dtde.getDropAction());
        final Collection<? extends GraphDropper> droppers = Lookup.getDefault().lookupAll(GraphDropper.class);
        for (final GraphDropper dropper : droppers) {
            dropHandler = dropper.drop(dtde);
            if (dropHandler != null) {
                break;
            }
        }
        dtde.dropComplete(dropHandler != null);

        // If a dropper did provide a handler for this drop event, process it in a new thread, providing it with the information obtained from hit testing the graph for the drop location.
        // (Note this has to be in a new thread because hit testing is done on the EDT, which we need to wait for the results of, but we are already on the EDT).
        if (dropHandler != null) {
            BiConsumer<Graph, DropInfo> resultDropHandler = dropHandler;
            final Thread computeDrop = new Thread(() -> {
                final Vector3f dropGraphLocation = processor.windowToGraphCoordinates(processor.getDisplayCamera(), dropLocation);
                final BlockingQueue<HitState> hitTestQueue = new ArrayBlockingQueue<>(1);
                manager.addOperation(processor.hitTestPoint(dropLocation.x, dropLocation.y, hitTestQueue));
                HitState hitState;
                while (true) {
                    try {
                        hitState = hitTestQueue.take();
                        break;
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                final DropInfo dropInfo = new DropInfo(dropGraphLocation, hitState.getCurrentHitId(), hitState.getCurrentHitType() == HitType.VERTEX, hitState.getCurrentHitType() == HitType.TRANSACTION);
                resultDropHandler.accept(graph, dropInfo);
            });
            computeDrop.setName("Graph Renderer Drop Target");
            computeDrop.start();
        }
    }

}
