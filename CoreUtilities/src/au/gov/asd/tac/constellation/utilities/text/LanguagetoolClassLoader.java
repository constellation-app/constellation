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
package au.gov.asd.tac.constellation.utilities.text;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Auriga2
 */

public class LanguagetoolClassLoader {
    private static URLClassLoader classLoader;
    private static LanguagetoolClassLoader languagetoolClassLoader;
    private static Class<?> jLanguagetool;
    private static Class<?> language;
    private static Class<?> languages;
    private static Class<?> multiThreadedJLanguageTool;
    private static Class<?> ruleMatch;
    private static Class<?> rule;
    private static Class<?> spellingCheckRule;
    private static final Logger LOGGER = Logger.getLogger(LanguagetoolClassLoader.class.getName());

    private LanguagetoolClassLoader(final List<URL> urls) {
        classLoader = new URLClassLoader(urls.toArray(URL[]::new));
    }

    public static synchronized void loadDependencies() {
        if (languagetoolClassLoader == null) {
            try {
                final List<URL> urls = LanguageToolDependencyUrlLoader.loadUrls();
                languagetoolClassLoader = new LanguagetoolClassLoader(urls);
                LoadOtherClasses();
            } catch (final MalformedURLException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "An error occured loading LanguageTool Classes", ex);
            }
        }
    }

    private static void LoadOtherClasses() throws ClassNotFoundException {
        // Load JLanguagetool
        jLanguagetool = classLoader.loadClass("org.languagetool.JLanguageTool");

        // Load other classes
        language = classLoader.loadClass("org.languagetool.Language");
        languages = classLoader.loadClass("org.languagetool.Languages");
        multiThreadedJLanguageTool = classLoader.loadClass("org.languagetool.MultiThreadedJLanguageTool");
        ruleMatch = classLoader.loadClass("org.languagetool.rules.RuleMatch");
        rule = classLoader.loadClass("org.languagetool.rules.Rule");
        spellingCheckRule = classLoader.loadClass("org.languagetool.rules.spelling.SpellingCheckRule");
    }
        
    public static LanguagetoolClassLoader getLanguagetoolClassLoader() {
        return languagetoolClassLoader;
    }

    public static Class<?> getJLanguagetool() {
        return jLanguagetool;
    }

    public static Class<?> getLanguage() {
        return language;
    }

    public static Class<?> getLanguages() {
        return languages;
    }

    public static Class<?> getMultiThreadedJLanguageTool() {
        return multiThreadedJLanguageTool;
    }

    public static Class<?> getRuleMatch() {
        return ruleMatch;
    }

    public static Class<?> getRule() {
        return rule;
    }

    public static Class<?> getSpellingCheckRule() {
        return spellingCheckRule;
    }
}
