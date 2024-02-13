/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.wordcloud.ui;

import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPaneListener;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.wordcloud.phraseanalysis.PhrasiphyContentParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openide.util.HelpCtx;

/**
 *
 * @author twilight_sparkle
 */
public class WordCloudParametersPane extends TitledPane implements PluginParametersPaneListener {

    private final PluginParameters params;
    private final Button run;
    private static final String EMPTY_STRING = "";
    private static final List<String> EMPTY_STRING_LIST = Arrays.asList(EMPTY_STRING);
    private List<String> nodeAttributes = new ArrayList<>();
    private List<String> transAttributes = new ArrayList<>();
    private static final Insets HELP_PADDING = new Insets(2, 0, 0, 0);

    public WordCloudParametersPane(final WordCloudPane master) {
        setText("Generate Word Cloud");
        setExpanded(true);
        setCollapsible(true);

        params = new PluginParameters();
        final PhrasiphyContentParameters phrasiphyContentParams = PhrasiphyContentParameters.getDefaultParameters();

        final PluginParameter<SingleChoiceParameterValue> elementType = SingleChoiceParameterType.build(PhrasiphyContentParameters.ELEMENT_TYPE_PARAMETER_ID);
        elementType.setName(PhrasiphyContentParameters.ELEMENT_TYPE_NAME);
        elementType.setDescription(PhrasiphyContentParameters.ELEMENT_TYPE_DESCRIPTION);
        SingleChoiceParameterType.setOptions(elementType, PhrasiphyContentParameters.ELEMENT_TYPE_CHOICES);
        SingleChoiceParameterType.setChoice(elementType, PhrasiphyContentParameters.ELEMENT_TYPE_DEFAULT);
        params.addParameter(elementType);

        final PluginParameter<SingleChoiceParameterValue> attrToAnalyse = SingleChoiceParameterType.build(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_PARAMETER_ID);
        attrToAnalyse.setName(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_NAME);
        attrToAnalyse.setDescription(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DESCRIPTION);
        SingleChoiceParameterType.setOptions(attrToAnalyse, EMPTY_STRING_LIST);
        SingleChoiceParameterType.setChoice(attrToAnalyse, EMPTY_STRING);
        params.addParameter(attrToAnalyse);

        final PluginParameter<IntegerParameterValue> phraseLength = IntegerParameterType.build(PhrasiphyContentParameters.PHRASE_LENGTH_PARAMETER_ID);
        phraseLength.setName(PhrasiphyContentParameters.PHRASE_LENGTH_NAME);
        phraseLength.setDescription(PhrasiphyContentParameters.PHRASE_LENGTH_DESCRIPTION);
        phraseLength.setStringValue(Integer.toString(phrasiphyContentParams.getPhraseLength()));
        IntegerParameterType.setMinimum(phraseLength, PhrasiphyContentParameters.PHRASE_LENGTH_MIN_VALUE);
        IntegerParameterType.setMaximum(phraseLength, PhrasiphyContentParameters.PHRASE_LENGTH_MAX_VALUE);
        params.addParameter(phraseLength);

        final PluginParameter<IntegerParameterValue> proximity = IntegerParameterType.build(PhrasiphyContentParameters.PROXIMITY_PARAMETER_ID);
        proximity.setName(PhrasiphyContentParameters.PROXIMITY_NAME);
        proximity.setDescription(PhrasiphyContentParameters.PROXIMITY_DESCRIPTION);
        proximity.setStringValue(Integer.toString(phrasiphyContentParams.getProximity()));
        IntegerParameterType.setMinimum(proximity, PhrasiphyContentParameters.PROXIMITY_MIN_VALUE);
        IntegerParameterType.setMaximum(proximity, PhrasiphyContentParameters.PROXIMITY_MAX_VALUE);
        params.addParameter(proximity);

        final PluginParameter<IntegerParameterValue> threshold = IntegerParameterType.build(PhrasiphyContentParameters.THRESHOLD_PARAMETER_ID);
        threshold.setName(PhrasiphyContentParameters.THRESHOLD_NAME);
        threshold.setDescription(PhrasiphyContentParameters.THRESHOLD_DESCRIPTION);
        threshold.setStringValue(Integer.toString(phrasiphyContentParams.getThreshold()));
        IntegerParameterType.setMinimum(threshold, PhrasiphyContentParameters.THRESHOLD_MIN_VALUE);
        params.addParameter(threshold);

        final PluginParameter<FileParameterValue> backgroundFile = FileParameterType.build(PhrasiphyContentParameters.BACKGROUND_PARAMETER_ID);
        backgroundFile.setName(PhrasiphyContentParameters.BACKGROUND_NAME);
        backgroundFile.setDescription(PhrasiphyContentParameters.BACKGROUND_DESCRIPTION);
        backgroundFile.setStringValue(EMPTY_STRING);
        params.addParameter(backgroundFile);

        final PluginParameter<SingleChoiceParameterValue> backgroundFilter = SingleChoiceParameterType.build(PhrasiphyContentParameters.BACKGROUND_FILTER_PARAMETER_ID);
        backgroundFilter.setName(PhrasiphyContentParameters.BACKGROUND_FILTER_NAME);
        backgroundFilter.setDescription(PhrasiphyContentParameters.BACKGROUND_FILTER_DESCRIPTION);
        SingleChoiceParameterType.setOptions(backgroundFilter, PhrasiphyContentParameters.BACKGROUND_FILTER_CHOICES);
        SingleChoiceParameterType.setChoice(backgroundFilter, PhrasiphyContentParameters.BACKGROUND_FILTER_DEFAULT);
        params.addParameter(backgroundFilter);

        params.addController(PhrasiphyContentParameters.ELEMENT_TYPE_PARAMETER_ID, (PluginParameter<?> masterParameter, Map<String, PluginParameter<?>> parameters, ParameterChange change) -> {
            @SuppressWarnings("unchecked") // ATTRIBUTE_TO_ANALYSE_PARAMETER is always of type SingleChoiceParameter
            final PluginParameter<SingleChoiceParameterValue> attrParam = (PluginParameter<SingleChoiceParameterValue>) parameters.get(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_PARAMETER_ID);
            if (change == ParameterChange.VALUE) {
                if ("transaction".equals(masterParameter.getStringValue())) {
                    if (transAttributes.contains(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_TRANSACTIONS)) {
                        attrParam.setStringValue(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_TRANSACTIONS);
                    } else {
                        attrParam.setStringValue(EMPTY_STRING);
                    }
                    SingleChoiceParameterType.setOptions(attrParam, transAttributes);
                } else if ("node".equals(masterParameter.getStringValue())) {
                    if (nodeAttributes.contains(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_NODES)) {
                        attrParam.setStringValue(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_NODES);
                    } else {
                        attrParam.setStringValue(EMPTY_STRING);
                    }
                    SingleChoiceParameterType.setOptions(attrParam, nodeAttributes);
                }
            }
        });

        params.addController(PhrasiphyContentParameters.PHRASE_LENGTH_PARAMETER_ID, (PluginParameter<?> masterParameter, Map<String, PluginParameter<?>> parameters, ParameterChange change) -> {
            @SuppressWarnings("unchecked") // PROXIMITY_PARAMETER is always of type IntegerParameter 
            final PluginParameter<IntegerParameterValue> proximityParam = (PluginParameter<IntegerParameterValue>) parameters.get(PhrasiphyContentParameters.PROXIMITY_PARAMETER_ID);
            if (change == ParameterChange.VALUE) {
                if (masterParameter.getError() != null) {
                    IntegerParameterType.setMinimum(proximityParam, 0);
                } else {
                    final int currentPhraseLength = Integer.parseInt(masterParameter.getStringValue());
                    IntegerParameterType.setMinimum(proximityParam, currentPhraseLength);
                    if (Integer.parseInt(proximityParam.getStringValue()) < currentPhraseLength) {
                        proximityParam.setStringValue(masterParameter.getStringValue());
                    }
                }
            }
        });

        final VBox content = new VBox();
        final HBox buttonBox = new HBox();
        run = new Button("Generate");
        run.setOnMouseClicked(event -> master.runPlugin(params));
        final Button helpButton = createHelpButton();
        buttonBox.getChildren().add(run);
        buttonBox.getChildren().add(helpButton);

        final PluginParametersPane pluginParametersPane = PluginParametersPane.buildPane(params, this, null);
        content.getChildren().add(pluginParametersPane);
        content.getChildren().add(buttonBox);
        setContent(content);
    }
    
