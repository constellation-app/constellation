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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.model.CellValue;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.model.CellValueProperty;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.model.TableRow;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.translator.AttributeTranslator;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.List;
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

    public void validate(final List<TableRow> data) {
        AttributeNode attributeNode = getAttributeNode();

        boolean columnFailed = false;

        if (attributeNode != null) {
            AttributeTranslator parser = attributeNode.getTranslator();
            PluginParameters parserParameters = attributeNode.getTranslatorParameters();
            String defaultValue = attributeNode.getDefaultValue();

            Class<? extends AttributeDescription> attributeDescriptionClass = AttributeRegistry.getDefault().getAttributes().get(attributeNode.getAttribute().getAttributeType());
            if (attributeDescriptionClass == null) {
                // Handle pseudo-attributes
                if (attributeNode.getAttribute().getName().equals(ImportController.DIRECTED)) {
                    attributeDescriptionClass = BooleanAttributeDescription.class;
                }
            }
            try {
                AttributeDescription attributeDescription = attributeDescriptionClass.newInstance();
                for (TableRow row : data) {
                    CellValueProperty property = row.getProperty(columnIndex);
                    String value = property.get().getOriginalText();
                    String parsedValue = parser.translate(value, parserParameters);
                    String errorMessage = attributeDescription.acceptsString(parsedValue);
                    columnFailed |= errorMessage != null;
                    if (parsedValue == null ? value == null : parsedValue.equals(value)) {
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
                }
            } catch (IllegalAccessException | InstantiationException ex) {
                ex.printStackTrace();
            }
        } else {
            for (TableRow row : data) {
                CellValueProperty property = row.getProperty(columnIndex);
                property.setText(property.get().getOriginalText());
                property.setMessage(null, false);
            }
        }

        if (columnFailed) {
            getGraphic().setStyle("-fx-background-color: rgba(255, 0, 0, 0.3);");
        } else {
            getGraphic().setStyle("-fx-background-color: transparent;");
        }
    }

    public AttributeNode getAttributeNode() {
        ColumnHeader header = (ColumnHeader) getGraphic();
        if (header.getChildren().size() > 1) {
            Node node = header.getChildren().get(1);
            if (node instanceof AttributeNode) {
                return (AttributeNode) node;
            }
        }
        return null;
    }

    public void setAttributeNode(AttributeNode attributeNode) {
        ColumnHeader header = (ColumnHeader) getGraphic();
        header.setCenter(attributeNode == null ? new Label() : attributeNode);
    }

    private class ColumnHeader extends BorderPane {

        private final Label label;

        public ColumnHeader(String label) {
            setPadding(new Insets(2));
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
