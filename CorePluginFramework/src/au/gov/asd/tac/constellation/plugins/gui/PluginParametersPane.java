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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters.PluginParametersNode;
import au.gov.asd.tac.constellation.plugins.parameters.types.ActionParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType.LocalDateParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.NumberParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType.ParameterListParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;

/**
 * The GUI which provides the interface to a {@link PluginParameters} object.
 * <p>
 * Typically this is built from the tree of {@link PluginParametersNode}
 * objects, each of which contain a {@link ParameterLayout} object, that
 * together describe the hierarchical visual structure of the pane.
 * <p>
 * The leaves in this tree correspond to the individual {@link PluginParameter}
 * objects. Each leaf node's layout will add a single widget for interacting
 * with the specified parameter to the pane. Internal nodes typically
 * dateFormatter groups of these widgets, for example placing them horizontally
 * rather than vertically, or grouping them under a subheading.
 *
 * @author ruby_crucis
 */
public final class PluginParametersPane extends GridPane {

    private static final Insets HELP_INSETS = new Insets(0, 0, 0, 0);

    /**
     * An abstract class which describes the visual layout of a
     * {@link PluginParametersNode} within a {@link PluginParametersPane}.
     */
    public abstract static class ParameterLayout {

        private boolean firstElement = true;

        public abstract Pane getParamPane(final PluginParametersNode node);

        public abstract LabelDescriptionBox getParamLabel(final PluginParametersNode node);

        public abstract Button getParamHelp(final PluginParametersNode node);

        public boolean nextElement(final PluginParametersNode node) {
            if (firstElement) {
                firstElement = false;
                return true;
            }
            return false;
        }

        public boolean hasMultipleElements() {
            return false;
        }

        public boolean supportsChildrenWithMultipleElements() {
            return false;
        }

        public abstract ParameterLayout copy();
    }

    /**
     * Returns null elements when trying to retrieve any aspect of the GUI for a
     * {@link PluginParametersNode}.
     */
    public static class NullLayout extends ParameterLayout {

        @Override
        public LabelDescriptionBox getParamLabel(final PluginParametersNode node) {
            return null;
        }

        @Override
        public Pane getParamPane(final PluginParametersNode node) {
            return null;
        }

        @Override
        public Button getParamHelp(final PluginParametersNode node) {
            return null;
        }

        @Override
        public ParameterLayout copy() {
            return new NullLayout();
        }
    }

    /**
     * Returns a javafx separator and then delegates subsequent GUI construction
     * to child nodes.
     */
    public static class SeparatedParameterLayout extends ParameterLayout {

        protected int currentChild = -1;
        protected final boolean separatorAfter;
        protected final String helpId;

        @Override
        public ParameterLayout copy() {
            return new SeparatedParameterLayout(separatorAfter, helpId);
        }

        @Override
        public boolean hasMultipleElements() {
            return true;
        }

        @Override
        public boolean supportsChildrenWithMultipleElements() {
            return true;
        }

        public SeparatedParameterLayout(final boolean separatorAfter) {
            this(separatorAfter, null);
        }

        public SeparatedParameterLayout(final boolean separatorAfter, final String helpId) {
            this.separatorAfter = separatorAfter;
            this.helpId = helpId;
        }

        private boolean nextChildElement(final PluginParametersNode node) {
            return node.getChildren().get(currentChild).getFormatter().nextElement(node.getChildren().get(currentChild));
        }

        @Override
        public boolean nextElement(final PluginParametersNode node) {
            if (super.nextElement(node)) {
                return true;
            }
            if (currentChild == node.getChildren().size()) {
                return false;
            }
            return ((currentChild != -1 && nextChildElement(node))
                    || (++currentChild != node.getChildren().size() && nextChildElement(node))
                    || separatorAfter);
        }

        @Override
        public LabelDescriptionBox getParamLabel(final PluginParametersNode node) {
            if (currentChild == -1 || currentChild == node.getChildren().size()
                    || node.getChildren().isEmpty()) {
                return null;
            }
            final PluginParametersNode child = node.getChildren().get(currentChild);
            return child.getFormatter().getParamLabel(child);
        }

