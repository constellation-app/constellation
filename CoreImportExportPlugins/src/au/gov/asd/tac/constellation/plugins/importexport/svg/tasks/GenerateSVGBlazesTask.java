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
package au.gov.asd.tac.constellation.plugins.importexport.svg.tasks;

import au.gov.asd.tac.constellation.plugins.importexport.svg.GraphVisualisationReferences;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGObjectConstants;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGTemplateConstants;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import java.util.List;
import au.gov.asd.tac.constellation.plugins.MultiTaskInteraction.SharedInteractionRunnable;

/**
 * A runnable task designed to build SVG assets representing graph blazes.
 * This task is designed to run concurrently and can be used to build a single blaze or all blazes.
 * 
 * @author capricornunicorn123
 */
public class GenerateSVGBlazesTask implements SharedInteractionRunnable {

    private final GraphVisualisationReferences graph;
    private final List<Integer> vertexIndicies;
    private final List<SVGObject> output;
    private final int totalSteps;
    private int currentStep;
    private boolean complete = false;
    
    public GenerateSVGBlazesTask(final GraphVisualisationReferences graph, final List<Integer> vertexIndicies, final List<SVGObject> output){
        this.graph = graph;
        this.vertexIndicies = vertexIndicies;
        this.output = output;
        this.totalSteps = vertexIndicies.size();
    }

    @Override
    public void run() {
        try {
            graph.initialise();
            vertexIndicies.forEach(vertexIndex -> {
                if (graph.inView(vertexIndex) && graph.isBlazed(vertexIndex)&& (!graph.selectedElementsOnly || graph.isVertexSelected(vertexIndex)) && graph.getVertexVisibility(vertexIndex) > 0) {

                    // Get relevant variables
                    final int blazeAngle = graph.getBlazeAngle(vertexIndex);
                    final float blazeSize = graph.getBlazeSize();
                    final float blazeWidth = 512 * blazeSize;
                    final float blazeHeight = 128 * blazeSize;
                    final Vector4f edgePosition = graph.offsetPosition(graph.getVertexPosition(vertexIndex), graph.getVertexScaledRadius(vertexIndex), Math.toRadians(blazeAngle + 90D));

                    // Build the blaze
                    final SVGObject svgBlaze = SVGTemplateConstants.BLAZE.getSVGObject();
                    svgBlaze.setID(String.format("blaze-%s", vertexIndex));
                    svgBlaze.setSortOrderValue(0);
                    svgBlaze.setFillColor(graph.getBlazeColor(vertexIndex));
                    svgBlaze.setOpacity(graph.getBlazeOpacity());svgBlaze.setDimension(blazeWidth, blazeHeight);
                    svgBlaze.setPosition(edgePosition.getX() , edgePosition.getY() - blazeHeight / 2);
                    SVGObjectConstants.INDICATOR.findIn(svgBlaze).setTransformation(String.format("rotate(%s %s %s)", blazeAngle - 90, 0, 16));
                    output.add(svgBlaze);
                }
                currentStep++;
            });
        } finally {
            graph.terminate();
            complete = true;
        }
    }

    @Override
    public int getTotalSteps() {
        return totalSteps;
    }

    @Override
    public int getCurrentStep() {
        return currentStep;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

}
