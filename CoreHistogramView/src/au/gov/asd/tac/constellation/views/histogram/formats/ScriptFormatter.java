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
package au.gov.asd.tac.constellation.views.histogram.formats;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.ObjectBin;
import java.util.HashMap;
import java.util.Map;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to apply an arbitrary script to transform
 * the natural bin values before they are binned.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class ScriptFormatter extends BinFormatter {

    public static final String SCRIPT_PARAMETER_ID = PluginParameter.buildId(ScriptFormatter.class, "script");

    private static final Map<String, String> LANGUAGES = new HashMap<>();

    static {
        LANGUAGES.put("Python", "jython");
    }

    public ScriptFormatter() {
        super("Format By Script", 10000);
    }

    @Override
    public PluginParameters createParameters() {
        PluginParameters params = new PluginParameters();

        final PluginParameter<StringParameterValue> scriptParameter = StringParameterType.build(SCRIPT_PARAMETER_ID);
        StringParameterType.setLines(scriptParameter, 10);
        scriptParameter.setName("Script");
        scriptParameter.setStringValue("");
        params.addParameter(scriptParameter);

        return params;
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        final String script = parameters.getParameters().get(SCRIPT_PARAMETER_ID).getStringValue();

        ScriptEngineManager manager;
        ScriptEngine engine;
        Bindings bindings = null;
        CompiledScript compiledScript = null;

        try {
            manager = new ScriptEngineManager();
            engine = manager.getEngineByName(LANGUAGES.get("Python"));
            bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            compiledScript = ((Compilable) engine).compile(script);
        } catch (Exception e) {
        }

        return new ScriptFormatBin(bin, bindings, compiledScript);
    }

    private class ScriptFormatBin extends ObjectBin {

        private final Bin bin;
        private final Bindings bindings;
        private final CompiledScript compiledScript;

        public ScriptFormatBin(Bin bin, Bindings bindings, CompiledScript compiledScript) {
            this.bin = bin;
            this.bindings = bindings;
            this.compiledScript = compiledScript;
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            if (compiledScript == null) {
                key = "ERROR";
            } else {
                bin.setKey(graph, attribute, element);
                bin.prepareForPresentation();
                bindings.put("value", bin.getKeyAsObject());
                bindings.put("label", bin.getLabel());
                try {
                    key = compiledScript.eval();
                } catch (ScriptException e) {
                    key = ObjectBin.ERROR_OBJECT;
                }
            }
        }

        @Override
        public Bin create() {
            return new ScriptFormatBin(bin, bindings, compiledScript);
        }
    }
}