        @Override
        public Pane getParamPane(final PluginParametersNode node) {
            if (node.getChildren().isEmpty()) {
                return null;
            }
            if (currentChild == -1 || currentChild == node.getChildren().size()) {
                final Separator separator = new Separator();
                separator.setStyle("-fx-background-color:#444444;");
                HBox separatorBox = new HBox(separator);
                HBox.setHgrow(separator, Priority.ALWAYS);
                return new VBox(new Label("test"), separatorBox);
            }
            final PluginParametersNode child = node.getChildren().get(currentChild);
            return child.getFormatter().getParamPane(child);
        }

        @Override
        public Button getParamHelp(final PluginParametersNode node) {
            if (currentChild == -1 && helpId != null) {
                return buildHelpButton(helpId, "this group");
            }
            if (currentChild == -1 || currentChild == node.getChildren().size()
                    || node.getChildren().isEmpty()) {
                return null;
            }
            final PluginParametersNode child = node.getChildren().get(currentChild);
            return child.getFormatter().getParamHelp(child);
        }
    }

    /**
     * Returns a label with a given title followed by a javafx separator and
     * then delegates subsequent GUI construction to child nodes.
     */
    public static class TitledSeparatedParameterLayout extends SeparatedParameterLayout {

        private final String title;
        private final int fontSize;

        private Label titleLabel;
        private SimpleDoubleProperty maxParamWidth = new SimpleDoubleProperty();

        public TitledSeparatedParameterLayout(final String label, final int fontSize, final boolean separatorAfter) {
            this(label, fontSize, separatorAfter, null);
        }

        public TitledSeparatedParameterLayout(final String label, final int fontSize, final boolean separatorAfter, final String helpId) {
            super(separatorAfter, helpId);
            this.title = label;
            this.fontSize = fontSize;
        }

        @Override
        public ParameterLayout copy() {
            return new TitledSeparatedParameterLayout(title, fontSize, separatorAfter, helpId);
        }

        @Override
        public Pane getParamPane(final PluginParametersNode node) {
            if (node.getChildren().isEmpty()) {
                return null;
            }
            if (currentChild == -1) {
                titleLabel = new Label(title);
                titleLabel.setFont(Font.font(Font.getDefault().getFamily(), FontPosture.ITALIC, fontSize));
                final Separator separator = new Separator();
                separator.setStyle("-fx-background-color:#444444;");
                HBox separatorBox = new HBox(separator);
                HBox.setHgrow(separator, Priority.ALWAYS);
                HBox.setMargin(separator, new Insets(PADDING, 0, 0, 0));
                return new VBox(titleLabel, separatorBox);
            }
            final Pane paramPane = super.getParamPane(node);
            final SimpleDoubleProperty updated = new SimpleDoubleProperty();
            updated.bind(Bindings.max(maxParamWidth, paramPane.widthProperty()));
            maxParamWidth = updated;
            if (titleLabel != null) {
                titleLabel.prefWidthProperty().bind(maxParamWidth);
            }
            return paramPane;
        }

        @Override
        public Button getParamHelp(final PluginParametersNode node) {
            if (currentChild == -1 && helpId != null) {
                return buildHelpButton(helpId, String.format("group '%s'", title));
            }
            return super.getParamHelp(node);
        }
    }

    /**
     * Returns a warning icon and a label with the given message and then
     * delegates subsequent GUI construction to child nodes.
     */
    public static class WarningSeparatedParameterLayout extends SeparatedParameterLayout {

        private final String message;
        private final int fontSize;
        private ImageView warningIcon;
        private Label warningLabel;
        private SimpleDoubleProperty maxParamWidth = new SimpleDoubleProperty(0);

        public WarningSeparatedParameterLayout(final Image icon, final String message, final int fontSize, final boolean separatorAfter) {
            this(icon, message, fontSize, separatorAfter, null);
        }

