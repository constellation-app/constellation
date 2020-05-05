/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.delimited.translator;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
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
 * The ScripAttributeTranslator translates an input field value by running a
 * specified script on the input value and replacing it with the output of the
 * script.
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeTranslator.class)
public class ScriptAttributeTranslator extends AttributeTranslator {

    private static final String PYTHON_LANGUAGE = "Python";
    public static final String SCRIPT_PARAMETER_ID = PluginParameter.buildId(ScriptAttributeTranslator.class, "script");

    private static final Map<String, String> LANGUAGES = new HashMap<>();

    static {
        LANGUAGES.put(PYTHON_LANGUAGE, "jython");
    }

    private String savedLanguage = null;
    private String savedScript = null;

    private Bindings bindings = null;
    private CompiledScript compiledScript = null;

    public ScriptAttributeTranslator() {
        super("Script", 1000);
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<StringParameterValue> scriptParameter = StringParameterType.build(SCRIPT_PARAMETER_ID);
        StringParameterType.setLines(scriptParameter, 10);
        scriptParameter.setName("Script");
        scriptParameter.setDescription("Modify the 'value' variable to implement your formatting");
        scriptParameter.setStringValue("value = value + \"2\"");
        params.addParameter(scriptParameter);

        return params;
    }

    @Override
    public String translate(final String value, final PluginParameters parameters) {

        // Get the reqiested language and scripte
        String script = parameters.getParameters().get(SCRIPT_PARAMETER_ID).getStringValue();

        // If the language and script do not match the compiled script then recompile
        if (!PYTHON_LANGUAGE.equals(savedLanguage) || !script.equals(savedScript)) {
            try {
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName(LANGUAGES.get(PYTHON_LANGUAGE));
                bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                compiledScript = ((Compilable) engine).compile(script);
                savedLanguage = PYTHON_LANGUAGE;
                savedScript = script;
            } catch (ScriptException e) {
                bindings = null;
                compiledScript = null;
                savedLanguage = null;
                savedScript = null;
                return "ERROR: " + e.getMessage();
            }
        }

        // Return the value returned from the script
        try {
            bindings.put("value", value);
            Object result = compiledScript.eval();
            if (result == null) {
                result = bindings.get("value");
            }
            return String.valueOf(result);
        } catch (ScriptException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String getParameterValues(final PluginParameters parameters) {
        String script = parameters.getParameters().get(SCRIPT_PARAMETER_ID).getStringValue();
        return PYTHON_LANGUAGE + "\t" + script;
    }

    @Override
    public void setParameterValues(final PluginParameters parameters, final String values) {
        final int scriptIndex = values.indexOf('\t');
        final String script = values.substring(scriptIndex + 1);
        parameters.getParameters().get(SCRIPT_PARAMETER_ID).setStringValue(script);
    }
}
