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
package au.gov.asd.tac.constellation.graph.interaction.animation;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.utilities.visual.VisualChangeBuilder;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author twilight_sparkle
 */
public class ThrobbingNodeAnimation extends Animation {

    private int nodeRadiusAttribute;
    private final float lowerLimit = 1;
    private final float upperLimit = 4;
    private float currentDirection = 0.1f;
    private float currentRadius = 1f;
    private final long throbbingNodeAnimationId = VisualChangeBuilder.generateNewId();

    final Map<Integer, Float> originalNodeRadii = new HashMap<>();

    @Override
    public void initialise(GraphWriteMethods wg) {
        nodeRadiusAttribute = VisualConcept.VertexAttribute.NODE_RADIUS.get(wg);
        for (int pos = 0; pos < wg.getVertexCount(); pos++) {
            final int vxId = wg.getVertex(pos);
            originalNodeRadii.put(vxId, wg.getFloatValue(nodeRadiusAttribute, vxId));
        }
    }

    @Override
    public List<VisualChange> animate(GraphWriteMethods wg) {
        if (currentRadius > upperLimit || currentRadius < lowerLimit) {
            currentDirection = -currentDirection;
        }
        currentRadius += currentDirection;
        for (int pos = 0; pos < wg.getVertexCount(); pos++) {
            final int vxId = wg.getVertex(pos);
            wg.setFloatValue(nodeRadiusAttribute, vxId, currentRadius);
        }
        return Arrays.asList(new VisualChangeBuilder(VisualProperty.VERTEX_RADIUS).forItems(wg.getVertexCount()).withId(throbbingNodeAnimationId).build());
    }

    @Override
    public void reset(GraphWriteMethods wg) {
        originalNodeRadii.forEach((vxId, radius) -> {
            wg.setObjectValue(nodeRadiusAttribute, vxId, radius);
        });
    }

    @Override
    public long getIntervalInMillis() {
        return 10;
    }

    @Override
    protected String getName() {
        return "Throbbing Node Animation";
    }

}
