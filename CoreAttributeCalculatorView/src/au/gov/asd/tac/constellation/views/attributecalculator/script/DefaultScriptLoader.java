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
package au.gov.asd.tac.constellation.views.attributecalculator.script;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.Exceptions;

/**
 *
 * @author twilight_sparkle
 */
public class DefaultScriptLoader extends AbstractScriptLoader {

    private static final String DESCRIPTION_KEY = "description";

    @Override
    public Map<String, String[]> getScripts() {

        final ObjectMapper mapper = new ObjectMapper();
        Map<String, String[]> namesToDescriptions = new HashMap<>();

        final List<String> defaultNamesList = new ArrayList<>();
        String[] defaultNames = new String[0];
        try {
            InputStream is = ScriptIO.class.getResourceAsStream("resources/DefaultScriptList.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8.name()))) {
                while (true) {
                    String line;
                    try {
                        line = reader.readLine();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        line = null;
                    }
                    if (line == null) {
                        break;
                    }
                    defaultNamesList.add(line);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        defaultNames = defaultNamesList.toArray(defaultNames);

        for (String name : defaultNames) {
            final String visibleName = ScriptIO.decode(name.substring(0, name.length() - 5));
            JsonNode node;
            final String fileName;
            try {
                final BufferedReader r = new BufferedReader(new InputStreamReader(ScriptIO.class.getResourceAsStream("resources/" + name), StandardCharsets.UTF_8.name()));
                final File tempFile = File.createTempFile("_inbuilt_script", ".json");
                try (FileWriter w = new FileWriter(tempFile)) {
                    while (true) {
                        final String line = r.readLine();
                        if (line == null) {
                            break;
                        }
                        w.append(line + "\n");
                    }
                }
                fileName = tempFile.getPath();
                node = mapper.readTree(tempFile);
            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
                continue;
            }
            final String description = node.get(DESCRIPTION_KEY).textValue();
            namesToDescriptions.put(visibleName, new String[]{fileName, description});
        }

        return namesToDescriptions;
    }

}
