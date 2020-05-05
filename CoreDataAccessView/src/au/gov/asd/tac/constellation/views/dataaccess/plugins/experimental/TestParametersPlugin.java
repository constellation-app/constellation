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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.experimental;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ActionParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType.LocalDateParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.PasswordParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.state.DataAccessPreferenceKeys;
import au.gov.asd.tac.constellation.views.dataaccess.templates.QueryNameValidator;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * A data access plugin that tests the various types of parameters (including
 * the global parameters).
 * <p>
 * Each of the parameter types should have a matching GUI input.
 *
 * @author algol
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)
})
@Messages("TestParametersPlugin=Test Parameters")
public class TestParametersPlugin extends RecordStoreQueryPlugin implements DataAccessPlugin {

    private static final Logger LOGGER = Logger.getLogger(TestParametersPlugin.class.getName());

    public static final String SELECTED_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "selected");
    public static final String TEST1_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "test1");
    public static final String TEST2_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "test2");
    public static final String PASSWORD_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "password");
    public static final String LOCAL_DATE_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "local_date");
    public static final String ELEMENT_TYPE_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "element_type");
    public static final String ROBOT_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "robot");
    public static final String REFRESH_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "refresh");
    public static final String PLANETS_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "planets");
    public static final String DICE_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "dice");
    public static final String PROBABILITY_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "probability");
    public static final String INPUT_FILE_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "input_file");
    public static final String OUTPUT_FILE_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "output_file");
    public static final String COLOR_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "color");
    public static final String CRASH_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "crash");
    public static final String INTERACTION_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "interaction");
    public static final String LEVEL_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "level");
    public static final String SLEEP_PARAMETER_ID = PluginParameter.buildId(TestParametersPlugin.class, "sleep");
    
    //Debug Levels
    private static final String NONE = "None";
    private static final String DEBUG = "Debug";
    private static final String INFO = "Info";
    private static final String WARNING = "Warning";
    private static final String ERROR = "Error";
    private static final String FATAL = "Fatal";

    private final SecureRandom r = new SecureRandom();

    @StaticResource
    private static final String ALIEN_ICON = "au/gov/asd/tac/constellation/views/dataaccess/plugins/experimental/resources/alien.png";

    public TestParametersPlugin() {
        addValidator(new QueryNameValidator());
    }

    @Override
    public String getType() {
        return DataAccessPluginCoreType.DEVELOPER;
    }

    @Override
    public int getPosition() {
        return Integer.MAX_VALUE - 10;
    }

    @Override
    public String getDescription() {
        return "Test the various input UIs";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();
        final String css = TestParametersPlugin.class.getResource("resources/test.css").toExternalForm();

        final BooleanParameterValue selectedpv = new BooleanParameterValue(true);
        selectedpv.setGuiInit(control -> {
            final CheckBox field = (CheckBox) control;
            field.getStylesheets().add(css);
        });
        final PluginParameter<BooleanParameterValue> selected = BooleanParameterType.build(SELECTED_PARAMETER_ID, selectedpv);
        selected.setName("Use selected");
        selected.setDescription("Only use selected elements");
        params.addParameter(selected);

        final PluginParameter<StringParameterValue> queryName = StringParameterType.build(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID);
        params.addParameter(queryName);

        final PluginParameter<DateTimeRangeParameterValue> dt = DateTimeRangeParameterType.build(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID);
        params.addParameter(dt);

        final PluginParameter<StringParameterValue> string1 = StringParameterType.build(TEST1_PARAMETER_ID);
        string1.setName("Test1");
        string1.setDescription("A test string");
        string1.setStringValue("Plugh.");
        params.addParameter(string1);

        final StringParameterValue string2pv = new StringParameterValue();
        string2pv.setGuiInit(control -> {
            final TextArea field = (TextArea) control;
            field.getStylesheets().add(css);
        });
        final PluginParameter<StringParameterValue> string2 = StringParameterType.build(TEST2_PARAMETER_ID, string2pv);
        string2.setName("Test2");
        string2.setDescription("A two line test string");
        StringParameterType.setLines(string2, 2);
        params.addParameter(string2);

        final PluginParameter<PasswordParameterValue> passwd = PasswordParameterType.build(PASSWORD_PARAMETER_ID);
        passwd.setName("Password");
        passwd.setDescription("Everyone needs a password");
        params.addParameter(passwd);

        final PluginParameter<LocalDateParameterValue> date = LocalDateParameterType.build(LOCAL_DATE_PARAMETER_ID);
        date.setName("Date");
        date.setDescription("Pick a day");
        date.setLocalDateValue(LocalDate.of(2001, Month.JANUARY, 1));
        params.addParameter(date);

        final List<GraphElementTypeParameterValue> elementTypeOptions = new ArrayList<>();
        for (final GraphElementType elementType : GraphElementType.values()) {
            elementTypeOptions.add(new GraphElementTypeParameterValue(elementType));
        }
        final PluginParameter<SingleChoiceParameterValue> elementType = SingleChoiceParameterType.build(ELEMENT_TYPE_PARAMETER_ID, GraphElementTypeParameterValue.class);
        elementType.setName("Graph element type");
        elementType.setDescription("Graph element type");
        SingleChoiceParameterType.setOptionsData(elementType, elementTypeOptions);
        params.addParameter(elementType);

        // A single choice list with a subtype of String.
        final SingleChoiceParameterValue robotpv = new SingleChoiceParameterValue(StringParameterValue.class);
        robotpv.setGuiInit(control -> {
            @SuppressWarnings("unchecked") //control will be of type ComboBox<ParameterValue> which extends from Region
            final ComboBox<ParameterValue> field = (ComboBox<ParameterValue>) control;
            final Image img = new Image(ALIEN_ICON);
            field.setCellFactory((ListView<ParameterValue> param) -> {
                return new ListCell<ParameterValue>() {
                    @Override
                    protected void updateItem(final ParameterValue item, final boolean empty) {
                        super.updateItem(item, empty);
                        this.setText(empty ? "" : item.toString());
                        final float f = empty ? 0 : item.toString().length() / 11f;
                        final Color c = Color.color(1 - f / 2f, 0, 0);
                        setTextFill(c);
                        setGraphic(new ImageView(img));
                    }
                };
            });
        });
        final PluginParameter<SingleChoiceParameterValue> robotOptions = SingleChoiceParameterType.build(ROBOT_PARAMETER_ID, robotpv);
        robotOptions.setName("Robot options");
        robotOptions.setDescription("A list of robots to choose from");

        // Use the helper method to add string options.
        SingleChoiceParameterType.setOptions(robotOptions, Arrays.asList("Bender", "Gort", "Maximillian", "Robbie", "Tom Servo"));

        // Create a ParameterValue of the underlying type (in this case, String) to set the default choice.
        final StringParameterValue robotChoice = new StringParameterValue("Gort");
        SingleChoiceParameterType.setChoiceData(robotOptions, robotChoice);
        params.addParameter(robotOptions);

        final PluginParameter<ParameterValue> buttonParam = ActionParameterType.build(REFRESH_PARAMETER_ID);
        buttonParam.setName("Refresh");
        buttonParam.setDescription("Update the available robots");
        params.addParameter(buttonParam);

        final PluginParameter<MultiChoiceParameterValue> planetOptions = MultiChoiceParameterType.build(PLANETS_PARAMETER_ID);
        planetOptions.setName("Planets");
        planetOptions.setDescription("Some planets");
        MultiChoiceParameterType.setOptions(planetOptions, Arrays.asList("Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Coruscant"));
        final List<String> checked = new ArrayList<>();
        checked.add("Earth");
        MultiChoiceParameterType.setChoices(planetOptions, checked);
        params.addParameter(planetOptions);

        final PluginParameter<IntegerParameterValue> diceOptions = IntegerParameterType.build(DICE_PARAMETER_ID);
        diceOptions.setName("Dice");
        diceOptions.setDescription("2d6");
        IntegerParameterType.setMinimum(diceOptions, 2);
        IntegerParameterType.setMaximum(diceOptions, 12);
        diceOptions.setIntegerValue(7);
        params.addParameter(diceOptions);

        final PluginParameter<FloatParameterValue> probability = FloatParameterType.build(PROBABILITY_PARAMETER_ID);
        probability.setName("Probability");
        probability.setDescription("0 ≤ p ≤ 1");
        FloatParameterType.setMinimum(probability, 0f);
        FloatParameterType.setMaximum(probability, 1f);
        FloatParameterType.setStep(probability, 0.1f);
        probability.setFloatValue(1f);
        params.addParameter(probability);

        final PluginParameter<FileParameterValue> openFileParam = FileParameterType.build(INPUT_FILE_PARAMETER_ID);
        openFileParam.setName("Input file");
        openFileParam.setDescription("A file to read stuff from");
        params.addParameter(openFileParam);

        final PluginParameter<FileParameterValue> saveFileParam = FileParameterType.build(OUTPUT_FILE_PARAMETER_ID);
        saveFileParam.setName("Output file");
        saveFileParam.setDescription("A file to write stuff to");
        FileParameterType.setKind(saveFileParam, FileParameterType.FileParameterKind.SAVE);
        FileParameterType.setFileFilters(saveFileParam, new FileChooser.ExtensionFilter("Text files", "*.txt"));
        params.addParameter(saveFileParam);

        final PluginParameter<ColorParameterValue> color = ColorParameterType.build(COLOR_PARAMETER_ID);
        color.setName("Color");
        color.setDescription("Your favourite colour");
        color.setColorValue(ConstellationColor.BLUE);
        params.addParameter(color);

        final PluginParameter<BooleanParameterValue> crash = BooleanParameterType.build(CRASH_PARAMETER_ID);
        crash.setName("Crash");
        crash.setDescription("Simulate plugin failure");
        params.addParameter(crash);

        final PluginParameter<SingleChoiceParameterValue> interactionOptions = SingleChoiceParameterType.build(INTERACTION_PARAMETER_ID);
        interactionOptions.setName("Interaction level");
        interactionOptions.setDescription("Interaction level for some interaction with the user");
        SingleChoiceParameterType.setOptions(interactionOptions, Arrays.asList(NONE, DEBUG, INFO, WARNING, ERROR, FATAL));
        params.addParameter(interactionOptions);

        final PluginParameter<SingleChoiceParameterValue> levelOptions = SingleChoiceParameterType.build(LEVEL_PARAMETER_ID);
        levelOptions.setName("PluginException level");
        levelOptions.setDescription("PluginException level to throw an exception at");
        levelOptions.setHelpID("not.actually.helpful");
        SingleChoiceParameterType.setOptions(levelOptions, Arrays.asList(NONE, DEBUG, INFO, WARNING, ERROR, FATAL));
        params.addParameter(levelOptions);

        final PluginParameter<IntegerParameterValue> sleepParam = IntegerParameterType.build(SLEEP_PARAMETER_ID);
        sleepParam.setName("Sleep");
        sleepParam.setDescription("Seconds");
        IntegerParameterType.setMinimum(sleepParam, 0);
        IntegerParameterType.setMaximum(sleepParam, 20);
        sleepParam.setIntegerValue(0);
        params.addParameter(sleepParam);

        params.addController(SELECTED_PARAMETER_ID, (final PluginParameter<?> master, final Map<String, PluginParameter<?>> parameters, final ParameterChange change) -> {
            if (change == ParameterChange.VALUE) {
                final boolean masterBoolean = master.getBooleanValue();

                @SuppressWarnings("unchecked") //TEST1_PARAMETER will always be of type StringParameter
                final PluginParameter<StringParameterValue> t1 = (PluginParameter<StringParameterValue>) parameters.get(TEST1_PARAMETER_ID);
                t1.setEnabled(masterBoolean);

                @SuppressWarnings("unchecked") //TEST1_PARAMETER will always be of type StringParameter
                final PluginParameter<StringParameterValue> t2 = (PluginParameter<StringParameterValue>) parameters.get(TEST2_PARAMETER_ID);
                t2.setEnabled(masterBoolean);

                @SuppressWarnings("unchecked") //PLANETS_PARAMETER will always be of type MultiChoiceParameter
                final PluginParameter<MultiChoiceParameterValue> p = (PluginParameter<MultiChoiceParameterValue>) parameters.get(PLANETS_PARAMETER_ID);
                p.setEnabled(masterBoolean);

                @SuppressWarnings("unchecked") //DICE_PARAMETER will always be of type IntegerParameter
                final PluginParameter<IntegerParameterValue> d = (PluginParameter<IntegerParameterValue>) parameters.get(DICE_PARAMETER_ID);
                d.setEnabled(masterBoolean);

                @SuppressWarnings("unchecked") //COLOR_PARAMETER will always be of type ColorParameter
                final PluginParameter<ColorParameterValue> c = (PluginParameter<ColorParameterValue>) parameters.get(COLOR_PARAMETER_ID);
                c.setVisible(masterBoolean);
            }
        });

        params.addController(REFRESH_PARAMETER_ID, (final PluginParameter<?> master, final Map<String, PluginParameter<?>> parameters, final ParameterChange change) -> {
            if (change == ParameterChange.NO_CHANGE) { // button pressed
                @SuppressWarnings("unchecked") //ROBOT_PARAMETER will always be of type SingleChoiceParameter
                final PluginParameter<SingleChoiceParameterValue> robot = (PluginParameter<SingleChoiceParameterValue>) parameters.get(ROBOT_PARAMETER_ID);
                final int n = (int) (System.currentTimeMillis() % 100);
                SingleChoiceParameterType.setOptions(robot, Arrays.asList("Kryton " + n, "C-3PO " + n, "R2-D2 " + n));
                SingleChoiceParameterType.setChoice(robot, "C-3PO " + n);
            }
        });

        return params;
    }

    @Override
    protected RecordStore query(final RecordStore query, final PluginInteraction interaction, final PluginParameters parameters) throws PluginException {
        final int sleep = parameters.getParameters().get(SLEEP_PARAMETER_ID).getIntegerValue();
        for (int i = 0; i < sleep; i++) {
            LOGGER.log(Level.INFO, "sleep {0}/{1}", new Object[]{i, sleep});
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.log(Level.INFO, "slept for {0} seconds", sleep);

        LOGGER.log(Level.INFO, "parameters: {0}", parameters);
//        final List<String> searchTerms = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K");
//        AuditLogHelpers.generateSearchLog(getName(), searchTerms, "Test", 0);

        LOGGER.log(Level.INFO, "GraphElementType: {0}", parameters.getSingleChoice(ELEMENT_TYPE_PARAMETER_ID));
        final LocalDate localDate = parameters.getLocalDateValue(LOCAL_DATE_PARAMETER_ID);
        LOGGER.log(Level.INFO, "localdate: {0} ", localDate);
        if (localDate != null) {
            final Calendar cal = LocalDateParameterType.toCalendar(localDate);
            LOGGER.log(Level.INFO, String.format("toDate: [%s] [%04d-%02d-%02d]",
                    cal, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)));
            LOGGER.log(Level.INFO, String.format("fields: [%04d-%02d-%02d]",
                    localDate.get(ChronoField.YEAR), localDate.get(ChronoField.MONTH_OF_YEAR), localDate.get(ChronoField.DAY_OF_MONTH)));
        }

        final MultiChoiceParameterValue planets = parameters.getMultiChoiceValue(PLANETS_PARAMETER_ID);
        planets.getChoices().stream().forEach(planet -> {
            LOGGER.log(Level.INFO, "Planet: {0}", planet);
        });

        LOGGER.log(Level.INFO, "==== begin string values");
        parameters.getParameters().values().stream().forEach(param -> {
            LOGGER.log(Level.INFO, "String {0}: \"{1}\"", new Object[]{param.getName(), param.getStringValue()});
        });
        LOGGER.log(Level.INFO, "==== end string values");

        final File outputDir = DataAccessPreferenceKeys.getDataAccessResultsDir();
        if (outputDir != null) {
            final String fnam = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")) + "-testChainer.txt";
            final File fout = new File(outputDir, fnam);
            try (final PrintWriter writer = new PrintWriter(fout, StandardCharsets.UTF_8.name())) {
                parameters.getParameters().values().stream().forEach(param -> writer.printf("%s: '%s'", param.getName(), param.getStringValue()));
            } catch (final FileNotFoundException | UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        final List<String> keys = query.keys();
        while (query.next()) {
            keys.stream().forEach(key -> {
                LOGGER.log(Level.INFO, String.format("%-20s: %s", key, query.get(key)));
            });

            LOGGER.log(Level.INFO, "--");
        }

        final boolean crash = parameters.getBooleanValue(CRASH_PARAMETER_ID);
        if (crash) {
            throw new RuntimeException("Simulated plugin failure");
        }

        try {
            interaction.setProgress(1, 0, String.format("Pretended to add %d node(s), modify %d node(s)", r.nextInt(100) + 1, r.nextInt(100) + 1), false);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }

        final String queryName = parameters.getStringValue(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID);
        LOGGER.log(Level.INFO, "query name: {0}", queryName);

        final DateTimeRange dtr = parameters.getDateTimeRangeValue(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID);
        final ZonedDateTime[] dtrStartEnd = dtr.getZonedStartEnd();
        LOGGER.log(Level.INFO, "range: (zdt) {0} .. {1}", new Object[]{dtrStartEnd[0], dtrStartEnd[1]});
        LOGGER.log(Level.INFO, "range: (zdt) {0} .. {1}", new Object[]{DateTimeFormatter.ISO_INSTANT.format(dtrStartEnd[0]), DateTimeFormatter.ISO_INSTANT.format(dtrStartEnd[1])});

        final String interactionLevel = parameters.getParameters().get(INTERACTION_PARAMETER_ID).getStringValue();
        final PluginNotificationLevel pnInteractionLevel;
        if (interactionLevel != null) {
            switch (interactionLevel) {
                case DEBUG:
                    pnInteractionLevel = PluginNotificationLevel.DEBUG;
                    break;
                case INFO:
                    pnInteractionLevel = PluginNotificationLevel.INFO;
                    break;
                case WARNING:
                    pnInteractionLevel = PluginNotificationLevel.WARNING;
                    break;
                case ERROR:
                    pnInteractionLevel = PluginNotificationLevel.ERROR;
                    break;
                case FATAL:
                    pnInteractionLevel = PluginNotificationLevel.FATAL;
                    break;
                default:
                    pnInteractionLevel = null;
                    break;
            }

            if (pnInteractionLevel != null) {
                interaction.notify(pnInteractionLevel, "Interaction from plugin");
            }
        }

        final String exceptionLevel = parameters.getParameters().get(LEVEL_PARAMETER_ID).getStringValue();
        final PluginNotificationLevel pnExceptionLevel;
        if (exceptionLevel != null) {
            switch (exceptionLevel) {
                case DEBUG:
                    pnExceptionLevel = PluginNotificationLevel.DEBUG;
                    break;
                case INFO:
                    pnExceptionLevel = PluginNotificationLevel.INFO;
                    break;
                case WARNING:
                    pnExceptionLevel = PluginNotificationLevel.WARNING;
                    break;
                case ERROR:
                    pnExceptionLevel = PluginNotificationLevel.ERROR;
                    break;
                case FATAL:
                    pnExceptionLevel = PluginNotificationLevel.FATAL;
                    break;
                default:
                    pnExceptionLevel = null;
                    break;
            }

            if (pnExceptionLevel != null) {
                throw new PluginException(pnExceptionLevel, "Exception thrown from plugin");
            }
        }

        // Add nodes containing global query parameters
        final RecordStore results = new GraphRecordStore();
        results.add();
        results.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.RAW, "name1@domain1.com");
        results.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, "Email");
        results.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.COMMENT, queryName);
        results.set(GraphRecordStoreUtilities.SOURCE + TemporalConcept.VertexAttribute.LAST_SEEN, DateTimeFormatter.ISO_INSTANT.format(dtrStartEnd[0]).replace("Z", ".000Z"));

        results.set(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.RAW, "name2@domain2.com");
        results.set(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, "Email");
        results.set(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.COMMENT, queryName);
        results.set(GraphRecordStoreUtilities.DESTINATION + TemporalConcept.VertexAttribute.LAST_SEEN, DateTimeFormatter.ISO_INSTANT.format(dtrStartEnd[1]).replace("Z", ".000Z"));

        results.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.COMMENT, parameters.toString());

        return results;
    }

    public class GraphElementTypeParameterValue extends ParameterValue {

        private GraphElementType elementType;

        public GraphElementTypeParameterValue() {
            elementType = null;
        }

        public GraphElementTypeParameterValue(final GraphElementType elementType) {
            this.elementType = elementType;
        }

        public GraphElementType get() {
            return elementType;
        }

        public boolean set(final GraphElementType newet) {
            if (newet != elementType) {
                elementType = newet;
                return true;
            }

            return false;
        }

        @Override
        public String validateString(final String s) {
            return null;
        }

        @Override
        public boolean setStringValue(final String s) {
            return set(GraphElementType.getValue(s));
        }

        @Override
        public Object getObjectValue() {
            return elementType;
        }

        @Override
        public boolean setObjectValue(final Object o) {
            final GraphElementType newElementType;
            if (o == null) {
                newElementType = null;
            } else if (o instanceof GraphElementType) {
                newElementType = (GraphElementType) o;
            } else {
                throw new IllegalArgumentException(String.format("Unexpected class %s", o.getClass()));
            }

            return set(newElementType);
        }

        @Override
        protected ParameterValue createCopy() {
            return new GraphElementTypeParameterValue(elementType);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(elementType);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GraphElementTypeParameterValue other = (GraphElementTypeParameterValue) obj;
            return this.elementType == other.elementType;
        }

        @Override
        public String toString() {
            return elementType.getShortLabel();
        }
    }
}
