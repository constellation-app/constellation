/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.AttributeType;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.GraphDestination;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.ImportAttributeDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.ImportController;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.ImportDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.ImportDestination;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.ImportJDBCPlugin;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.JDBCImporterStage;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.NewAttribute;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.RowFilter;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.SchemaDestination;
import au.gov.asd.tac.constellation.plugins.importexport.jdbc.translator.AttributeTranslator;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.file.FilenameEncoder;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

public class ImportJDBCIO {

    private static final String IMPORT_DELIMITED_DIR = "ImportDelimited";

    private static final String SOURCE = "source";
    private static final String PARSER = "parser";
    private static final String SCHEMA_INIT = "schema_init";
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
    private static final String PARAMETERS = "parameters";
    private static final String JSON_EXTENSION = ".json";

    public static void saveParameters(final JDBCImporterStage stage, final ImportController importController) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File delimIoDir = new File(userDir, IMPORT_DELIMITED_DIR);
        if (!delimIoDir.exists()) {
            delimIoDir.mkdir();
        }

        if (!delimIoDir.isDirectory()) {
            final String msg = String.format("Can't create directory '%s'.", delimIoDir);
            NotifyDisplayer.display(msg, NotifyDescriptor.ERROR_MESSAGE);
            return;
        }

        final String templName = new TemplateListDialog(stage, false, null).getName(stage, delimIoDir);
        if (templName != null) {
            // A JSON document to store everything in.
            // Two objects; the source data + the configuration data.
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode rootNode = mapper.createObjectNode();

            final ObjectNode source = rootNode.putObject(SOURCE);
            source.put(SCHEMA_INIT, importController.isSchemaInitialised());

            // We don't want to rely on a particular kind of graph being current when we load this definition.
            // Therefore, we only save a schema factory as the destination.
            final ImportDestination<?> importd = importController.getDestination();
            final String destination;
            if (importd instanceof SchemaDestination) {
                destination = ((SchemaDestination) importd).getDestination().getName();
            } else if (importd instanceof GraphDestination) {
                destination = ((GraphDestination) importd).getGraph().getSchema().getFactory().getName();
            } else {
                throw new IllegalArgumentException(String.format("Unrecognised destination '%s'", importd));
            }
            source.put(DESTINATION, destination);

            // One ImportDefinition per tab.
            final ArrayNode definitionArray = rootNode.putArray(DEFINITIONS);
            final List<ImportDefinition> definitions = importController.getDefinitions();
            final String[] columns = importController.getCurrentColumns();
            definitions.stream().forEach(impdef -> {
                final ObjectNode def = definitionArray.addObject();

                def.put(FIRST_ROW, impdef.getFirstRow());

                if (impdef.getRowFilter() != null) {
                    final ObjectNode filter = def.putObject(FILTER);
                    final ArrayNode columnsArray = filter.putArray(COLUMNS);
                    for (final String column : impdef.getRowFilter().getColumns()) {
                        columnsArray.add(column);
                    }

                    final String script = impdef.getRowFilter().getScript();
                    if (script != null) {
                        filter.put(SCRIPT, script);
                    } else {
                        filter.putNull(SCRIPT);
                    }
                }

                final ObjectNode attrDefs = def.putObject(ATTRIBUTES);
                for (final AttributeType attrType : AttributeType.values()) {
                    final ArrayNode typeArray = attrDefs.putArray(attrType.name());
                    final List<ImportAttributeDefinition> iadefs = impdef.getDefinitions(attrType);
                    iadefs.stream().forEach(iadef -> {
                        if (hasSavableAttribute(iadef)) {
                            final ObjectNode type = typeArray.addObject();

                            // Remember the column label as a check for a similar column when we load.
                            // There's no point remembering the column index:  the user might have moved the columns around.
                            //                    type.put(COLUMN_INDEX, iadef.getColumnIndex());
                            // If the column index is not defined, then set the column label to null so
                            // that the settings still get applied in the attribute list on load
                            if (iadef.getColumnIndex() == ImportJDBCPlugin.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN) {
                                type.putNull(COLUMN_LABEL);
                            } else {
                                type.put(COLUMN_LABEL, columns[iadef.getColumnIndex() + 1]);
                            }

                            type.put(ATTRIBUTE_LABEL, iadef.getAttribute().getName());
                            if (iadef.getAttribute() instanceof NewAttribute) {
                                type.put(ATTRIBUTE_TYPE, iadef.getAttribute().getAttributeType());
                                type.put(ATTRIBUTE_DESCRIPTION, iadef.getAttribute().getDescription());
                            }

                            type.put(TRANSLATOR, iadef.getTranslator().getClass().getName());
                            type.put(TRANSLATOR_ARGS, iadef.getTranslator().getParameterValues(iadef.getParameters()));

                            if (iadef.getDefaultValue() != null) {
                                type.put(DEFAULT_VALUE, iadef.getDefaultValue());
                            } else {
                                type.putNull(DEFAULT_VALUE);
                            }
                        }
                    });
                }
            });

            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
            final File f = new File(delimIoDir, FilenameEncoder.encode(templName + JSON_EXTENSION));
            try {
                mapper.writeValue(f, rootNode);
                StatusDisplayer.getDefault().setStatusText(String.format("Import definition saved to %s.", f.getPath()));
            } catch (final IOException ex) {
                final String msg = String.format("Can't save import definition: %s", ex.getMessage());
                final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }

    public static void loadParameters(final JDBCImporterStage stage, final ImportController importController) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File delimIoDir = new File(userDir, IMPORT_DELIMITED_DIR);

        final String templName = new TemplateListDialog(stage, true, null).getName(stage, delimIoDir);
        if (templName != null) {
            final File template = new File(delimIoDir, FilenameEncoder.encode(templName) + JSON_EXTENSION);
            if (!template.canRead()) {
                final NotifyDescriptor nd = new NotifyDescriptor.Message(String.format("Template %s does not exist", templName), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } else {
                try {
                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode root = mapper.readTree(new File(delimIoDir, FilenameEncoder.encode(templName) + JSON_EXTENSION));
                    final JsonNode source = root.get(SOURCE);

                    final boolean schemaInit = source.get(SCHEMA_INIT).booleanValue();
                    importController.setSchemaInitialised(schemaInit);

                    final String destination = source.get(DESTINATION).textValue();
                    final SchemaFactory schemaFactory = SchemaFactoryUtilities.getSchemaFactory(destination);
                    if (schemaFactory != null) {
                        importController.setDestination(new SchemaDestination(schemaFactory));

                        final List<ImportDefinition> definitions = new ArrayList<>();
                        final ArrayNode definitionsArray = (ArrayNode) root.withArray(DEFINITIONS);
                        for (final JsonNode definitionNode : definitionsArray) {
                            final int firstRow = definitionNode.get(FIRST_ROW).intValue();
                            final RowFilter filter = new RowFilter();
                            if (definitionNode.has(FILTER)) {
                                final JsonNode filterNode = definitionNode.get(FILTER);
                                final String script = filterNode.get(SCRIPT).textValue();
                                final JsonNode columnsArray = filterNode.withArray(COLUMNS);
                                final List<String> columns = new ArrayList<>();
                                for (final JsonNode column : columnsArray) {
                                    columns.add(column.textValue());
                                }

                                filter.setScript(script);
                                filter.setColumns(columns.toArray(new String[columns.size()]));
                            }

                            final ImportDefinition impdef = new ImportDefinition(firstRow, filter);

                            final JsonNode attributesNode = definitionNode.get(ATTRIBUTES);
                            for (final AttributeType attrType : AttributeType.values()) {
                                final ArrayNode columnArray = (ArrayNode) attributesNode.withArray(attrType.toString());
                                for (final JsonNode column : columnArray) {
                                    final String columnLabel = column.get(COLUMN_LABEL).textValue();
                                    final String label = column.get(ATTRIBUTE_LABEL).textValue();
                                    if (!importController.hasAttribute(attrType.getElementType(), label)) {
                                        // Manually created attribute.
                                        final String type = column.get(ATTRIBUTE_TYPE).textValue();
                                        final String descr = column.get(ATTRIBUTE_DESCRIPTION).textValue();
                                        final NewAttribute a = new NewAttribute(attrType.getElementType(), type, label, descr);
                                        importController.createManualAttribute(a);
                                    }

                                    final Attribute attribute = importController.getAttribute(attrType.getElementType(), label);

                                    final AttributeTranslator translator = AttributeTranslator.getTranslator(column.get(TRANSLATOR).textValue());
                                    final String args = column.get(TRANSLATOR_ARGS).textValue();
                                    final String defaultValue = column.get(DEFAULT_VALUE).textValue();
                                    final PluginParameters params = translator.createParameters();
                                    translator.setParameterValues(params, args);

                                    final ImportAttributeDefinition iad = new ImportAttributeDefinition(columnLabel, defaultValue, attribute, translator, params);
                                    impdef.addDefinition(attrType, iad);
                                }
                            }

                            definitions.add(impdef);
                        }

                        importController.setClearManuallyAdded(false);
                        try {
                            stage.update(importController, definitions);
                        } finally {
                            importController.setClearManuallyAdded(true);
                        }
                    } else {
                        final String msg = String.format("Can't find schema factory '%s'", destination);
                        final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                } catch (final IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Determine if the attribute should be saved to the configuration file.
     * <p>
     * There are two cases when an attribute should be saved to the
     * configuration file.
     * <ul>
     * <li>If the attribute is assigned to a column</li>
     * <li>If the attribute is not assigned to a column but has a default value
     * or translations defined</li>
     * </ul>
     *
     * @param iadef Attribute definition
     * @return True if the attribute should be saved, False otherwise
     */
    public static boolean hasSavableAttribute(final ImportAttributeDefinition iadef) {
        return (iadef.getColumnIndex() != ImportJDBCPlugin.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN)
                || ((iadef.getColumnIndex() == ImportJDBCPlugin.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN)
                && (iadef.getParameters() != null || iadef.getDefaultValue() != null));
    }
}