        public WarningSeparatedParameterLayout(final Image icon, final String message, final int fontSize, final boolean separatorAfter, final String helpId) {
            super(separatorAfter, helpId);
            this.warningIcon = new ImageView(icon);
            this.message = message;
            this.fontSize = fontSize;
        }

        @Override
        public ParameterLayout copy() {
            return new WarningSeparatedParameterLayout(warningIcon.getImage(), message, fontSize, separatorAfter, helpId);
        }

        @Override
        public Pane getParamPane(final PluginParametersNode node) {
            if (node.getChildren().isEmpty()) {
                return null;
            }
            if (currentChild == -1) {
                warningLabel = new Label(message);
                warningLabel.setFont(Font.font(Font.getDefault().getFamily(), FontPosture.ITALIC, fontSize));
                warningLabel.setWrapText(true);
                warningLabel.setAlignment(Pos.CENTER);
                final HBox warning = new HBox(warningIcon, warningLabel);
                warning.setStyle("-fx-border-color: red");
                return new VBox(warning);
            }
            final Pane paramPane = super.getParamPane(node);
            final SimpleDoubleProperty updated = new SimpleDoubleProperty();
            updated.bind(Bindings.max(maxParamWidth, paramPane.widthProperty()));
            maxParamWidth = updated;
            warningLabel.prefWidthProperty().bind(maxParamWidth);
            return paramPane;
        }

        @Override
        public Button getParamHelp(final PluginParametersNode node) {
            if (currentChild == -1 && helpId != null) {
                return buildHelpButton(helpId, String.format("group '%s'", message));
            }
            return super.getParamHelp(node);
        }
    }

    /**
     * Returns the widget (with associated label and help) corresponding to a
     * leaf node that contains a single {@link PluginParameter}. Does nothing
     * for nodes that are not leaf nodes.
     */
    public static class SingleParameterLayout extends ParameterLayout {

        private final ParameterConstructor constructor;

        public SingleParameterLayout(final ParameterConstructor constructor) {
            this.constructor = constructor;
        }

        @Override
        public ParameterLayout copy() {
            return new SingleParameterLayout(constructor);
        }

        @Override
        public Pane getParamPane(final PluginParametersNode node) {
            if (node.isLeaf()) {
                return constructor.buildParameterPane(node.getParameter());
            }
            return null;
        }

        @Override
        public LabelDescriptionBox getParamLabel(final PluginParametersNode node) {
            if (node.isLeaf() && node.getParameter().getType().requiresLabel()) {
                return constructor.buildParameterLabel(node.getParameter());
            }
            return null;
        }

        @Override
        public Button getParamHelp(final PluginParametersNode node) {
            if (node.isLeaf() && node.getParameter().getHelpID() != null) {
                return constructor.buildParameterHelp(node.getParameter());
            }
            return null;
        }

    }

    /**
     * Iterates through all child nodes and adds their constructed GUIs to an
     * HBox. Returns this HBox.
     */
    public static class HorizontalParameterGroupLayout extends ParameterLayout {

        private static final List<String> SHOULD_NOT_EXPAND = Arrays.asList(
                StringParameterType.ID,
                SingleChoiceParameterType.ID,
                FileParameterType.ID,
                DateTimeRangeParameterType.ID,
                MultiChoiceParameterType.ID,
                ParameterListParameterType.ID,
                LocalDateParameterType.ID,
                ActionParameterType.ID
        );
        private GridPane paramGroupGridPane;
        private int currentCol;
        private final boolean shouldHGrow;

        @Override
        public ParameterLayout copy() {
            return new HorizontalParameterGroupLayout();
        }

        public HorizontalParameterGroupLayout() {
            this(true);
        }

        public HorizontalParameterGroupLayout(final boolean shouldHGrow) {
            this.shouldHGrow = shouldHGrow;
        }

