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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Masks secrets in tool arguments before they are displayed.
 *
 * @author doubtsec
 */
public final class Redactor {

    public static final String MASK = "••••••";
    private static final Pattern SECRET_NAME = Pattern.compile(".*(password|passwd|token|secret|api[_-]?key|credential).*");

    private Redactor() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param name a property or parameter name.
     * @return true if the name suggests a secret.
     */
    public static boolean isSecretName(final String name) {
        return name != null && SECRET_NAME.matcher(name.toLowerCase(Locale.ROOT)).matches();
    }

    /**
     * Return a copy of a JSON value with secret-looking properties masked.
     *
     * @param node the value.
     * @return the redacted copy.
     */
    public static JsonNode redact(final JsonNode node) {
        if (node == null) {
            return null;
        }
        final JsonNode copy = node.deepCopy();
        redactInPlace(copy);
        return copy;
    }

    private static void redactInPlace(final JsonNode node) {
        if (node instanceof ObjectNode object) {
            object.properties().forEach(entry -> {
                if (isSecretName(entry.getKey()) && entry.getValue().isValueNode()) {
                    object.put(entry.getKey(), MASK);
                } else {
                    redactInPlace(entry.getValue());
                }
            });
        } else if (node instanceof ArrayNode array) {
            array.forEach(Redactor::redactInPlace);
        }
    }
}
