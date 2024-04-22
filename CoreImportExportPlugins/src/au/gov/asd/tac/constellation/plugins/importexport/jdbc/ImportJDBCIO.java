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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc;

import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.plugins.importexport.AttributeType;
import au.gov.asd.tac.constellation.plugins.importexport.GraphDestination;
import au.gov.asd.tac.constellation.plugins.importexport.ImportAttributeDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.ImportConstants;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDestination;
import au.gov.asd.tac.constellation.plugins.importexport.NewAttribute;
import au.gov.asd.tac.constellation.plugins.importexport.SchemaDestination;
import au.gov.asd.tac.constellation.plugins.importexport.TemplateListDialog;
import au.gov.asd.tac.constellation.plugins.importexport.TemplateUtilities;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.file.FilenameEncoder;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.scene.control.Alert;
import javafx.stage.Window;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbPreferences;

public final class ImportJDBCIO {

    private static final String IMPORT_JDBC_DIR = "ImportDatabase";
    private static final Logger LOGGER = Logger.getLogger(ImportJDBCIO.class.getName());

    private static final String SOURCE = "source";
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
    private static final String SHOW_ALL_SCHEMA_ATTRIBUTES = "show_all_schema_attributes";
    private static final String LOAD_TEMPLATE = "Load Template";
    private static final String SAVE_TEMPLATE = "Save Template";

    private ImportJDBCIO() {
        // add a private constructor to hide the implicit public one - java:S1118
    }

    public static void saveParameters(final Window parentWindow, final JDBCImportController importController) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File delimIoDir = new File(userDir, IMPORT_JDBC_DIR);
        if (!delimIoDir.exists()) {
            delimIoDir.mkdir();
        }

        if (!delimIoDir.isDirectory()) {
            final String message = String.format("Can't create directory '%s'.", delimIoDir);
            NotifyDisplayer.displayAlert(SAVE_TEMPLATE, "Templates Directory Error", message, Alert.AlertType.ERROR);
            return;
        }

        final String templName = new TemplateListDialog(parentWindow, false).getName(delimIoDir);
        if (templName != null) {
            // A JSON document to store everything in.
            // Two objects; the source data + the configuration data.
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode rootNode = mapper.createObjectNode();

            final ObjectNode source = rootNode.putObject(SOURCE);
            source.put(SCHEMA_INIT, importController.isSchemaInitialised());
            source.put(SHOW_ALL_SCHEMA_ATTRIBUTES, importController.isShowAllSchemaAttributesEnabled());

            // We don't want to rely on a particular kind of graph being current when we load this definition.
            // Therefore, we only save a schema factory as the destination.
            final ImportDestination<?> importd = importController.getDestination();
            final String destination;
            switch (importd) {
                case SchemaDestination schemaDestination -> destination = schemaDestination.getDestination().getName();
                case GraphDestination graphDestination -> destination = graphDestination.getGraph().getSchema().getFactory().getName();
                default -> throw new IllegalArgumentException(String.format("Unrecognised destination '%s'", importd));
            }
            source.put(DESTINATION, destination);

            // One ImportDefinition per tab.
            final ArrayNode definitionArray = rootNode.putArray(DEFINITIONS);
            final List<ImportDefinition> definitions = importController.getDefinitions(false);
            final String[] columns = importController.getCurrentColumns();
            definitions.stream().forEach(impdef -> definitionCompute(definitionArray, columns, impdef));

            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
            final File f = new File(delimIoDir, FilenameEncoder.encode(templName + FileExtensionConstants.JSON));
            try {
                mapper.writeValue(f, rootNode);
                StatusDisplayer.getDefault().setStatusText(String.format("Import definition saved to %s.", f.getPath()));
            } catch (final IOException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                final String msg = String.format("Can't save import definition: %s", ex.getMessage());
                final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }

    private static void definitionCompute(final ArrayNode definitionArray, final String[] columns, final ImportDefinition impdef) {
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
            final Consumer<ImportAttributeDefinition> importConsumer = (ImportAttributeDefinition iadef) -> {
                if (hasSavableAttribute(iadef)) {
                    final ObjectNode type = typeArray.addObject();

                    // Remember the column label as a check for a similar column when we load.
                    // There's no point remembering the column index:  the user might have moved the columns around.
                    // If the column index is not defined, then set the column label to null so
                    // that the settings still get applied in the attribute list on load
                    if (iadef.getColumnIndex() == ImportConstants.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN) {
                        type.putNull(COLUMN_LABEL);
                    } else {
                        type.put(COLUMN_LABEL, columns[iadef.getColumnIndex() + 1]);
                    }

                    type.put(ATTRIBUTE_LABEL, iadef.getAttribute().getName());
                    if (iadef.getAttribute() instanceof NewAttribute newAttribute) {
                        type.put(ATTRIBUTE_TYPE, newAttribute.getAttributeType());
                        type.put(ATTRIBUTE_DESCRIPTION, newAttribute.getDescription());
                    }

                    type.put(TRANSLATOR, iadef.getTranslator().getClass().getName());
                    type.put(TRANSLATOR_ARGS, iadef.getTranslator().getParameterValues(iadef.getParameters()));

                    if (iadef.getDefaultValue() != null) {
                        type.put(DEFAULT_VALUE, iadef.getDefaultValue());
                    } else {
                        type.putNull(DEFAULT_VALUE);
                    }
                }
            };
            iadefs.stream().forEach(importConsumer);
        }
    }

    public static void loadParameters(final Window parentWindow, final JDBCImportController importController) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File delimIoDir = new File(userDir, IMPORT_JDBC_DIR);

        final String templName = new TemplateListDialog(parentWindow, true).getName(delimIoDir);
        if (templName != null) {
            final File template = new File(delimIoDir, FilenameEncoder.encode(templName) + FileExtensionConstants.JSON);
            if (!template.canRead()) {
                NotifyDisplayer.display(String.format("Template %s does not exist", templName), NotifyDescriptor.ERROR_MESSAGE);
            } else {
                loadParameterFile(importController, delimIoDir, templName);
            }
        }
    }

    private static void loadParameterFile(final JDBCImportController importController, final File delimIoDir, final String templName) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(new File(delimIoDir, FilenameEncoder.encode(templName) + FileExtensionConstants.JSON));
            final JsonNode source = TemplateUtilities.getRequiredFieldFromTemplate(root, SOURCE);

            final String destination = TemplateUtilities.getRequiredFieldFromTemplate(source, DESTINATION).textValue();
            final SchemaFactory schemaFactory = SchemaFactoryUtilities.getSchemaFactory(destination);

            if (schemaFactory != null) {
                final List<ImportDefinition> definitions = TemplateUtilities.getImportDefinitions(importController, root, templName);

                try {
                    ((JDBCImportPane) importController.getImportPane()).update(importController, definitions);
                } finally {
                    importController.setClearManuallyAdded(true);
                }
            } else {
                final String message = String.format("Can't find schema factory '%s'", destination);
                NotifyDisplayer.displayAlert(LOAD_TEMPLATE, "Destination Schema Error", message, Alert.AlertType.ERROR);
            }
        } catch (final NoSuchElementException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
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
        return (iadef.getColumnIndex() != ImportConstants.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN)
                || ((iadef.getColumnIndex() == ImportConstants.ATTRIBUTE_NOT_ASSIGNED_TO_COLUMN)
                && (iadef.getParameters() != null || iadef.getDefaultValue() != null));
    }
}
