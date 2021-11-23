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
package au.gov.asd.tac.constellation.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * A basic implementation of a plugin designed to be the superclass of almost
 * all plugin implementations. However, in most cases it will not be necessary
 * to directly extend this class but rather extend one of several template
 * implementations designed to handle the common plugin types in Constellation.
 *
 * @author sirius
 */
public abstract class AbstractPlugin implements Plugin {

    private final String pluginName;

    protected AbstractPlugin() {
        this.pluginName = null;
    }

    protected AbstractPlugin(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * The name of the plugin.
     * <p>
     * It is easy to forget to provide this, either by adding an @Messages
     * annotation or (especially when writing an anonymous subclass) overriding
     * getName(). Because the exception can easily be lost amongst the threads
     * used by the plugin environment, this can lead to a lot of head
     * scratching.
     * <p>
     * Therefore, if the bundle can't be found or getName() hasn't been
     * overridden, an in-your-face dialog box is displayed using @{see
     * Exceptions.printStackTrace()}. Once the developer who is adding the new
     * plugin fixes the problem, it won't be seen by users.
     *
     * @return The name of the plugin.
     */
    @Override
    public String getName() {
        // If a plugin name has been specified programmatically then return it
        if (pluginName != null) {
            return pluginName;
        }

        // Otherwise attempt to look up a name from the bundle
        try {
            return NbBundle.getMessage(getClass(), getClass().getSimpleName());
        } catch (final MissingResourceException ex) {
            // A common problem when implementing anonymous plugin classes is to forget to implement getName().
            // Since anonymous subclasses don't have an @Messages() annotation the exception is thrown,
            // and it can be difficult to figure out what's going on, what with the Future instances and threads and all.
            // This attempts to make it obvious (and also helps when the @Message() annotation has been forgotten).
            final String msg = String.format("Have you added @Messages() or overridden getName() for class '%s'?", this.getClass().getName());
            final Exception iae = new IllegalArgumentException(msg, ex);
            Exceptions.printStackTrace(iae);

            // Throw the original exception as if we hadn't intercepted it.
            throw ex;
        }
    }

    @Override
    public String getDescription() {
        final String helpFileResource = getClass().getSimpleName() + ".html";
        final URL helpURL = getClass().getResource(helpFileResource);
        if (helpURL != null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(helpURL.openStream(), StandardCharsets.UTF_8.name()))) {
                final StringBuilder out = new StringBuilder();
                final char[] buffer = new char[1024];
                int read = in.read(buffer);
                while (read != -1) {
                    out.append(buffer, 0, read);
                    read = in.read(buffer);
                }
                return out.toString();
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    private static final String[] DEFAULT_TAGS = new String[]{PluginTags.GENERAL};

    @Override
    public String[] getTags() {
        Class<?> pluginClass = getClass();
        while (true) {
            final PluginInfo info = pluginClass.getAnnotation(PluginInfo.class);
            if (info != null) {
                return info.tags();
            }
            pluginClass = pluginClass.getSuperclass();
            if (pluginClass == null || pluginClass == Object.class) {
                return DEFAULT_TAGS;
            }
        }
    }

    /**
     * Get the HelpCtx associated with this plugin.
     * <p>
     * This implementation looks for a help id with the same name as the fully
     * qualified class name of the plugin. If that help id exists, a HelpCtx()
     * will be returned. Otherwise, null will be returned.
     *
     * @return A HelpCtx() if help for this plugin has been implemented, null
     * otherwise.
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    @Override
    public PluginParameters createParameters() {
        return null;
    }

    @Override
    public void updateParameters(Graph graph, PluginParameters parameters) {
    }
}
