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

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.plugins.importexport.model.CellValue;
import au.gov.asd.tac.constellation.plugins.importexport.model.CellValueProperty;
import au.gov.asd.tac.constellation.plugins.importexport.model.TableRow;
import au.gov.asd.tac.constellation.plugins.importexport.translator.AttributeTranslator;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;

/**
 * ImportTableColumn extends a standard JavaFX table column to add field
 * validation.
 *
 * @author sirius
 */
public class ImportTableColumn extends TableColumn<TableRow, CellValue> {

    private static final Logger LOGGER = Logger.getLogger(ImportTableColumn.class.getName());
    private static final Insets BORDERPANE_PADDING = new Insets(2);

    private final String label;
    private final int columnIndex;

    public ImportTableColumn(final String label, final int columnIndex) {
        this.label = label;
        this.columnIndex = columnIndex;
        final ColumnHeader header = new ColumnHeader(label);
        setGraphic(header);
    }

    public String getLabel() {
        return label;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public boolean validate(final List<TableRow> data) {
        final AttributeNode attributeNode = getAttributeNode();

        boolean columnFailed = false;
                
        if (attributeNode != null) {
            final AttributeTranslator parser = attributeNode.getTranslator();
            final PluginParameters parserParameters = attributeNode.getTranslatorParameters();
            final String defaultValue = attributeNode.getDefaultValue();

            Class<? extends AttributeDescription> attributeDescriptionClass = AttributeRegistry.getDefault()
                    .getAttributes().get(attributeNode.getAttribute().getAttributeType());
            // Handle pseudo-attributes
            if (attributeDescriptionClass == null && attributeNode.getAttribute().getName().equals(ImportController.DIRECTED)) {
                attributeDescriptionClass = BooleanAttributeDescription.class;
            }
            try {
                final AttributeDescription attributeDescription = attributeDescriptionClass != null
                        ? attributeDescriptionClass.getDeclaredConstructor().newInstance()
                        : BooleanAttributeDescription.class.getDeclaredConstructor().newInstance();

                for (final TableRow row : data) {                    
                    columnFailed = processRow(row, attributeDescription, parser, parserParameters, defaultValue) || columnFailed;
                }
            } catch (final IllegalAccessException | IllegalArgumentException
                    | InstantiationException | NoSuchMethodException
                    | SecurityException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        } else {
            data.stream().map(row -> row.getProperty(columnIndex)).map(property -> {
                property.setText(property.get().getOriginalText());
                return property;
            }).forEachOrdered(property -> property.setMessage(null, false));
        }
        getGraphic().setStyle(columnFailed ? "-fx-background-color: rgba(255, 0, 0, 0.3);"
                : "-fx-background-color: transparent;");

        return !columnFailed;
    }

    private boolean processRow(final TableRow row, final AttributeDescription attributeDescription,
            final AttributeTranslator parser, final PluginParameters parserParameters, final String defaultValue) {
        boolean columnFailed = false;
        final CellValueProperty property = row.getProperty(columnIndex);
        final String value = property.get().getOriginalText();
        final String parsedValue = parser.translate(value, parserParameters);
        final String errorMessage = attributeDescription.acceptsString(parsedValue);
        columnFailed |= errorMessage != null;
        if (parsedValue == null ? (value == null) : (parsedValue.equals(value))) {
            property.setText(value);
            if (errorMessage != null && defaultValue == null) {
                property.setMessage(errorMessage, true);
            } else {
                property.setMessage(null, false);
                columnFailed = false;
            }
        } else {
            property.setText(parsedValue);
            if (errorMessage != null) {
                property.setMessage(errorMessage, true);
            } else {
                property.setMessage("Original: " + value, false);
            }
        }
        return columnFailed;
    }

    public AttributeNode getAttributeNode() {
        final ColumnHeader header = (ColumnHeader) getGraphic();
        if (header.getChildren().size() > 1) {
            final Node node = header.getChildren().get(1);
            if (node instanceof AttributeNode attributeNode) {
                return attributeNode;
            }
        }
        return null;
    }

    public void setAttributeNode(final AttributeNode attributeNode) {
        final ColumnHeader header = (ColumnHeader) getGraphic();
        header.setCenter(attributeNode == null ? new Label() : attributeNode);
    }

    private class ColumnHeader extends BorderPane {

        private final Label label;

        public ColumnHeader(final String label) {
            setPadding(BORDERPANE_PADDING);
            this.label = new Label(label);
            BorderPane.setAlignment(this.label, Pos.CENTER);
            setTop(this.label);
            setCenter(new Label());
        }

        @Override
        public String toString() {
            return label.getText();
        }
    }
}
