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
package au.gov.asd.tac.constellation.views.scripting;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.views.scripting.graph.SGraph;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.Action;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.python.core.Py;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.jsr223.PyScriptEngine;

/**
 * Execute a Python script.
 * <p>
 * This scripting plugin only supports Python. We used to support other
 * languages but for supportability we will only support Python for now.
 *
 * @author algol
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
@Messages("ScriptingExecutePlugin=Execute Script")
public class ScriptingExecutePlugin extends SimplePlugin {
    
    private static final Logger LOGGER = Logger.getLogger(ScriptingExecutePlugin.class.getName());

    public static final String SCRIPT_PARAMETER_ID = PluginParameter.buildId(ScriptingExecutePlugin.class, "script_text");
    public static final String NEW_OUTPUT_PARAMETER_ID = PluginParameter.buildId(ScriptingExecutePlugin.class, "new_output");
    public static final String GRAPH_NAME_PARAMETER_ID = PluginParameter.buildId(ScriptingExecutePlugin.class, "graph_name");
    public static final String OUTPUT_EXCEPTION_PARAMETER_ID = PluginParameter.buildId(ScriptingExecutePlugin.class, "script_exception");

    private static final ScriptEngineManager FACTORY = new ScriptEngineManager();

    @Override
    protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final Graph graph = graphs.getGraph();
        final String script = parameters.getParameters().get(SCRIPT_PARAMETER_ID).getStringValue();
        final boolean newOutput = parameters.getParameters().get(NEW_OUTPUT_PARAMETER_ID).getBooleanValue();
        final String graphName = parameters.getParameters().get(GRAPH_NAME_PARAMETER_ID).getStringValue();

        // configure the output pane
        final ScriptingInterruptAction interruptor = new ScriptingInterruptAction();
        final Action[] actions = new Action[]{interruptor};
        final InputOutput io = IOProvider.getDefault().getIO(graphName, newOutput, actions, null);
        if (!newOutput) {
            try {
                io.getOut().reset();
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
        io.select();

        // configure scripting engine
        final ScriptEngine engine = FACTORY.getEngineByMimeType(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        if (engine instanceof PyScriptEngine) {
            // Add custom libs to the Jython sys.path.
            final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
            final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
            final File pythonLib = new File(userDir, "PythonLib");
            if (pythonLib.isDirectory()) {
                final PySystemState state = Py.getSystemState();
                state.path.append(new PyString(pythonLib.getPath()));
            }
        }
        engine.getContext().setWriter(new InterruptibleWriter(io.getOut()));
        engine.getContext().setErrorWriter(new InterruptibleWriter(io.getErr()));

        // set scripting engine for graph
        final SGraph sGraph = new SGraph(graph);
        try {
            sGraph.setEngine(engine);

            // add custom objects to scripting engine
            engine.getContext().setAttribute("graph", sGraph, ScriptContext.ENGINE_SCOPE);
            Lookup.getDefault().lookupAll(ScriptingModule.class).iterator().forEachRemaining(module
                    -> engine.getContext().setAttribute(module.getName(), module, ScriptContext.ENGINE_SCOPE));

            try {
                // Jython caches modules - force it to reload all modules in case the user has changed something in their local PythonLib.
                if (engine instanceof PyScriptEngine) {
                    engine.eval("import sys\nsys.modules.clear()", engine.getContext());
                }
                engine.eval(script, engine.getContext());
            } catch (final ScriptException ex) {
                parameters.getParameters().get(OUTPUT_EXCEPTION_PARAMETER_ID).setObjectValue(ex);
                ex.printStackTrace(io.getErr());

                throw new PluginException(PluginNotificationLevel.ERROR, ex);
            }
        } finally {
            // attempt to cleanup locks created by script
            sGraph.cleanup();
        }
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> scriptParam = StringParameterType.build(SCRIPT_PARAMETER_ID);
        scriptParam.setName("Script Text");
        scriptParam.setDescription("The text of the Python script");
        parameters.addParameter(scriptParam);

        final PluginParameter<BooleanParameterValue> showOutputParam = BooleanParameterType.build(NEW_OUTPUT_PARAMETER_ID);
        showOutputParam.setName("New Output");
        showOutputParam.setDescription("Show script output in output window");
        parameters.addParameter(showOutputParam);

        final PluginParameter<StringParameterValue> nameParam = StringParameterType.build(GRAPH_NAME_PARAMETER_ID);
        nameParam.setName("Graph Name");
        nameParam.setDescription("The name of the graph");
        nameParam.setStringValue("");
        parameters.addParameter(nameParam);

        final PluginParameter<ObjectParameterValue> exceptionParam = ObjectParameterType.build(OUTPUT_EXCEPTION_PARAMETER_ID);
        exceptionParam.setName("Script Exception");
        exceptionParam.setDescription("If there is an exception when the script runs, this will hold a reference to the exception object");
        parameters.addParameter(exceptionParam);

        return parameters;
    }

    /**
     * Allow a script to be interrupted when output occurs.
     * <p>
     * A ScriptEngine doesn't offer the ability to interrupt the execution of a
     * script. By intercepting output, we can check for an interrupted thread,
     * which may be better than nothing.
     */
    private static class InterruptibleWriter extends Writer {

        private final OutputWriter w;

        public InterruptibleWriter(final OutputWriter w) {
            this.w = w;
        }

        @Override
        public void write(final char[] cbuf, final int off, final int len) throws IOException {
            w.write(cbuf, off, len);

            if (Thread.interrupted()) {
                throw new IOException("Script interrupted by user");
            }
            try {
                Thread.sleep(1);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("Script interrupted by user");
            }
        }

        @Override
        public void flush() throws IOException {
            w.flush();
        }

        @Override
        public void close() throws IOException {
            w.close();
        }
    }
}
