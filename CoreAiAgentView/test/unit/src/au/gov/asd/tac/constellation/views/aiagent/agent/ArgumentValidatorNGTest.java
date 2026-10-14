/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.agent;

import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for {@link ArgumentValidator}.
 */
public class ArgumentValidatorNGTest {

    private static final String SCHEMA = """
            {"type":"object","properties":{
              "name":{"type":"string"},
              "count":{"type":"integer"},
              "ratio":{"type":"number"},
              "flag":{"type":"boolean"},
              "mode":{"type":"string","enum":["a","b"]},
              "ids":{"type":"array","items":{"type":"integer"}},
              "nested":{"type":"object","properties":{"x":{"type":"string"}},"required":["x"]}
            },"required":["name"]}
            """;

    private static List<String> validate(final String args) throws Exception {
        final JsonNode schema = JsonFactoryUtilities.getObjectMapper().readTree(SCHEMA);
        return ArgumentValidator.validate(JsonFactoryUtilities.getObjectMapper().readTree(args), schema);
    }

    @Test
    public void testValid() throws Exception {
        assertTrue(validate("{\"name\":\"a\",\"count\":3,\"ratio\":1.5,\"flag\":true,\"mode\":\"b\",\"ids\":[1,2],\"nested\":{\"x\":\"y\"}}").isEmpty());
    }

    @Test
    public void testLenientScalars() throws Exception {
        // models often quote numbers and booleans
        assertTrue(validate("{\"name\":\"a\",\"count\":\"3\",\"ratio\":\"1.5\",\"flag\":\"true\"}").isEmpty());
    }

    @Test
    public void testProblems() throws Exception {
        final List<String> problems = validate("{\"count\":\"x\",\"mode\":\"c\",\"ids\":[\"q\"],\"nested\":{}}");
        assertEquals(problems.size(), 5, problems.toString());
        assertTrue(problems.contains("arguments.name is required"));
        assertTrue(problems.contains("arguments.count must be an integer"));
        assertTrue(problems.stream().anyMatch(p -> p.startsWith("arguments.mode must be one of")));
        assertTrue(problems.contains("arguments.ids[0] must be an integer"));
        assertTrue(problems.contains("arguments.nested.x is required"));
    }

    @Test
    public void testNotAnObject() throws Exception {
        assertEquals(validate("[1]"), List.of("arguments must be an object"));
    }
}
