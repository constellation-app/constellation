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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.plugins.importexport.translator.AttributeTranslator;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import javafx.scene.control.Alert;

/**
 * Contains common functions performed by ImportDelimitedIO and ImportJDBCIO
 * when handling Templates
 *
 * @author algol
 */
public class TemplateUtilities {
    private static final String SOURCE = "source";
    private static final String SCHEMA_INIT = "schema_init";
    private static final String SHOW_ALL_SCHEMA_ATTRIBUTES = "show_all_schema_attributes";
    private static final String DESTINATION = "destination";
    private static final String DEFINITIONS = "definitions";
    private static final String FILTER = "filter";
    private static final String SCRIPT = "script";
    private static final String COLUMNS = "columns";
    private static final String FIRST_ROW = "first_row";
    private static final String ATTRIBUTES = "attributes";
    private static final String COLUMN_LABEL = "column_label";
    private static final String ATTRIBUTE_LABEL = "attribute_label";
    private static final String ATTRIBUTE_TYPE = "attribute_type";
    private static final String ATTRIBUTE_DESCRIPTION = "attribute_description";
    private static final String TRANSLATOR = "translator";
    private static final String TRANSLATOR_ARGS = "translator_args";
    private static final String DEFAULT_VALUE = "default_value";
    private static final String LOAD_TEMPLATE = "Load Template";

    private TemplateUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * This method is used to retrieve the essential fields from a template. If
     * any is missing, a warning is displayed and NoSuchElementException is
     * thrown.
     * @param jsonNode
     * @param fieldName
     * @return 
     */
    public static JsonNode getRequiredFieldFromTemplate(final JsonNode jsonNode, final String fieldName) {
        if (jsonNode.has(fieldName)) {
            return jsonNode.get(fieldName);
        } else {
            //To handle npe when json parsor accessing missing required fields in the template
            final String message = """
                                   Missing field '%s' in the template. Add the missing field manually or use a new template."""
                    .formatted(fieldName);
            NotifyDisplayer.displayAlert(LOAD_TEMPLATE, "Template Error", message, Alert.AlertType.ERROR);
            throw new NoSuchElementException(NotifyDisplayer.BLOCK_POPUP_FLAG + message);
        }
    }

    public static List<ImportDefinition> getImportDefinitions(final ImportController importController, final JsonNode root, final String templName) {
        final List<ImportDefinition> definitions = new ArrayList<>();
        int tabCount = 0;
        final JsonNode source = getRequiredFieldFromTemplate(root, SOURCE);
        final String destination = getRequiredFieldFromTemplate(source, DESTINATION).textValue();
        final SchemaFactory schemaFactory = SchemaFactoryUtilities.getSchemaFactory(destination);
        final boolean schemaInit = getRequiredFieldFromTemplate(source, SCHEMA_INIT).booleanValue();

        importController.setDestination(new SchemaDestination(schemaFactory));
        importController.setSchemaInitialised(schemaInit);
        final boolean showAllSchemaAttributes = source.get(SHOW_ALL_SCHEMA_ATTRIBUTES) != null
                && source.get(SHOW_ALL_SCHEMA_ATTRIBUTES).booleanValue();

        ((ImportController) importController).getImportPane().setTemplateOptions(showAllSchemaAttributes);

        final ArrayNode definitionsArray = (ArrayNode) root.withArray(DEFINITIONS);
        for (final JsonNode definitionNode : definitionsArray) {
            tabCount++;
            final int firstRow = getRequiredFieldFromTemplate(definitionNode, FIRST_ROW).intValue();

            final RowFilter filter = populateFilter(definitionNode);
            final ImportDefinition impdef = new ImportDefinition("", firstRow, filter);

            final List<String> missingUserAttributes = populateImportDefinitions(importController, definitionNode, impdef);
            definitions.add(impdef);

            if (!missingUserAttributes.isEmpty()) {
                final String message = """
                                       The template `%s` is outdated or corrupted. In the tab `Run %d` following attributes are considered user added for the destination `%s` with `showAllSchemaAttributes` %s, as specified in the template. Hence they require `attribute_type` property.
                                       
                                       %s 
                                       
                                       Consider using a new template or add the missing attributes manually."""
                        .formatted(templName, tabCount, schemaFactory.getLabel(), showAllSchemaAttributes ? "enabled" : "disabled", Arrays.toString(missingUserAttributes.toArray()));

                NotifyDisplayer.displayAlert(LOAD_TEMPLATE, "Template Error", message, Alert.AlertType.WARNING);
            }
        }
        importController.setClearManuallyAdded(false);
        return definitions;
    }

    private static RowFilter populateFilter(final JsonNode definitionNode) {
        final RowFilter filter = new RowFilter();
        if (definitionNode.has(FILTER)) {
            final JsonNode filterNode = getRequiredFieldFromTemplate(definitionNode, FILTER);
            final String script = getRequiredFieldFromTemplate(filterNode, SCRIPT).textValue();
            final JsonNode columnsArray = filterNode.withArray(COLUMNS);
            final List<String> columns = new ArrayList<>();
            for (final JsonNode column : columnsArray) {
                columns.add(column.textValue());
            }

            filter.setScript(script);
            filter.setColumns(columns.toArray(new String[columns.size()]));
        }
        return filter;
    }

    private static List<String> populateImportDefinitions(final ImportController importController, final JsonNode definitionNode, final ImportDefinition impdef) {
        final List<String> missingUserAttributes = new ArrayList<>();
        final JsonNode attributesNode = getRequiredFieldFromTemplate(definitionNode, ATTRIBUTES);

        for (final AttributeType attrType : AttributeType.values()) {
            final ArrayNode columnArray = (ArrayNode) attributesNode.withArray(attrType.toString());

            for (final JsonNode column : columnArray) {
                final String label = getRequiredFieldFromTemplate(column, ATTRIBUTE_LABEL).textValue();
                if (!importController.hasAttribute(attrType.getElementType(), label)) {
                    if (!column.has(ATTRIBUTE_TYPE)) {
                        missingUserAttributes.add(label);
                        continue;
                    }
                    // Manually created attribute.
                    final String type = getRequiredFieldFromTemplate(column, ATTRIBUTE_TYPE).textValue();
                    final String descr = (column.has(ATTRIBUTE_DESCRIPTION)) ? column.get(ATTRIBUTE_DESCRIPTION).textValue() : "";
                    final NewAttribute a = new NewAttribute(attrType.getElementType(), type, label, descr);
                    importController.createManualAttribute(a);
                }
                impdef.addDefinition(attrType, getImportAttributeDefinition(importController, attrType, column, label));
            }
        }
        return missingUserAttributes;
    }

    private static ImportAttributeDefinition getImportAttributeDefinition(final ImportController importController, final AttributeType attrType, final JsonNode column, final String label) {
        final String columnLabel = getRequiredFieldFromTemplate(column, COLUMN_LABEL).textValue();
        final Attribute attribute = importController.getAttribute(attrType.getElementType(), label);

        final AttributeTranslator translator = AttributeTranslator.getTranslator(TemplateUtilities.getRequiredFieldFromTemplate(column, TRANSLATOR).textValue());
        final String args = getRequiredFieldFromTemplate(column, TRANSLATOR_ARGS).textValue();
        final String defaultValue = getRequiredFieldFromTemplate(column, DEFAULT_VALUE).textValue();
        final PluginParameters params = translator.createParameters();
        translator.setParameterValues(params, args);

        return new ImportAttributeDefinition(columnLabel, defaultValue, attribute, translator, params);
    }
}
