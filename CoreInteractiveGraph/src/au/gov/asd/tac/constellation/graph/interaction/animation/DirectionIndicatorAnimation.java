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
package au.gov.asd.tac.constellation.graph.interaction.animation;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;

/**
 * Cause the graph to animate all connections to indicate their direction.
 * This animation is an infinite animation and will not update the graph
 * when animations are disabled.
 *
 * @author capricornunicorn123
 */
public final class DirectionIndicatorAnimation extends Animation { 
    
    public static final String NAME = "Direction Indicators Animation";
    
    private int motionAtt;
    
    @Override
    public void initialise(final GraphWriteMethods wg) {
        motionAtt = VisualConcept.MetaAttribute.CONNECTION_MOTION.ensure(wg);
        // Don't initilise the animation if there are no transactions
        if (wg.getTransactionCount() < 1) {
            stop();
        }
    }

    @Override
    public void animate(final GraphWriteMethods wg) {
        
        // Don't animate unless there are transactions
        if (wg.getTransactionCount() > 0) {
            final float current_value = wg.getFloatValue(motionAtt, 0);
            float floatValue = current_value > Float.MAX_VALUE ? -1.0F : current_value + 0.5F;
            wg.setFloatValue(motionAtt, 0, floatValue);
        }
    }

    @Override
    public void reset(final GraphWriteMethods wg) {
        wg.setFloatValue(motionAtt, 0, VisualGraphDefaults.DEFAULT_CONNECTION_MOTION);
    }

    @Override
    public long getIntervalInMillis() {
        return 60;
    }
    
    @Override
    protected String getName() {
        return NAME;
    }

    @Override
    public void setFinalFrame(final GraphWriteMethods wg){
        //Do Nothing
    }
}
