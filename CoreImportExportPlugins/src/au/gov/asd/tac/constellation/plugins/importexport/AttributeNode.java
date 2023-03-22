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
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.importexport.translator.AttributeTranslator;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;

/**
 * A representation of an Attribute on the GUI.
 * <p>
 * A graph Attribute (more specifically, it's label) is displayed in a Label
 * with a particular background color and a context menu. The context menu
 * allows properties of the AttributeNode (translator, default value) to be
 * edited.
 * <p>
 * AttributeNodes are originally displayed in their {@link AttributeList} but
 * can be dragged around the GUI, specifically onto column headers to indicate
 * that the user wants to import the values in that column into that specified
 * attribute.
 *
 * @author sirius
 */
public final class AttributeNode extends Label implements Comparable<AttributeNode> {

    private static final Image KEY_IMAGE = UserInterfaceIconProvider.KEY.buildImage(16, ConstellationColor.CHERRY.getJavaColor());
    private static final Image ADD_IMAGE = UserInterfaceIconProvider.ADD.buildImage(16, Color.BLACK);
    private static final String DOUBLE_UNDERSCORE = "__";

    private final AttributeList attributeList;
    private final ToggleGroup menuGroup = new ToggleGroup();
    private final Menu parseMenu = new Menu("Formatter");
    private final MenuItem deleteMenu = new MenuItem("Delete");
    private final MenuItem setDefaultMenuItem = new MenuItem("Set Default Value...");

    private Map<String, PluginParameters> recentTranslatorParameters = new HashMap<>();
    private AttributeTranslator translator = AttributeTranslator.getTranslators().get(0);
    private ImportTableColumn column;
    private PluginParameters translatorParameters;
    private Attribute attribute;
    private String defaultValue;
    private boolean isKey;

    /**
     *
     * @param attributeList The AttributeList to which this AttributeNode
     * belongs.
     * @param attribute The Attribute that this AttributeNode represents.
     * @param isKey True if this Attribute is a key.
     */
    public AttributeNode(final AttributeList attributeList, final Attribute attribute, final boolean isKey) {

        this.attributeList = attributeList;

        setMaxWidth(Double.MAX_VALUE);
        setAlignment(Pos.CENTER);
        final String attributeColor = attributeList.getAttributeType().getColor().getHtmlColor();
        final String attributeContrastingColor = ConstellationColor.getContrastHtmlColor(attributeColor).getHtmlColor();
        setStyle("-fx-background-color: " + attributeColor + "; "
                + "-fx-text-fill:" + attributeContrastingColor + "; "
                + "-fx-border-color: black; "
                + "-fx-border-radius: 5; "
                + "-fx-background-radius: 5;");

        // Create a context menu.
        final ContextMenu menu = new ContextMenu();

        menu.getItems().add(parseMenu);

        final List<AttributeTranslator> attributeTranslators = AttributeTranslator.getTranslators();
        for (final Iterator<AttributeTranslator> i = attributeTranslators.iterator(); i.hasNext();) {
            if (!i.next().appliesToAttributeType(attribute.getAttributeType())) {
                i.remove();
            }
        }

        attributeTranslators.stream().map(at -> {
            final RadioMenuItem item = new RadioMenuItem(at.getLabel());
            item.setSelected(at.getClass().equals(translator.getClass()));
            item.setToggleGroup(menuGroup);
            item.setOnAction((ActionEvent t) -> attributeItemAction(at));
            return item;
        }).forEachOrdered(item -> parseMenu.getItems().add(item));

        setDefaultMenuItem.setOnAction((ActionEvent event) -> {
            defaultValue = attributeList.importController.showSetDefaultValueDialog(attribute.getName(), defaultValue);
            updateDefaultValue();
            attributeList.getRunPane().setDraggingAttributeNode(this);
            attributeList.getRunPane().validate(AttributeNode.this.column);
        });
        menu.getItems().add(setDefaultMenuItem);

        deleteMenu.setOnAction((ActionEvent event) -> attributeList.deleteAttributeNode(AttributeNode.this));
        menu.getItems().add(deleteMenu);

        setAttribute(attribute, isKey);

        setContextMenu(menu);
    }

