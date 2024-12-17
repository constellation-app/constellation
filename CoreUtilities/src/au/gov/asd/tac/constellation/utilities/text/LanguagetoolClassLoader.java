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
package au.gov.asd.tac.constellation.utilities.text;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Auriga2
 */

public class LanguagetoolClassLoader {
    private static URLClassLoader classLoader;
    public static LanguagetoolClassLoader LanguagetoolClassLoader;
    public static Class<?> JLanguagetool;
    public static Class<?> Language;
    public static Class<?> Languages;
    public static Class<?> MultiThreadedJLanguageTool;
    public static Class<?> RuleMatch;
    public static Class<?> Rule;
    public static Class<?> SpellingCheckRule;
    private static final Logger LOGGER = Logger.getLogger(LanguagetoolClassLoader.class.getName());

    private LanguagetoolClassLoader(URL[] urls) {
        classLoader = new URLClassLoader(urls);
    }

    public static synchronized void loadDependencies() {
        if (LanguagetoolClassLoader == null) {
            try {
                URL[] urls = LanguageToolDependencyUrlLoader.loadUrls();
                LanguagetoolClassLoader = new LanguagetoolClassLoader(urls);
                LoadOtherClasses();
            } catch (MalformedURLException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "An error occured loading LanguageTool Classes", ex);
            }
        }
    }

    private static void LoadOtherClasses() throws ClassNotFoundException {
        // Load JLanguagetool
        JLanguagetool = classLoader.loadClass("org.languagetool.JLanguageTool");

        // Load other classes
        Language = classLoader.loadClass("org.languagetool.Language");
        Languages = classLoader.loadClass("org.languagetool.Languages");
        MultiThreadedJLanguageTool = classLoader.loadClass("org.languagetool.MultiThreadedJLanguageTool");
        RuleMatch = classLoader.loadClass("org.languagetool.rules.RuleMatch");
        Rule = classLoader.loadClass("org.languagetool.rules.Rule");
        SpellingCheckRule = classLoader.loadClass("org.languagetool.rules.spelling.SpellingCheckRule");
    }
}
