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
package au.gov.asd.tac.constellation.help;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates the file structure of the help docs
 *
 * @author Delphinus8821
 */
public class FileStructureBuilder {

    public final Logger LOGGER = Logger.getLogger(FileStructureBuilder.class.getName());

    public void run() {
        try {

            // First layer of the file directory
            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> map = mapper.readValue(Paths.get("src/au/gov/asd/tac/constellation/help/json-input.json").toFile(), Map.class);

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                LOGGER.log(Level.INFO, entry.getKey() + " = " + entry.getValue());
                String directoryName = "src/au/gov/asd/tac/constellation/help/docs/" + entry.getKey();
                File directory = new File(String.valueOf(directoryName));

                if (!directory.exists()) {
                    directory.mkdir();
                }

                // Second layer of the file directory
                String newJson = mapper.writeValueAsString(entry.getValue());
                Map<?, ?> nextMap = mapper.readValue(newJson, Map.class);
                for (Map.Entry<?, ?> nextEntry : nextMap.entrySet()) {
                    LOGGER.log(Level.INFO, nextEntry.getKey() + " = " + nextEntry.getValue());
                    String nextDirectoryName = "src/au/gov/asd/tac/constellation/help/docs/" + entry.getKey() + "/" + nextEntry.getKey();
                    File nextDirectory = new File(String.valueOf(nextDirectoryName));

                    if (!nextDirectory.exists()) {
                        nextDirectory.mkdir();
                    }

                    //Final layer of the file directory
                    String finalJson = mapper.writeValueAsString(nextEntry.getValue());
                    Map<?, ?> finalMap = mapper.readValue(finalJson, Map.class);
                    for (Map.Entry<?, ?> finalEntry : finalMap.entrySet()) {
                        LOGGER.log(Level.INFO, finalEntry.getKey() + " = " + finalEntry.getValue());
                        String finalDirectoryName = "src/au/gov/asd/tac/constellation/help/docs/" + entry.getKey() + "/" + nextEntry.getKey() + "/"
                                + finalEntry.getKey();
                        File finalDirectory = new File(String.valueOf(finalDirectoryName));

                        if (!finalDirectory.exists()) {
                            finalDirectory.mkdir();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FileStructureBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
