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
package au.gov.asd.tac.constellation.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.openide.util.HelpCtx;

/**
 * The Plugin interface represents the simplest requirements for a plugin in
 * Constellation. However, it will almost never be necessary to implement this
 * interface directly and build a plugin from scratch. In most cases, it will be
 * better to extend one of the several template implementations that are
 * designed to handle the common use cases for plug-ins with in Constellation.
 *
 * @author sirius
 */
public interface Plugin {

    /**
     * Returns the name of this plugin.
     * <p>
     * Subclasses will typically override this to retrieve the name from the
     * plugin's associated Bundle using the classname as the key.
     *
     * <pre><code>
     * &#64;NbBundle.getMessage(getClass(), getClass().getSimpleName())
     * </code></pre>
     *
     * The easiest way for Plugin implementors to do this for a class
     * <tt>MyPlugin</tt> is to add a <tt>NbBundle.Messages</tt>
     * annotation to the class.
     *
     * <pre><code>
     * &#64;NbBundle.Messages("MyPlugin=My lovely plugin")
     * </code></pre>
     *
     * @return The name of this plugin.
     */
    public String getName();

    /**
     * Returns a description of this plugin
     *
     * This is typically used by the user interface to provide more information
     * about what a plugin does.
     *
     * @return A String describing this plugin
     */
    public String getDescription();

    /**
     * Return a list of tags for this plugin.
     *
     * Tags are used through out Constellation to categorise and filter plugins.
     *
     * @return a list of tags for this plugin.
     */
    public String[] getTags();

    /**
     * Creates the parameters for this plugin.
     * <p>
     * This method will only be called once and the results will be cached for
     * future use. New executions of the plugin will use copies of the
     * PluginParameters created here. This ensures that the parameter definition
     * of a plugin does not change during the running of Constellation.
     *
     * @return The PluginParameters used by this plugin.
     */
    public PluginParameters createParameters();

    /**
     * Updates the plugins given the state of a graph.
     *
     * Often it is useful to have elements of a plugin's parameters reflect the
     * graph upon which the plug-in will be run. Examples of this include the
     * available choices in a drop down list or the default values in a text
     * field. While the createParameters() method is called only once to create
     * the parameters, this method is called each time a plugin is run on a
     * graph allowing the plugin a chance to customise its parameters for that
     * particular graph.
     *
     * @param graph the graph that this plugin will be run on.
     * @param parameters the parameters for the plugin
     */
    public void updateParameters(final Graph graph, final PluginParameters parameters);

    /**
     * Called to run the plugin.
     * <p>
     * This method is generally called by the framework in response to the user
     * selecting a menu item, for instance. All changes to
     * {@link PluginParameter} will fire an event. To suppress the event, call
     * {@link PluginParameter.suppressEvent(...)} method. Cases such as
     * pre-configuration of plugins which need the currently opened graph to
     * define runtime activity and available menu items may find this method
     * useful.
     *
     * @param graphs an object holding all information about the graphs in the
     * system.
     * @param interaction an object allowing the plugin to access all user
     * interaction.
     * @param parameters the parameters for this execution of the plugin.
     *
     * @throws InterruptedException if the plugin has been canceled or
     * unexpectedly ceased for any reason.
     * @throws PluginException if an anticipated exception occurs during plugin
     * execution.
     */
    public void run(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException;

    /**
     * Called to run the plugin.
     * <p>
     * This method is generally called by the framework in response to the user
     * selecting a menu item, for instance.
     *
     * @param graph an GraphWriteMethods object representing the graph to be
     * operated on.
     * @param interaction an object allowing the plugin to access all user
     * interaction.
     * @param parameters the parameters for this execution of the plugin.
     *
     * @throws InterruptedException if the plugin has been canceled or
     * unexpectedly ceased for any reason.
     * @throws PluginException if an anticipated exception occurs during plugin
     * execution.
     */
    public void run(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException;

    /**
     * Called to run the plugin.
     * <p>
     * This method is generally called by the framework in response to the user
     * selecting a menu item, for instance.
     *
     * @param graph an GraphReadMethods object representing the graph to be
     * operated on.
     * @param interaction an object allowing the plugin to access all user
     * interaction.
     * @param parameters the parameters for this execution of the plugin.
     *
     * @throws InterruptedException if the plugin has been cancelled or
     * unexpectedly ceased for any reason.
     * @throws PluginException if an anticipated exception occurs during plugin
     * execution.
     */
    public void run(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException;

    /**
     * Get the HelpCtx associated with this plugin.
     *
     * @return A HelpCtx if help exists, otherwise null.
     */
    public HelpCtx getHelpCtx();

    public final String ID = UUID.randomUUID().toString();

    /**
     * Returns a unique id for this plugin.
     *
     * @return a unique id for this plugin.
     */
    public default String getId() {
        return ID;
    }

    /**
     * Should this plugin replace another plugin?
     * <p>
     * If the name of a data access plugin is returned, then the
     * {@link PluginRegistry} will ensure that that plugin is not loaded.
     *
     * @return The name of the plugin this plugin is intended to replace in the
     * , otherwise an empty list. Note that the name of the plugin should be the
     * full package and class name like the following to be consistent with the
     * {@code PluginRegistry}.
     *
     * <pre><code>
     * plugin.getClass().getName();
     * </code></pre>
     */
    public default List<String> getOverriddenPlugins() {
        return Collections.emptyList();
    }

}