        private void addElements(final PluginParametersNode node, final Region... elements) {
            final HBox singleParam = new HBox();
            singleParam.setSpacing(10);
            for (final Region element : elements) {
                if (element != null) {
                    singleParam.getChildren().addAll(element);
                }
            }
            final ColumnConstraints paramConstraints = new ColumnConstraints();
            if (!SHOULD_NOT_EXPAND.contains(node.getParameter().getType().getId())) {
                singleParam.setMinWidth(USE_PREF_SIZE);
            }
            paramConstraints.minWidthProperty().bind(singleParam.minWidthProperty());
            paramGroupGridPane.getColumnConstraints().add(paramConstraints);
            paramGroupGridPane.add(singleParam, currentCol++, 0);
        }

        @Override
        public Pane getParamPane(final PluginParametersNode node) {
            if (node.getChildren().isEmpty()) {
                return null;
            }
            currentCol = 0;
            paramGroupGridPane = new GridPane();

            final PluginParametersNode firstChild = node.getChildren().get(0);
            final Pane firstPane = firstChild.getFormatter().getParamPane(firstChild);
            final Button firstButton = firstChild.getFormatter().getParamHelp(firstChild);
            addElements(firstChild, firstPane, firstButton);
            HBox.setHgrow(firstPane, shouldHGrow ? Priority.ALWAYS : Priority.NEVER);
            if (firstButton != null) {
                HBox.setMargin(firstButton, new Insets(0, PADDING, 0, 0));
            }

            for (final PluginParametersNode child : node.getChildren().subList(1, node.getChildren().size() - 1)) {
                final LabelDescriptionBox label = child.getFormatter().getParamLabel(child);
                label.setStyle("-fx-label-padding: " + -PADDING);
                final Pane paramPane = child.getFormatter().getParamPane(child);
                final Button paramHelp = child.getFormatter().getParamHelp(child);
                label.bindDescriptionToLabel();
                label.setMinWidth(USE_PREF_SIZE);
                addElements(child, label, paramPane, paramHelp);
                HBox.setHgrow(paramPane, shouldHGrow ? Priority.ALWAYS : Priority.NEVER);
                if (paramHelp != null) {
                    HBox.setMargin(paramHelp, new Insets(0, PADDING, 0, 0));
                }
            }

            final PluginParametersNode lastChild = node.getChildren().get(node.getChildren().size() - 1);
            final LabelDescriptionBox lastLabel = lastChild.getFormatter().getParamLabel(lastChild);
            lastLabel.setStyle("-fx-label-padding: " + -PADDING);
            lastLabel.bindDescriptionToLabel();
            lastLabel.setMinWidth(USE_PREF_SIZE);
            final Pane lastPane = lastChild.getFormatter().getParamPane(lastChild);
            addElements(lastChild, lastLabel, lastPane);
            HBox.setHgrow(lastPane, shouldHGrow ? Priority.ALWAYS : Priority.NEVER);
            return paramGroupGridPane;
        }

        @Override
        public LabelDescriptionBox getParamLabel(final PluginParametersNode node) {
            if (node.getChildren().isEmpty()) {
                return null;
            }

            final PluginParametersNode firstChild = node.getChildren().get(0);
            return firstChild.getFormatter().getParamLabel(firstChild);
        }

        @Override
        public Button getParamHelp(final PluginParametersNode node) {
            if (node.getChildren().isEmpty()) {
                return null;
            }
            final PluginParametersNode lastChild = node.getChildren().get(node.getChildren().size() - 1);
            return lastChild.getFormatter().getParamHelp(lastChild);
        }
    }

    /**
     * Constructs the entire Pane for a {@link PluginParameters} object. Should
     * be used as the layout for the root node of this object only.
     */
    public static class ParameterPaneLayout extends ParameterLayout {

        @Override
        public ParameterLayout copy() {
            return new ParameterPaneLayout();
        }

        @Override
        public boolean supportsChildrenWithMultipleElements() {
            return true;
        }

