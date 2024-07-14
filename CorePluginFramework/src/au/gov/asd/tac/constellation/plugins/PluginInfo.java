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

import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be added to plugin implementations that further
 * describe their behaviour.
 *
 * @author sirius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginInfo {

    /**
     * The minimum number of milliseconds that need to elapse before an
     * additional log entry will be created for this plugin. This is to prevent
     * plugins that get called very often from creating too many log entries.
     *
     * @return A long representing the minimum number of milliseconds required.
     */
    public long minLogInterval() default -1;

    /**
     * The general type of the plugin based on its functionality
     *
     * @return A pluginType enumeration constant corresponding to the type of
     * the plugin that this info object is annotating.
     */
    public PluginType pluginType() default PluginType.NONE;

    /**
     * A list of tags which describe this plugin.
     *
     * @return a list of tags associated with this plugin.
     */
    public String[] tags() default PluginTags.GENERAL;
}
