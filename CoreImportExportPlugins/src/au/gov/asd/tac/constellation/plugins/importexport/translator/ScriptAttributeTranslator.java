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
package au.gov.asd.tac.constellation.plugins.importexport.translator;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.openide.NotifyDescriptor;
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
    
    private static final Logger LOGGER = Logger.getLogger(ScriptAttributeTranslator.class.getName());

    private static final String PYTHON_LANGUAGE = "Python";
    public static final String SCRIPT_PARAMETER_ID = PluginParameter.buildId(ScriptAttributeTranslator.class, "script");
    private static final String DEFAULT_SCRIPT = "value = value + \"2\"";
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
        scriptParameter.setStringValue(DEFAULT_SCRIPT);
        params.addParameter(scriptParameter);

        return params;
    }

    @Override
    public String translate(final String value, final PluginParameters parameters) {

        // Get the reqiested language and scripte
        final String script = parameters.getParameters().get(SCRIPT_PARAMETER_ID).getStringValue();

        // If the language and script do not match the compiled script then recompile
        if (!PYTHON_LANGUAGE.equals(savedLanguage) || !script.equals(savedScript)) {
            try {
                final ScriptEngineManager manager = new ScriptEngineManager();
                final ScriptEngine engine = manager.getEngineByName(LANGUAGES.get(PYTHON_LANGUAGE));
                bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                compiledScript = ((Compilable) engine).compile(script);
                savedLanguage = PYTHON_LANGUAGE;
                savedScript = script;
            } catch (final ScriptException ex) {
                bindings = null;
                compiledScript = null;
                savedLanguage = null;
                savedScript = null;
                parameters.getParameters().get(SCRIPT_PARAMETER_ID).setStringValue(DEFAULT_SCRIPT);
                final Throwable scrEx = new ScriptException(NotifyDisplayer.BLOCK_POPUP_FLAG + ex.getLocalizedMessage());
                scrEx.setStackTrace(ex.getStackTrace());
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), scrEx);
                NotifyDisplayer.display(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                return value;
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
        } catch (final ScriptException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String getParameterValues(final PluginParameters parameters) {
        final String script = parameters.getParameters().get(SCRIPT_PARAMETER_ID).getStringValue();
        return PYTHON_LANGUAGE + SeparatorConstants.TAB + script;
    }

    @Override
    public void setParameterValues(final PluginParameters parameters, final String values) {
        final int scriptIndex = values.indexOf('\t');
        final String script = values.substring(scriptIndex + 1);
        parameters.getParameters().get(SCRIPT_PARAMETER_ID).setStringValue(script);
    }
}