        @Override
        public Pane getParamPane(final PluginParametersNode node) {
            if (node.getChildren().isEmpty()) {
                return null;
            }

            final GridPane paramGroupPane = new PluginParametersPane();
            paramGroupPane.setMinHeight(0);
            GridPane.setHalignment(paramGroupPane, HPos.LEFT);
            paramGroupPane.setPadding(Insets.EMPTY);
            
            int row = 0;
            final DoubleProperty descriptionWidth = new SimpleDoubleProperty();
            DoubleProperty maxLabelWidth = new SimpleDoubleProperty();

            for (final PluginParametersNode child : node.getChildren()) {
                while (child.getFormatter().nextElement(child)) {
                    final RowConstraints rowConstraints = new RowConstraints();
                    rowConstraints.setVgrow(Priority.NEVER);
                    rowConstraints.setFillHeight(false);
                    paramGroupPane.getRowConstraints().addAll(rowConstraints);

                    final LabelDescriptionBox label = child.getFormatter().getParamLabel(child);
                    if (label != null) {
                        paramGroupPane.add(label, 0, row);
                        GridPane.setValignment(label, VPos.TOP);
                        GridPane.setHgrow(label, Priority.ALWAYS);
                        GridPane.setFillHeight(label, false);

                        maxLabelWidth = label.updateBindingWithLabelWidth(maxLabelWidth);
                    }

                    final Pane paramPane = child.getFormatter().getParamPane(child);
                    if (paramPane != null) {
                        final VBox cell = new VBox(paramPane);
                        cell.setAlignment(Pos.CENTER_LEFT);
                        cell.setStyle("-fx-padding: " + PADDING);
                        GridPane.setFillHeight(cell, true);
                        if (label == null) {
                            paramGroupPane.add(cell, 0, row, 2, 1);
                        } else {
                            paramGroupPane.add(cell, 1, row);
                        }
                    }

                    final Button paramHelp = child.getFormatter().getParamHelp(child);
                    if (paramHelp != null) {
                        paramGroupPane.add(paramHelp, 2, row);
                        GridPane.setMargin(paramHelp, new Insets(PADDING, PADDING, 0, 0));
                        GridPane.setValignment(paramHelp, VPos.TOP);
                        GridPane.setFillHeight(paramHelp, false);
                    }

                    row++;
                }
            }

            descriptionWidth.bind(Bindings.max(50, maxLabelWidth));
            return paramGroupPane;
        }

        @Override
        public LabelDescriptionBox getParamLabel(final PluginParametersNode node) {
            return null;
        }

        @Override
        public Button getParamHelp(final PluginParametersNode node) {
            return null;
        }
    }

    public static final double PADDING = 5;
    private int displayedParams = 0;

    /**
     * Constructor the Pane for the given set of parameters.
     *
     * @param parameters The {@link PluginParameters} object to construct the
     * pane for.
     * @param top A {@link PluginParametersPaneListener} which will be informed
     * of changes to the validity of the parameter values set from the
     * constructed pane.
     * @return the new PluginParametersPane.
     */
    public static PluginParametersPane buildPane(final PluginParameters parameters, final PluginParametersPaneListener top) {
        return buildPane(parameters, top, null);
    }

    /**
     * Constructor the Pane for the given set of parameters, skipping the layout
     * for parameters which are in the excluded list.
     *
     * @param parameters The {@link PluginParameters} object to construct the
     * pane for.
     * @param top A {@link PluginParametersPaneListener} which will be informed
     * of changes to the validity of the parameter values set from the
     * constructed pane.
     * @param excludedParameters A Set of labels of the parameters to be
     * excluded from this pane.
     * @return the new PluginParametersPane.
     */
    public static PluginParametersPane buildPane(final PluginParameters parameters, final PluginParametersPaneListener top, final Set<String> excludedParameters) {
        int displayedParams = 0;
        PluginParametersPane pane = null;
        if (parameters != null) {
            final PluginParametersNode root = parameters.getRootNode();
            final ParameterConstructor parameterConstructor = new PluginParameterPaneConstructor(top);
            // Reset all formatting allowing a new layout to occur (in case we have
            // previously built a pane for this PluginParameters object).
            root.resetFormatting();
            // Set the formatter for the root to buildId a parameter pane
            root.setFormatter(new ParameterPaneLayout());
            // Set the formatter appropriately for all leaf nodes
            for (final PluginParametersNode node : root.getLeaves()) {
                if (excludedParameters != null && excludedParameters.contains(node.name)) {
                    node.setFormatter(new NullLayout());
                } else {
                    node.setFormatter(new SingleParameterLayout(parameterConstructor));
                    displayedParams++;
                }
            }
            pane = (PluginParametersPane) root.getFormatter().getParamPane(root);
        }
        if (pane == null) {
            pane = new PluginParametersPane();
        }
        pane.displayedParams = displayedParams;
        return pane;
    }