    private void attributeItemAction(final AttributeTranslator at) {
        PluginParameters parameters;
        if (recentTranslatorParameters.containsKey(at.getLabel())) {
            parameters = recentTranslatorParameters.get(at.getLabel());
            if (parameters != null) {
                parameters = parameters.copy();
            }
        } else {
            parameters = at.createParameters();
        }
        if (parameters == null) {
            translator = at;
            translatorParameters = null;
            recentTranslatorParameters.put(at.getLabel(), null);
            if (AttributeNode.this.getColumn() != null) {
                attributeList.getRunPane().setDraggingAttributeNode(this);
                attributeList.getRunPane().validate(AttributeNode.this.column);
            }
        } else {
            final Window parent = attributeList.importController.getImportPane().getParentWindow();
            final PluginParametersDialog dialog = new PluginParametersDialog(
                    parent, at.getLabel() + " Parameters",
                    parameters, "OK", "Cancel");

            dialog.showAndWait();
            if (PluginParametersDialog.OK.equalsIgnoreCase(dialog.getResult())) {
                translator = at;
                translatorParameters = parameters;
                recentTranslatorParameters.put(at.getLabel(), parameters);
                if (AttributeNode.this.getColumn() != null) {
                    attributeList.getRunPane().setDraggingAttributeNode(this);
                    attributeList.getRunPane().validate(AttributeNode.this.column);
                }
            } else {
                updateTranslatorGroupToggle();
            }
        }
    }

    private void updateTranslatorGroupToggle() {
        for (final MenuItem item1 : parseMenu.getItems()) {
            if (item1.getText().equals(translator.getLabel())) {
                menuGroup.selectToggle((Toggle) item1);
                break;
            }
        }
    }

    /**
     * Update the label text to show the default value
     */
    public void updateDefaultValue() {
        setText(attribute.getName() + (defaultValue == null ? "" : (" = " + defaultValue)));
    }

    /**
     * The AttributeList to which this AttributeNode belongs.
     *
     * @return The AttributeList to which this AttributeNode belongs.
     */
    public AttributeList getAttributeList() {
        return attributeList;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public boolean isKey() {
        return isKey;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
        updateDefaultValue();
    }

    public AttributeTranslator getTranslator() {
        return translator;
    }

    public void setTranslator(final AttributeTranslator translator, final PluginParameters params) {
        this.translator = translator;
        this.translatorParameters = params;
        recentTranslatorParameters.put(translator.getLabel(), params);
        if (AttributeNode.this.getColumn() != null) {
            attributeList.getRunPane().setDraggingAttributeNode(this);
            attributeList.getRunPane().validate(AttributeNode.this.column);
        }

        updateTranslatorGroupToggle();
    }

    public PluginParameters getTranslatorParameters() {
        return translatorParameters;
    }

    public void setAttribute(final Attribute attribute, final boolean isKey) {
        if (attribute != this.attribute || this.isKey != isKey) {
            this.attribute = attribute;
            this.isKey = isKey;
            setText(attribute.getName());
            if (isKey) {
                setGraphic(new ImageView(KEY_IMAGE));
            } else if (attribute instanceof NewAttribute) {
                setGraphic(new ImageView(ADD_IMAGE));
            } else {
                setGraphic(null);
            }

            setTooltip(new Tooltip(String.format("%s: %s", attribute.getAttributeType(), attribute.getDescription())));

            deleteMenu.setDisable(!(attribute instanceof NewAttribute));
        }
    }

    public ImportTableColumn getColumn() {
        return column;
    }

    public void setColumn(final ImportTableColumn column) {
        this.column = column;
    }

    /**
     * Order attributes alphabetically, except keys come first and weird labels
     * (starting with "__" - DOUBLE_UNDERSCORE) come last.
     *
     * @param other The other AttributeNode.
     *
     * @return As per spec.
     */
    @Override
    public int compareTo(final AttributeNode other) {
        int comp = Boolean.compare(other.isKey(), isKey());
        if (comp == 0) {
            final String label = attribute.getName();
            final String otherLabel = other.attribute.getName();
            if (!label.startsWith(DOUBLE_UNDERSCORE) && otherLabel.startsWith(DOUBLE_UNDERSCORE)) {
                comp = -1;
            } else if (label.startsWith(DOUBLE_UNDERSCORE) && !otherLabel.startsWith(DOUBLE_UNDERSCORE)) {
                comp = 1;
            } else {
                comp = label.compareTo(otherLabel);
            }
        }
        return comp;
    }
}
