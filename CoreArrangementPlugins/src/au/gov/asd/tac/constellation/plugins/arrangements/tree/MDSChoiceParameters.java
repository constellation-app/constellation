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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;

/**
 * A convenient holder for the parameters of an MDS arrangement.
 *
 * @author algol
 */
public class MDSChoiceParameters {

    final LinkWeight linkWeight;
    final int weightAttributeId;
    final float scale;
    final int iterationsPerStageTrial;
    final int maxTrialsPerStage;
    final int minTrialsPerStage;
    final boolean tryToAvoidOverlap;
    final int overlapAvoidance;

    public MDSChoiceParameters(final LinkWeight linkWeight, final int weightAttributeId, final float scale, final int iterationsPerStageTrial, final int maxTrialsPerStage, final int minTrialsPerStage, final boolean tryToAvoidOverlap, final int overlapAvoidance) {
        this.linkWeight = linkWeight;
        this.weightAttributeId = weightAttributeId;
        this.scale = scale;
        this.iterationsPerStageTrial = iterationsPerStageTrial;
        this.maxTrialsPerStage = maxTrialsPerStage;
        this.minTrialsPerStage = minTrialsPerStage;
        this.tryToAvoidOverlap = tryToAvoidOverlap;
        this.overlapAvoidance = overlapAvoidance;
    }

    public static MDSChoiceParameters getDefaultParameters() {
        return new MDSChoiceParameters(LinkWeight.USE_EXTENTS, Graph.NOT_FOUND, 1, 20, 8, 1, true, 100);
    }
}
