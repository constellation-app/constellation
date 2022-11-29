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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;

/**
 * Allows validation of a RecordStore at particular execution points of a
 * RecordStoreQueryPlugin.
 * <p>
 * Each method runs at a specified execution point. If the data in the
 * RecordStore is not valid, throw a {@link PluginException}. Of course, since
 * the RecordStore is writable, a RecordStoreValidator can also modify the
 * RecordStore as it sees fit.
 * <p>
 * No assumptions are made about the position of the record pointer before or
 * after the validator methods run.
 *
 * @author algol
 */
public abstract class RecordStoreValidator {

    /**
     * Validate a RecordStore before the plugin query point.
     * <p>
     *
     * @param plugin The plugin being run.
     * @param rs The RecordStore containing data loaded from the graph.
     * @param interaction User interaction.
     * @param parameters Plugin parameters.
     *
     * @throws PluginException if an anticipated exception occurs during plugin
     * execution.
     */
    public void validatePreQuery(final RecordStoreQueryPlugin plugin, final RecordStore rs, final PluginInteraction interaction, final PluginParameters parameters) throws PluginException {
    }

    /**
     * Validate a RecordStore after the plugin query point.
     * <p>
     *
     * @param plugin The plugin being run.
     * @param rs The RecordStore containing new data from the plugin.
     * @param interaction User interaction.
     * @param parameters Plugin parameters.
     *
     * @throws PluginException if an anticipated exception occurs during plugin
     * execution.
     */
    public void validatePostQuery(final RecordStoreQueryPlugin plugin, final RecordStore rs, final PluginInteraction interaction, final PluginParameters parameters) throws PluginException {
    }

    /**
     * Validate a RecordStore before the plugin edit point.
     * <p>
     *
     * @param plugin The plugin being run.
     * @param rs The RecordStore containing data about to be written to the
     * graph.
     * @param wg The graph being edited.
     * @param interaction User interaction.
     * @param parameters Plugin parameters.
     *
     * @throws PluginException if an anticipated exception occurs during plugin
     * execution.
     */
    public void validatePreEdit(final RecordStoreQueryPlugin plugin, final RecordStore rs, final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws PluginException {
    }
}
