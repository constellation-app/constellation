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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

/**
 * A convenient holder for the parameters of an MDS arrangement.
 *
 * @author algol
 */
public class MDSChoiceParameters {

    private final LinkWeight linkWeight;
    private final float scale;
    private final int iterationsPerStageTrial;
    private final int maxTrialsPerStage;
    private final int minTrialsPerStage;
    private final boolean tryToAvoidOverlap;
    private final int overlapAvoidance;

    public MDSChoiceParameters(final LinkWeight linkWeight, final float scale, final int iterationsPerStageTrial, final int maxTrialsPerStage, final int minTrialsPerStage, final boolean tryToAvoidOverlap, final int overlapAvoidance) {
        this.linkWeight = linkWeight;
        this.scale = scale;
        this.iterationsPerStageTrial = iterationsPerStageTrial;
        this.maxTrialsPerStage = maxTrialsPerStage;
        this.minTrialsPerStage = minTrialsPerStage;
        this.tryToAvoidOverlap = tryToAvoidOverlap;
        this.overlapAvoidance = overlapAvoidance;
    }

    public LinkWeight getLinkWeight() {
        return linkWeight;
    }

    public float getScale() {
        return scale;
    }

    public int getIterationsPerStageTrial() {
        return iterationsPerStageTrial;
    }

    public int getMaxTrialsPerStage() {
        return maxTrialsPerStage;
    }

    public int getMinTrialsPerStage() {
        return minTrialsPerStage;
    }

    public boolean isTryToAvoidOverlap() {
        return tryToAvoidOverlap;
    }

    public int getOverlapAvoidance() {
        return overlapAvoidance;
    }
    
    public static MDSChoiceParameters getDefaultParameters() {
        return new MDSChoiceParameters(LinkWeight.USE_EXTENTS, 1, 20, 8, 1, true, 100);
    }
}