    public int getNumberDisplayedParams() {
        return displayedParams;
    }

    private interface ParameterConstructor {

        public LabelDescriptionBox buildParameterLabel(final PluginParameter<?> parameter);

        public Pane buildParameterPane(final PluginParameter<?> parameter);

        public Button buildParameterHelp(final PluginParameter<?> parameter);
    }

    private static class LabelDescriptionBox extends VBox {

        public final Label label;
        public final Label description;

        public LabelDescriptionBox(final Label label, final Label description) {
            this.label = label;
            this.description = description;
            getChildren().addAll(label, description);
            this.setPadding(new Insets(5));
        }

        public void bindDescriptionToLabel() {
            description.maxWidthProperty().bind(Bindings.max(50, label.widthProperty()));
        }

        public DoubleProperty updateBindingWithLabelWidth(final DoubleProperty property) {
            final DoubleProperty newBinding = new SimpleDoubleProperty();
            newBinding.bind(Bindings.max(property, label.widthProperty()));
            return newBinding;
        }
    }

    private static Button buildHelpButton(final String helpId, final String helpForLabel) {
        final Button helpButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor())));
        helpButton.paddingProperty().set(HELP_INSETS);
        helpButton.setTooltip(new Tooltip(String.format("Display help for %s", helpForLabel)));
        helpButton.setOnAction(event -> {
            if (!new HelpCtx(helpId).display()) {
                StatusDisplayer.getDefault().setStatusText(String.format("Help for %s not found.", helpForLabel));
            }
        });

        // Get rid of the ugly button look so the icon stands alone.
        helpButton.setStyle("-fx-border-color: transparent; -fx-background-color: transparent; -fx-effect: null; ");
        return helpButton;
    }

    private static class PluginParameterPaneConstructor implements ParameterConstructor {

        private int validParams = 0;
        private final PluginParametersPaneListener top;

        public PluginParameterPaneConstructor(final PluginParametersPaneListener top) {
            this.top = top;
        }

        @Override
        public Button buildParameterHelp(final PluginParameter<?> parameter) {
            return buildHelpButton(parameter.getHelpID(), String.format("parameter '%s'", parameter.getName()));
        }

        @Override
        public final LabelDescriptionBox buildParameterLabel(final PluginParameter<?> parameter) {
            final StringBuilder labelBuilder = new StringBuilder(parameter.getName());
            if (parameter.isRequired()) {
                labelBuilder.append("*");
            }
            final Label label = new Label(labelBuilder.toString());
            final Label description = new Label(parameter.getDescription());
            label.setMinWidth(145);
            label.setWrapText(true);
            label.setStyle("-fx-font-weight: bold");
            description.setId("smallInfoText"); 
            description.getStyleClass().add("description-label");
            description.setWrapText(true);
            description.setStyle("-fx-font-size: smaller"); 
            final LabelDescriptionBox labels = new LabelDescriptionBox(label, description);
            labels.setVisible(parameter.isVisible());
            labels.setManaged(parameter.isVisible());
            parameter.setVisible(parameter.isVisible());
            linkParameterLabelToTop(parameter, labels);
            return labels;
        }

        public void parameterHasChanged() {
            if (top != null) {
                top.validityChanged(validParams >= 0);
            }
        }

        public void linkParameterLabelToTop(final PluginParameter<?> parameter, final LabelDescriptionBox ldb) {
            parameter.addListener((parameter1, change) -> {
                switch (change) {
                    case NAME -> ldb.label.setText(parameter1.getName());
                    case DESCRIPTION -> ldb.description.setText(parameter1.getDescription());
                    case VISIBLE -> {
                        ldb.setManaged(parameter.isVisible());
                        ldb.setVisible(parameter.isVisible());
                    }
                    default -> {
                        // do nothing
                    }
                }
            });
        }

        public void linkParameterWidgetToTop(final PluginParameter<?> parameter) {
            updateTop(parameter);
            
            parameter.addListener((parameter1, change) -> {
                switch (change) {
                    case ERROR -> {
                        if (parameter1.getError() == null) {
                            validParams++;
                        } else {
                            validParams--;
                        }
                        Platform.runLater(this::parameterHasChanged);
                    }
                    case VALUE -> Platform.runLater(this::parameterHasChanged);
                    default -> {
                        // do nothing
                    }
                }
                updateTop(parameter);
            });
        }
        
        /**
         * Notifies the listener of conditions relevant to the plugin is class.
         * Top notified of changes in validity of parameters contained with the plugin parameters pane. 
         * @param parameter 
         */
        private void updateTop(final PluginParameter<?> parameter){  
            if (parameter != null & top != null){
                if ((parameter.isRequired() && StringUtils.isBlank(parameter.getStringValue())) || parameter.getError() != null){
                    top.notifyParameterValidityChange(parameter, false);
                } else {
                    top.notifyParameterValidityChange(parameter, true);
                }
            }
        }
            
        @Override
        @SuppressWarnings("unchecked") //All casts in this method are checked prior to casting.
        public final Pane buildParameterPane(final PluginParameter<?> parameter) {
            final String id = parameter.getType().getId();

            final Pane pane;
            switch (id) {
                case StringParameterType.ID -> pane = new ValueInputPane((PluginParameter<StringParameterValue>) parameter, StringParameterType.getLines((PluginParameter<StringParameterValue>) parameter));
                case IntegerParameterType.ID -> pane = new NumberInputPane<Integer>((PluginParameter<NumberParameterValue>) parameter);
                case FloatParameterType.ID -> pane = new NumberInputPane<Float>((PluginParameter<NumberParameterValue>) parameter);
                case BooleanParameterType.ID -> pane = new BooleanInputPane((PluginParameter<BooleanParameterValue>) parameter);
                case SingleChoiceParameterType.ID -> pane = new SingleChoiceInputPane((PluginParameter<SingleChoiceParameterValue>) parameter);
                case ColorParameterType.ID -> pane = new ColorInputPane((PluginParameter<ColorParameterValue>) parameter);
                case DateTimeRangeParameterType.ID -> pane = new DateTimeRangeInputPane((PluginParameter<DateTimeRangeParameterValue>) parameter);
                case FileParameterType.ID -> pane = new FileInputPane((PluginParameter<FileParameterValue>) parameter);
                case LocalDateParameterType.ID -> pane = new LocalDateInputPane((PluginParameter<LocalDateParameterValue>) parameter);
                case MultiChoiceParameterType.ID -> pane = new MultiChoiceInputPane((PluginParameter<MultiChoiceParameterValue>) parameter);
                case ParameterListParameterType.ID -> pane = new ParameterListInputPane((PluginParameter<ParameterListParameterValue>) parameter);
                case ActionParameterType.ID -> pane = new ActionInputPane(parameter);
                case PasswordParameterType.ID -> pane = new PasswordInputPane((PluginParameter<PasswordParameterValue>) parameter);
                default -> throw new IllegalArgumentException("Unsupported parameter type ID: " + id);
            }

            linkParameterWidgetToTop(parameter);
            return pane;
        }
    }
}
