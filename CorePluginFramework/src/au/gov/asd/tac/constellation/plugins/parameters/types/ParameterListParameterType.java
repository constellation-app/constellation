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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType.ParameterList;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType.ParameterListParameterValue;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import org.openide.util.lookup.ServiceProvider;

/**
 * The ParameterListParameterType defines {@link PluginParameter} objects that
 * holds a dynamic, possibly empty list of other {@link PluginParameters}
 * objects (each having the same set of parameters).
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = PluginParameterType.class)
public class ParameterListParameterType extends PluginParameterType<ParameterListParameterValue> {

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "parameterlist";

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final ParameterListParameterType INSTANCE = new ParameterListParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of BooleanParameterType.
     */
    public static PluginParameter<ParameterListParameterValue> build(final String id) {
        return new PluginParameter<>(new ParameterListParameterValue(), INSTANCE, id);
    }

    /**
     * Constructs a new instance of this type.
     * <p>
     * Note: This constructor should not be called directly; it is public for
     * the purposes of lookup (which may be removed for types in the future). To
     * buildId parameters from the type, the static method
     * {@link #build buildId()} should be used, or the singleton
     * {@link #INSTANCE INSTANCE}.
     */
    public ParameterListParameterType() {
        super(ID);
    }

    /**
     * Set the prototype {@link PluginParameters} object for the given
     * parameter. This defines the structure of the {@link PluginParameters}
     * objects in the list that the parameter will hold.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param prototypeParameters A
     * {@link au.gov.asd.tac.constellation.plugins.parameters.PluginParameters}
     * object which forms the prototype for the given parameter.
     */
    public static void setPrototypeParameters(final PluginParameter<ParameterListParameterValue> parameter, final PluginParameters prototypeParameters) {
        parameter.setParameterListValue(new ParameterList(
                (final PluginParameters p) -> PluginParametersPane.buildPane(p, null), prototypeParameters, parameter
        ));
    }

    /**
     * Add a new {@link PluginParameters} object to the given parameters list of
     * values. The new {@link PluginParameters} will have all the values set to
     * those defined by the prototype {@link PluginParameters}.
     *
     * @param parameter A {@link PluginParameter} of this type. for the given
     * parameter.
     *
     * @return a new pane to display the specified parameter.
     */
    public static Pane addToList(final PluginParameter<ParameterListParameterValue> parameter) {
        return parameter.getParameterListValue().get().append();
    }

    /**
     * Remove the {@link PluginParameters} object corresponding to the specified
     * Pane from the given parameter's list of values.
     * <p>
     * Note this method will be redesigned in the future. GUI elements should
     * most certainly not be coupled with ParameterType objects.
     *
     * @param parameter A {@link PluginParameter} of this type. for the given
     * parameter.
     * @param parameterPane A Pane corresponding to the {@link PluginParameters}
     * object to be removed.
     *
     * @return a new pane to display the specified parameter.
     */
    public static Pane removeFromList(final PluginParameter<ParameterListParameterValue> parameter, final Pane parameterPane) {
        return parameter.getParameterListValue().get().remove(parameterPane);
    }

    /**
     * Move the {@link PluginParameters} object corresponding to the specified
     * Pane up in the given parameter's list of values.
     * <p>
     * Note this method will be redesigned in the future. GUI elements should
     * most certainly not be coupled with ParameterType objects.
     *
     * @param parameter A {@link PluginParameter} of this type. for the given
     * parameter.
     * @param parameterPane A Pane corresponding to the {@link PluginParameters}
     * object to be moved up.
     */
    public static void moveUp(final PluginParameter<ParameterListParameterValue> parameter, final Pane parameterPane) {
        parameter.getParameterListValue().get().moveUp(parameterPane);
    }

    /**
     * Move the {@link PluginParameters} object corresponding to the specified
     * Pane down in the given parameter's list of values.
     * <p>
     * Note this method will be redesigned in the future. GUI elements should
     * most certainly not be coupled with ParameterType objects.
     *
     * @param parameter A {@link PluginParameter} of this type. for the given
     * parameter.
     * @param parameterPane A Pane corresponding to the {@link PluginParameters}
     * object to be moved down.
     */
    public static void moveDown(final PluginParameter<ParameterListParameterValue> parameter, final Pane parameterPane) {
        parameter.getParameterListValue().get().moveDown(parameterPane);
    }

    @FunctionalInterface
    public static interface PluginPaneFactory {

        public Pane getNewPane(final PluginParameters parameters);
    }

    @Override
    public boolean requiresLabel() {
        return false;
    }

    public static class ParameterListLockingPluginParameters extends PluginParameters {

        @Override
        public PluginParameters copy() {
            final PluginParameters copy = new ParameterListLockingPluginParameters();
            copyTo(copy);
            return copy;
        }

        @Override
        public void startParameterLoading() {
            for (final PluginParameter<?> p : getParameters().values()) {
                if (p.getType() instanceof ParameterListParameterType) {
                    p.getParameterListValue().lockValue();
                }
            }
        }

        @Override
        public void endParameterLoading() {
            for (final PluginParameter<?> p : getParameters().values()) {
                if (p.getType() instanceof ParameterListParameterType) {
                    p.getParameterListValue().unlockValue();
                }
            }
        }

    }

    public static class ParameterList {

        private final PluginPaneFactory paneFactory;
        private final PluginParameters parametersPrototype;
        private final List<PluginParameters> parametersList;
        private final List<Pane> parameterPanes;
        private PluginParameter<?> enclosingParameter;
        private int validParams = 0;

        private void parameterHasChanged() {
            enclosingParameter.setError(validParams >= 0 ? null : "invalid enclosed params");
        }

        private void linkToEnclosing(final PluginParameter<?> parameter) {
            parameter.setEnclosingParameter(enclosingParameter);
            parameter.addListener((final PluginParameter<?> parameter1, final ParameterChange change) -> {
                switch (change) {
                    case ERROR -> {
                        if (parameter1.getError() == null) {
                            validParams++;
                        } else {
                            validParams--;
                        }
                        parameterHasChanged();
                    }
                    case VALUE -> parameterHasChanged();
                    default -> {
                    }
                }
            });
        }

        private void unlinkFromEnclosing(final PluginParameter<?> parameter) {
            if (parameter.getError() != null) {
                validParams++;
                parameterHasChanged();
            }
        }

        public ParameterList(final PluginPaneFactory paneFactory, final PluginParameters parametersPrototype, final PluginParameter<?> enclosingParameter) {
            parametersList = new ArrayList<>();
            parameterPanes = new ArrayList<>();
            this.paneFactory = paneFactory;
            this.parametersPrototype = parametersPrototype;
            this.enclosingParameter = enclosingParameter;
        }

        PluginParameters getNewItem() {
            return parametersPrototype.copy();
        }

        public Pane append() {
            return append(getNewItem());
        }

        public Pane append(final PluginParameters newParams) {
            newParams.getParameters().values().forEach(param -> linkToEnclosing(param));
            parametersList.add(newParams);
            final Pane newPane = paneFactory.getNewPane(newParams);
            parameterPanes.add(newPane);
            enclosingParameter.fireChangeEvent(ParameterChange.VALUE);
            return newPane;
        }

        public Pane remove(final Pane parameterPane) {
            final int index = parameterPanes.indexOf(parameterPane);
            final PluginParameters oldParams = parametersList.get(index);
            oldParams.getParameters().values().forEach(param -> unlinkFromEnclosing(param));
            parametersList.remove(index);
            enclosingParameter.fireChangeEvent(ParameterChange.VALUE);
            return parameterPanes.remove(index);
        }

        public void moveUp(final Pane parameterPane) {
            final int index = parameterPanes.indexOf(parameterPane);
            final PluginParameters pp = parametersList.remove(index);
            parametersList.add(index - 1, pp);
            parameterPanes.remove(index);
            parameterPanes.add(index - 1, parameterPane);
            enclosingParameter.fireChangeEvent(ParameterChange.VALUE);
        }

        public void moveDown(final Pane parameterPane) {
            final int index = parameterPanes.indexOf(parameterPane);
            final PluginParameters pp = parametersList.remove(index);
            parametersList.add(index + 1, pp);
            parameterPanes.remove(index);
            parameterPanes.add(index + 1, parameterPane);
            enclosingParameter.fireChangeEvent(ParameterChange.VALUE);
        }

    }

    public static class ParameterListParameterValue extends ParameterValue {
        
        private static final Logger LOGGER = Logger.getLogger(ParameterListParameterValue.class.getName());

        private ParameterList value;
        private boolean locked = false;
        // Used to temporarily store a string value while loading this paramete at times when the structure may need to be set elsewhere before this string value makes sense.
        private String cachedValue = null;

        public ParameterListParameterValue() {
            this(null);
        }

        private ParameterListParameterValue(final ParameterList value) {
            this.value = value;
        }

        @Override
        public Object getObjectValue() {
            return value;
        }

        @Override
        public boolean setObjectValue(final Object o) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public boolean set(final ParameterList val) {
            if (value == null && val == null) {
                return false;
            }
            if (value != null && value.equals(val)) {
                return false;
            }
            value = val;
            return true;
        }

        public ParameterList get() {
            return value;
        }

        @Override
        public String validateString(final String s) {
            return null;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(value.paneFactory);
            hash = 37 * hash + Objects.hashCode(value.parametersPrototype);
            hash = 37 * hash + Objects.hashCode(value.parametersList);
            hash = 37 * hash + Objects.hashCode(value.parameterPanes);
            hash = 37 * hash + Objects.hashCode(value.enclosingParameter);
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final ParameterListParameterValue other = (ParameterListParameterValue) obj;
            return Objects.equals(value.paneFactory, other.value.paneFactory)
                    && Objects.equals(value.parametersPrototype, other.value.parametersPrototype)
                    && Objects.equals(value.parametersList, other.value.parametersList)
                    && Objects.equals(value.parameterPanes, other.value.parameterPanes)
                    && Objects.equals(value.enclosingParameter, other.value.enclosingParameter);
        }

        public List<PluginParameters> getListOfPluginParameters() {
            return Collections.unmodifiableList(value.parametersList);
        }

        public List<Pane> getPanes() {
            return Collections.unmodifiableList(value.parameterPanes);
        }

        @Override
        protected ParameterListParameterValue createCopy() {
            final ParameterListParameterValue copy = new ParameterListParameterValue(new ParameterList(value.paneFactory, value.parametersPrototype, value.enclosingParameter));
            copy.setStringValue(toString());
            copy.locked = locked;
            copy.cachedValue = cachedValue;
            return copy;
        }

        public void setEnclosingParameter(final PluginParameter<?> param) {
            value.enclosingParameter = param;
        }

        @Override
        public String toString() {
            if (value.parametersList.isEmpty()) {
                return "";
            }
            final StringBuilder strValBuilder = new StringBuilder(String.valueOf(value.parametersList.size()));
            for (final PluginParameters pps : value.parametersList) {
                strValBuilder.append("::");
                for (final Entry<String, PluginParameter<?>> pp : pps.getParameters().entrySet()) {
                    strValBuilder.append(pp.getKey().replace(SeparatorConstants.SEMICOLON, "\\;").replaceAll(SeparatorConstants.COLON, "\\:"));
                    strValBuilder.append(";;");
                    final String val = pp.getValue().getStringValue();
                    if (val != null) {
                        strValBuilder.append(val.replace(SeparatorConstants.SEMICOLON, "\\;").replaceAll(SeparatorConstants.COLON, "\\:"));
                    }
                    strValBuilder.append(";;");
                }
            }
            return strValBuilder.toString();
        }

        @Override
        public boolean setStringValue(final String strValue) {
            if (locked) {
                if (cachedValue.equals(strValue)) {
                    return false;
                } else {
                    cachedValue = strValue;
                    return true;
                }
            }
            if (strValue.equals(toString())) {
                return false;
            }
            value.parametersList.clear();
            value.parameterPanes.clear();
            if (strValue.isEmpty()) {
                return true;
            }
            appendPanels(strValue);
            return true;
        }

        private void appendPanels(final String strValue) {
            final String[] args = strValue.split("::");
            for (int i = 1; i <= Integer.parseInt(args[0]); i++) {
                final PluginParameters newParams = value.getNewItem();
                final String[] keyVals = args[i].split(";;");
                for (int j = 0; j < keyVals.length; j += 2) {
                    final String key = keyVals[j].replace("\\;", SeparatorConstants.SEMICOLON).replace("\\:", SeparatorConstants.COLON);
                    final String val = keyVals[j + 1].replace("\\;", SeparatorConstants.SEMICOLON).replace("\\:", SeparatorConstants.COLON);
                    newParams.getParameters().get(key).setStringValue(val);
                }
                final CountDownLatch panelCreated = new CountDownLatch(1);
                if (Platform.isFxApplicationThread()) {
                    value.append(newParams);
                    panelCreated.countDown();
                } else {
                    Platform.runLater(() -> {
                        value.append(newParams);
                        panelCreated.countDown();
                    });
                }
                try {
                    panelCreated.await();
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Prevents any value changes from occuring via setStringValue() - they are instead stored in cachedValue.
        // Used in cases when the parameter's structure may be in an indeterminate state such as at parameter loading time.
        public void lockValue() {
            cachedValue = toString();
            locked = true;
        }

        // Allows value changes to occur via setStringValue() and sets the value to the last value stored in the cache.
        public void unlockValue() {
            locked = false;
            if (cachedValue != null) {
                setStringValue(cachedValue);
            }
            cachedValue = null;
        }

        protected boolean isLocked() {
            return locked;
        }

        protected String getCachedValue() {
            return cachedValue;
        }       
    }
}
