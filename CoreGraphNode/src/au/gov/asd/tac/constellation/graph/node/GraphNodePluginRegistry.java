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
package au.gov.asd.tac.constellation.graph.node;

import au.gov.asd.tac.constellation.graph.node.templates.LoadTemplatePlugin;
import au.gov.asd.tac.constellation.graph.node.templates.ManageTemplatesPlugin;
import au.gov.asd.tac.constellation.graph.node.templates.SaveTemplatePlugin;

/**
 *
 * @author arcturus
 */
public class GraphNodePluginRegistry {

    public static final String LOAD_TEMPLATE = LoadTemplatePlugin.class.getName();
    public static final String MANAGE_TEMPLATES = ManageTemplatesPlugin.class.getName();
    public static final String SAVE_TEMPLATE = SaveTemplatePlugin.class.getName();
}
