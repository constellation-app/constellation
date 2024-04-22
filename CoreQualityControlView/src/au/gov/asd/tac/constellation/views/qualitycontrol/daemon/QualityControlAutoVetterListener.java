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
package au.gov.asd.tac.constellation.views.qualitycontrol.daemon;

/**
 * Listens to changes to the Quality Control View.
 *
 * @author aldebaran30701
 */
public interface QualityControlAutoVetterListener {

    /**
     * Called when the quality control has run a rule.
     * <p>
     * This can happen when a graph changes.
     *
     * @param canRun The value representing if the DAV can run.
     */
    public void qualityControlRuleChanged(final boolean canRun);
}
