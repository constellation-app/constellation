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
package au.gov.asd.tac.constellation.views.attributeeditor.editors.operations;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.attributeeditor.AttributePrototype;

/**
 *
 * @author twilight_sparkle
 */
public class CreateAttributeEditOperation implements EditOperation {

    private AttributePrototype attrNew;

    public CreateAttributeEditOperation() {
        // Method intentionally left blank
    }

    @Override
    public void performEdit(final Object value) {
        this.attrNew = (AttributePrototype) value;
        PluginExecution.withPlugin(new CreateAttributePlugin()).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    @PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE})
    private final class CreateAttributePlugin extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Attribute Editor: Create Attribute";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            graph.addAttribute(attrNew.getElementType(), attrNew.getDataType(), attrNew.getAttributeName(), attrNew.getAttributeDescription(), attrNew.getDefaultValue(), null);
        }
    }

}
