/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

import au.gov.asd.tac.constellation.utilities.gui.field.MultiChoiceInput;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * A {@link ConstellationInput} for managing choice selection. This input
 * provides a set of shared features used by {@link SingleChoiceInputs] and {@link MultiChoiceInput}.
 *
 * See referenced classes and interfaces for further details on inherited and implemented features.
 *
 * @param <O> The type of object represented by this input field. (An individual
 * choice)
 * @param <C> The type of object returned by this input. (an individual choice
 * or a set of choices)
 *
 * @author capricornunicorn123
 */
public abstract class ChoiceInputField<C extends Object, O extends Object>
        extends ConstellationInput<C> {

    private final List<O> options = new ArrayList<>();
    protected final List<ImageView> icons = new ArrayList<>();

    protected ChoiceInputField() {
    }

    protected ChoiceInputField(final ObservableList<O> options) {
        if (options == null) {
            throw new InvalidOperationException(
                    "Attempting to Set Options with null options");
        }
        this.options.addAll(options);
    }

    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">   
    /**
     * Defines the options that users can select from in this field. Any
     * previously defined options will be overwritten with this new list.
     *
     * @param options
     */
    public final void setOptions(final List<O> options) {
        this.options.clear();
        if (options != null) {
            this.options.addAll(options);
        } else {
            throw new InvalidOperationException(
                    "Attempting to Set Options with null options");
        }
    }

    /**
     * Retrieves the options that users can select from in this field.
     *
     * @return List of Options
     */
    public final List<O> getOptions() {
        return this.options;
    }

    /**
     * Defines the list of icons for the context menu
     *
     * @param icons
     */
    public final void setIcons(final List<ImageView> icons) {
        getIcons().clear();
        getIcons().addAll(icons);
    }

    /**
     * @return the icons
     */
    public List<ImageView> getIcons() {
        return icons;
    }

    /**
     * Removes all choices.
     */
    public final void clearChoices() {
        setText("");
    }
    // </editor-fold> 

}
