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
package au.gov.asd.tac.constellation.utilities.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MappingJsonFactory;

/**
 * Singleton containing a JsonFactory and a MappingJsonFactory, as only one of each need to be created application wide.
 *
 * @author Quasar985
 */
public class JsonFactoryUtilities {

    /* Both of these take significant time to initialise (~3 seconds for JsonFactory, and ~30 for MappingJsonFactory).
     * So they are only initialised when actually needed
     */
    private static JsonFactory jsonFactory = null;
    private static MappingJsonFactory mappingFactory = null;

    private JsonFactoryUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static JsonFactory getJsonFactory() {
        if (jsonFactory == null) {
            jsonFactory = new JsonFactory();
        }

        return jsonFactory;
    }

    public static MappingJsonFactory getMappingJsonFactory() {
        if (mappingFactory == null) {
            mappingFactory = new MappingJsonFactory();
        }

        return mappingFactory;
    }
}
