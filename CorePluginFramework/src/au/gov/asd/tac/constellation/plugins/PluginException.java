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
package au.gov.asd.tac.constellation.plugins;

/**
 * An exception that is thrown during the running of a plugin to indicate that a
 * well-known and understood error occurred. These include errors such as the
 * user providing the wrong password or the graph being incompatible with the
 * plugin. These errors should be handled gracefully by the plugin. If a plugin
 * throws an exception other than a PluginException, it is considered by the
 * framework to be a programming error and a large dialog box complete with
 * stack traces are displayed.
 *
 * @author sirius
 */
public class PluginException extends Exception {

    private final PluginNotificationLevel notificationLevel;

    /**
     * Create a PluginException for a specific plugin to wrap another exception.
     *
     * @param plugin The plugin causing the exception
     * @param notificationLevel The notification level (severity) of the
     * exception
     * @param message A message detailing the exception
     * @param cause The underlying exception that caused this plugin exception
     * to be thrown
     */
    public PluginException(Plugin plugin, PluginNotificationLevel notificationLevel, String message, Throwable cause) {
        super("""
              %s failed with the following message:
              %s"""
                .formatted(
                      plugin.getName(), 
                      message
                ), cause);
        this.notificationLevel = notificationLevel;
    }

    /**
     * Create a PluginException to wrap another exception.
     *
     * @param notificationLevel The notification level (severity) of the
     * exception
     * @param cause The underlying exception that caused this plugin exception
     * to be thrown
     */
    public PluginException(PluginNotificationLevel notificationLevel, Throwable cause) {
        super(cause);
        this.notificationLevel = notificationLevel;
    }

    /**
     * Create a PluginException
     *
     * @param notificationLevel The notification level (severity) of the
     * exception
     * @param message A message detailing the exception
     */
    public PluginException(PluginNotificationLevel notificationLevel, String message) {
        super(message);
        this.notificationLevel = notificationLevel;
    }

    /**
     * Retrieves the notification level (severity) of the exception.
     *
     * @return A PluginNotificationLevel constant describing the severity of the
     * exception.
     */
    public PluginNotificationLevel getNotificationLevel() {
        return notificationLevel;
    }
}
