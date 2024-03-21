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
package au.gov.asd.tac.constellation.graph.interaction.animation;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Case the node radius to throb larger and smaller than the actual node radius. 
 * This Animation is infinite so will not animate when animations are disabled.
 * 
 * @author twilight_sparkle
 * @author capricornunicorn123
 */
public class ThrobbingNodeAnimation extends Animation {
    
    public static String NAME = "Throbbing Node Animation";
    private static final float LOWER_LIMIT = 0.5F;
    private static final float UPPER_LIMIT = 1.5F;
    
    private int nodeRadiusAttribute;
    private float currentDirection = 0.05F;
    private float currentRadius = 1F;
    
    final Map<Integer, Float> originalNodeRadii = new HashMap<>();

    @Override
    public void initialise(final GraphWriteMethods wg) {
        nodeRadiusAttribute = VisualConcept.VertexAttribute.NODE_RADIUS.get(wg);
        // dont initialise if there is 0 nodes present
        if (wg.getVertexCount() == 0) {
            stop();
        } else {
            for (int pos = 0; pos < wg.getVertexCount(); pos++) {
                registerNode(wg.getVertex(pos), wg);
            }
        }
    }

    @Override
    public List<ThreeTuple<Integer, Integer, Object>> animate(final GraphReadMethods wg) {
        List<ThreeTuple<Integer, Integer, Object>> graphWrites = new ArrayList<>();
        // if there is at least 1 node on the graph
        if (wg.getVertexCount() > 0) {

            if (currentRadius > UPPER_LIMIT || currentRadius < LOWER_LIMIT) {
                currentDirection = -currentDirection;
            }
            
            currentRadius += currentDirection;
            for (int pos = 0; pos < wg.getVertexCount(); pos++) {
                final int vxId = wg.getVertex(pos);
                
                // If a node is added during animation its original radius needs to be captured. 
                if (originalNodeRadii.get(vxId) == null){
                    registerNode(vxId, wg);
                }
                
                graphWrites.add(new ThreeTuple<>(nodeRadiusAttribute, vxId, currentRadius * originalNodeRadii.get(vxId)));
            }
            return graphWrites;
        }
        return null;
    }

    @Override
    public void reset(final GraphWriteMethods wg) {
        originalNodeRadii.forEach((vxId, radius) -> wg.setObjectValue(nodeRadiusAttribute, vxId, radius));
    }

    @Override
    public long getIntervalInMillis() {
        return 35;
    }

    @Override
    protected String getName() {
        return NAME;
    }
    
    @Override
    public void setFinalFrame(final GraphWriteMethods wg){
        //Do Nothing
    }

    private void registerNode(final int vxId, final GraphReadMethods wg) {
        originalNodeRadii.put(vxId, wg.getFloatValue(nodeRadiusAttribute, vxId));
    }

}
