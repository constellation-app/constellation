/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.utilities;

import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.file.GraphMLImportProcessor.DATA_TAG;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.file.GraphMLImportProcessor.KEY_TAG;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.file.GraphMLImportProcessor.NAME_TYPE_DELIMITER;
import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author canis_majoris
 */
public class GraphMLUtilities {
    
    private GraphMLUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * This method adds attributes to nodes or transactions
     *
     * @param node
     * @param nodeAttributes
     * @param result
     * @param element
     */
    public static void addAttributes(final Node node, final Map<String, String> nodeAttributes, final RecordStore result, final String element) {
        final NodeList children = node.getChildNodes();
        for (int childIndex = 0; childIndex < children.getLength(); childIndex++) {
            final Node childNode = children.item(childIndex);
            if (childNode != null && childNode.getNodeName().equals(DATA_TAG)) {
                final String attribute = childNode.getAttributes().getNamedItem(KEY_TAG).getNodeValue();
                final String value = childNode.getTextContent();
                final String attr = nodeAttributes.get(attribute);
                final String attrName = attr.split(NAME_TYPE_DELIMITER)[0];
                final String attrType = attr.split(NAME_TYPE_DELIMITER)[1];
                addAttribute(result, element, attrType, attrName, value);
            }
        }
    }

    /**
     * This method adds the attribute to the RecordStore depending on the type.
     *
     * @param result
     * @param element
     * @param attrType
     * @param attrName
     * @param value
     */
    public static void addAttribute(final RecordStore result, final String element, final String attrType, final String attrName, final String value) {
        switch (attrType) {
            case "boolean":
                result.set(element + attrName, Boolean.parseBoolean(value));
                break;
            case "int":
                result.set(element + attrName, Integer.parseInt(value));
                break;
            case "long":
                result.set(element + attrName, Long.parseLong(value));
                break;
            case "float":
                result.set(element + attrName, Float.parseFloat(value));
                break;
            case "double":
                result.set(element + attrName, Double.parseDouble(value));
                break;
            default:
                result.set(element + attrName, value);
                break;
        }
    }

}