    public void updateParameters(final List<String> nodeAttributes, final List<String> transAttributes) {
        this.nodeAttributes = new ArrayList<>(nodeAttributes);
        this.transAttributes = new ArrayList<>(transAttributes);
        @SuppressWarnings("unchecked") // ELEMENT_TYPE_PARAMETER is always of type SingleChoiceParameter
        final PluginParameter<SingleChoiceParameterValue> elParam = (PluginParameter<SingleChoiceParameterValue>) params.getParameters().get(PhrasiphyContentParameters.ELEMENT_TYPE_PARAMETER_ID);
        @SuppressWarnings("unchecked") // ATTRIBUTE_TO_ANALYSE_PARAMETER is always of type SingleChoiceParameter 
        final PluginParameter<SingleChoiceParameterValue> attrParam = (PluginParameter<SingleChoiceParameterValue>) params.getParameters().get(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_PARAMETER_ID);

        if ("transaction".equals(elParam.getStringValue())) {
            if (transAttributes.contains(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_TRANSACTIONS)) {
                attrParam.setStringValue(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_TRANSACTIONS);
            } else {
                attrParam.setStringValue(EMPTY_STRING);
            }
            SingleChoiceParameterType.setOptions(attrParam, this.transAttributes);
        } else if ("node".equals(elParam.getStringValue())) {
            if (nodeAttributes.contains(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_NODES)) {
                attrParam.setStringValue(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_DEFAULT_NODES);
            } else {
                attrParam.setStringValue(EMPTY_STRING);
            }
            SingleChoiceParameterType.setOptions(attrParam, this.nodeAttributes);
        }
    }
    
