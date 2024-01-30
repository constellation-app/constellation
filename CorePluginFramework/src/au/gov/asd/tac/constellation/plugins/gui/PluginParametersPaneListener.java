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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;

/**
 * An interface for listening to changes in the validity of the parameter values
 * that are being set in one or more {@link PluginParametersPane}. Often these
 * listeners are organised in a hierarchical structure where child listeners
 * inform parents of changes. Typically this pattern is used to allow views to
 * enable and disable plugins based on the current validity of their parameters
 * as set through their corresponding panes.
 *
 * @see PluginParametersPane
 * @author ruby_crucis
 */
public interface PluginParametersPaneListener {

    /**
     * Called when the object that is being listened to has changes its
     * validity.
     *
     * @param valid Whether or not the objec
     */
    public void validityChanged(boolean valid);

    /**
     * Called to indicate that a change registered by a given listener means an
     * update throughout its hierarchy of listeners is required. This is used by
     * the data access view as changes to the validity of a given plugin's
     * parameters have ramifications for the entire view - whenever all selected
     * plugins are in a valid state, the data access view's go button becomes
     * active.
     */
    public void hierarchicalUpdate();

    /**
     * Called to notify listeners that a parameter being listened too has changed its validity. 
     * Can be triggered by a missing required parameter or by a parameter having a value that is invalid.
     * @param parameter
     * @param currentlySatisfied 
     */
    public void notifyParameterValidityChange(final PluginParameter<?> parameter, final boolean currentlySatisfied);
     
}
