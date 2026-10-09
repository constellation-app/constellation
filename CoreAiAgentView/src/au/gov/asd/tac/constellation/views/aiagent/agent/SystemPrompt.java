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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The system prompt sent at the start of every conversation.
 *
 * @author constellation
 */
public final class SystemPrompt {

    private static final Logger LOGGER = Logger.getLogger(SystemPrompt.class.getName());
    private static final String RESOURCE = "/au/gov/asd/tac/constellation/views/aiagent/resources/system-prompt.md";
    private static String defaultPrompt;

    private SystemPrompt() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @return the built in system prompt.
     */
    public static synchronized String getDefault() {
        if (defaultPrompt == null) {
            try (final InputStream in = SystemPrompt.class.getResourceAsStream(RESOURCE)) {
                defaultPrompt = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (final IOException ex) {
                LOGGER.log(Level.WARNING, "Couldn't read the default AI assistant system prompt", ex);
                defaultPrompt = "";
            }
        }
        return defaultPrompt;
    }

    /**
     * @param override a user supplied prompt, may be null or blank.
     * @return the override if there is one, otherwise the built in prompt.
     */
    public static String resolve(final String override) {
        return override == null || override.isBlank() ? getDefault() : override;
    }
}
