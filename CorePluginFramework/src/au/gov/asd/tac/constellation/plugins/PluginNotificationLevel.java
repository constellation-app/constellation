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
package au.gov.asd.tac.constellation.plugins;

/**
 * A PluginNotificationLevel represents the seriousness of an error or message
 * during the running of a plugin.
 */
public enum PluginNotificationLevel {

    /**
     * A error has occurred that is serious enough to mean that Constellation
     * should exit.
     */
    FATAL,
    /**
     * An error has occurred that is serious enough for the plugin to stop
     * running.
     */
    ERROR,
    /**
     * Ann error has occurred that the user should know about, although the
     * plugin could complete.
     */
    WARNING,
    /**
     * Something happened during the running of a plugin that the user should be
     * aware of but would not really be considered an error.
     */
    INFO,
    /**
     * Something happened that should be logged but the user does not need to
     * see it.
     */
    DEBUG

}