    protected Button getRun() {
        return run;
    }
    
    protected List<String> getNodeAttributes() {
        return new ArrayList<>(nodeAttributes);
    }
    
    protected List<String> getTransAttributes() {
        return new ArrayList<>(transAttributes);
    }

    @Override
    public void validityChanged(final boolean enabled) {
        run.setDisable(!enabled);
    }

    @Override
    public void hierarchicalUpdate() {
         // Does nothing as not required
    }

    public void setAttributeSelectionEnabled(final boolean val) {
        params.getParameters().get(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_PARAMETER_ID).setEnabled(val);
    }

    public PluginParameters getParams() {
        return params;
    }
     
    public static Button createHelpButton() {
        final Button helpDocumentationButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor())));
        helpDocumentationButton.paddingProperty().set(HELP_PADDING);
        helpDocumentationButton.setTooltip(new Tooltip("Display help for Word Cloud View"));
        helpDocumentationButton.setOnAction(event -> new HelpCtx(WordCloudTopComponent.class.getName()).display());

        // Get rid of the ugly button look so the icon stands alone.
        helpDocumentationButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent; -fx-effect: null; ");

        return helpDocumentationButton;
    }
    
    @Override
    public void notifyParameterValidityChange(final PluginParameter<?> parameter, final boolean currentlySatisfied) {
        // Must be overriden to implement PluginParametersPaneListener
    }
}
