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
package au.gov.asd.tac.constellation.views.wordcloud.phraseanalysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author twilight_sparkle
 */
public class PhraseAnalysisModelLoader {

    private static final Map<String, Set<String>> excludedWords = new HashMap<>();
    private static final Map<String, Set<String>> delimiters = new HashMap<>();

    private static final String COMMENT_TOKEN = "##";
    private static final String SET_TOKEN = "#{";
    private static final String OPEN_SET = "{";
    private static final String CLOSE_SET = "}";

    private static boolean isLoaded = false;
    
    private PhraseAnalysisModelLoader() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, Set<String>> getExcludedWords() {
        return Collections.unmodifiableMap(excludedWords);
    }

    public static Map<String, Set<String>> getDelimiters() {
        return Collections.unmodifiableMap(delimiters);
    }
    
    public static void loadMap() throws IOException {

        if (isLoaded) {
            return;
        }

        final BufferedReader excludedWordsReader = new BufferedReader(new InputStreamReader(PhraseAnalysisModelLoader.class.getResourceAsStream("ExcludedWords.u8"), StandardCharsets.UTF_8.name()));
        processLines(excludedWordsReader, excludedWords, false);

        final BufferedReader delimitersReader = new BufferedReader(new InputStreamReader(PhraseAnalysisModelLoader.class.getResourceAsStream("Delimiters.u8"), StandardCharsets.UTF_8.name()));
        processLines(delimitersReader, delimiters, true);

        isLoaded = true;
    }

    private enum Mode {
        ADD_CHARACTERS, ADD_SET_OF_SETS, LOOK_FOR_MODE_TOKEN
    }

    private static void processLines(final BufferedReader reader, final Map<String, Set<String>> map, final boolean singleCharacterLines) throws IOException {

        Mode currentMode = Mode.LOOK_FOR_MODE_TOKEN;
        Set<String> currentSet = new HashSet<>();

        while (true) {
            String line = reader.readLine();
            if (line == null) {
                return;
            } else if (line.isBlank()) {
                currentMode = Mode.LOOK_FOR_MODE_TOKEN;
                continue;
            }

            switch (currentMode) {
                case ADD_SET_OF_SETS -> {
                    line = line.substring(line.indexOf(OPEN_SET) + 1, line.indexOf(CLOSE_SET));
                    currentSet.addAll(map.get(line));
                }
                case ADD_CHARACTERS -> 
                    currentSet.add(singleCharacterLines ? line.substring(0, 1) : line);
                case LOOK_FOR_MODE_TOKEN -> {
                    final String token_type = line.substring(0, 2);
                    if (!token_type.equals(COMMENT_TOKEN)) {
                        currentSet = new HashSet<>();
                        final String currentString = line.substring(line.indexOf(OPEN_SET) + 1, line.indexOf(CLOSE_SET));
                        currentMode = token_type.equals(SET_TOKEN) ? Mode.ADD_CHARACTERS : Mode.ADD_SET_OF_SETS;
                        map.put(currentString, currentSet);
                    }
                }
            }
        }
    }
}
