/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.gui.field;

import au.gov.asd.tac.constellation.utilities.gui.field.framework.AutoCompleteSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ChoiceInputField;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInput;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputButton.ButtonType;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputConstants;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputConstants.ChoiceType;
import static au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputConstants.ChoiceType.SINGLE_DROPDOWN;
import static au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputConstants.ChoiceType.SINGLE_SPINNER;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputDropDown;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputListener;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.LeftButtonSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.RightButtonSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ShortcutSupport;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * A {@link ChoiceInput} for managing single choice selection. This input provides the following
 * {@link ConstellationInput} support features
 * <ul>
 * <li>{@link RightButtonSupport} - Increments the choice in spinners and triggers a drop down menu to select a choice
 * from the list of options.</li>
 * <li>{@link LeftButtonSupport} - Only used in Spinner inputs to decrement the choice.</li>
 * <li>{@link ShortcutSupport} - Increments and decrements the data chronologically with up and down arrow.</li>
 * <li>{@link AutoCompleteSupport} - Provides a list of colors with a name that matches the text in the input
 * field.</li>
 * </ul>
 * See referenced classes and interfaces for further details on inherited and implemented features.
 *
 * @param <C> The type of object represented by this input.
 *
 * @author capricornunicorn123
 */
public class SingleChoiceInput<C extends Object> extends ChoiceInputField<C, C> implements RightButtonSupport, LeftButtonSupport, AutoCompleteSupport, ShortcutSupport {

    private final ChoiceType type;

    public SingleChoiceInput(final ChoiceType type) {
        this.type = type;
        initialiseDependantComponents();
    }

    @Override
    public EventHandler<KeyEvent> getShortcuts() {
        //Add shortcuts where users can increment and decrement the date using up and down arrows
        return event -> {
            if (event.getCode() == KeyCode.UP) {
                this.decrementChoice();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                this.incrementChoice();
                event.consume();
            }
        };
    }

    public C getChoice() {
        final List<C> matches = getOptions().stream().filter(choice -> choice.toString().equals(getText())).toList();
        return matches.isEmpty() ? null : matches.getFirst();
    }

    /**
     * Changes the List of selected Choices to ensure the provided choice is included. if single choice selection mode
     * then the old choice is removed if multi choice the old choice is retained
     *
     * @param choice
     */
    public void setChoice(final C choice) {
        if (choice != null && this.getOptions().contains(choice)) {
            this.setText(choice.toString());
        } else {
            clearChoices();
        }
    }

    /**
     * Removes the provided choice from the currently selected choices.
     *
     * @param choice
     */
    public void removeChoice(final C choice) {
        if (getChoice() == choice) {
            clearChoices();
        }
    }

    /**
     * Used in single choice Options to increment a selected choice. If the choice is the last choice in the list of
     * options the next choice is the first option.
     */
    private void incrementChoice() {
        final C selection = this.getChoice();
        if (selection != null) {
            final int nextSelectionIndex = this.getOptions().indexOf(selection) + 1;
            if (nextSelectionIndex < this.getOptions().size()) {
                this.setChoice(this.getOptions().get(nextSelectionIndex));
            } else {
                this.setChoice(this.getOptions().getFirst());
            }
        } else {
            this.setChoice(this.getOptions().getLast());
        }
    }

    /**
     * Used in single choice Options to decrement a selected choice. If the choice is the first choice in the list of
     * options the previous choice is the last option.
     */
    private void decrementChoice() {
        final C selection = this.getChoice();
        if (selection != null) {
            final int prevSelectionIndex = this.getOptions().indexOf(selection) - 1;
            if (prevSelectionIndex < this.getOptions().size()) {
                this.setChoice(this.getOptions().get(prevSelectionIndex));
            } else {
                this.setChoice(this.getOptions().getLast());
            }
        } else {
            this.setChoice(this.getOptions().getFirst());
        }
    }

    @Override
    public C getValue() {
        return getChoice();
    }

    @Override
    public void setValue(final C value) {
        this.setChoice(value);
    }

    @Override
    public boolean isValidContent() {
        return getText().isBlank() || getChoice() != null;
    }

    @Override
    public List<MenuItem> getLocalMenuItems() {
        final List<MenuItem> items = new ArrayList<>();
        if (type != null) {
            if (type == SINGLE_SPINNER) {
                final MenuItem next = new MenuItem("Increment");
                next.setOnAction(value -> executeRightButtonAction());
                items.add(next);

                final MenuItem prev = new MenuItem("Decrement");
                prev.setOnAction(value -> executeLeftButtonAction());
                items.add(prev);
            }
            final MenuItem choose = new MenuItem("Select Choice");
            choose.setOnAction(value -> executeRightButtonAction());
            items.add(choose);
        }
        return items;
    }

    @Override
    public LeftButton getLeftButton() {
        if (type == SINGLE_SPINNER) {
            return new LeftButton(new Label(ConstellationInputConstants.PREVIOUS_BUTTON_LABEL), ButtonType.CHANGER) {
                public EventHandler<? super MouseEvent> action() {
                    return event -> executeLeftButtonAction();
                }
            };
        } else {
            return null;
        }
    }

    @Override
    public RightButton getRightButton() {
        final Label label;
        final ButtonType buttonType;

        switch (type) {
            case SINGLE_SPINNER -> {
                label = new Label(ConstellationInputConstants.NEXT_BUTTON_LABEL);
                buttonType = ButtonType.CHANGER;
            }
            case SINGLE_DROPDOWN -> {
                label = new Label(ConstellationInputConstants.SELECT_BUTTON_LABEL);
                buttonType = ButtonType.DROPDOWN;
            }
            default -> {
                return null;
            }
        }
        return new RightButton(label, buttonType) {           
            @Override
            public void show() {
                // show our menu instead
                executeRightButtonAction();
            }

            @Override
            public void hide() {
                // this is triggered when clicking away from button
                setMenuShown(false);
            }
        };
    }

    @Override
    public void executeLeftButtonAction() {
        if (type == SINGLE_SPINNER) {
            decrementChoice();
        }
    }

    @Override
    public void executeRightButtonAction() {
        if (type == SINGLE_SPINNER) {
            this.incrementChoice();
        } else if (type == SINGLE_DROPDOWN) {
            this.showDropDown(new ChoiceInputDropDown(this));
        }
    }

    @Override
    public List<MenuItem> getAutoCompleteSuggestions() {
        final List<MenuItem> suggestions = new ArrayList<>();
        // Get suggestions based on the text showing 
        this.getOptions().stream().map(value -> value)
                .filter(value -> value.toString().toUpperCase().contains(getText().toUpperCase()))
                .forEach(value -> {
                    final MenuItem item = new MenuItem(value.toString());
                    item.setOnAction(event -> this.setChoice(value));
                    suggestions.add(item);
                });
        
        // If the text matches a suggestion, show all suggestions 
        // The currently selected option will show at the top of the suggestions list 
        if (suggestions.size() > 0) {
            final String match = suggestions.get(0).getText();
            if (suggestions.size() == 1 && match.toUpperCase().equals(getText().toUpperCase())) {
                this.getOptions().stream().map(value -> value)
                        .forEach(value -> {
                            if (!match.equals(value.toString())) {
                                final MenuItem item = new MenuItem(value.toString());
                                item.setOnAction(event -> this.setChoice(value));
                                suggestions.add(item);
                            }
                        });
            }
        }
        return suggestions;
    }

    /**
     * A context menu to be used as a drop down for {@link ChoiceInputFields}
     */
    private class ChoiceInputDropDown extends ConstellationInputDropDown {

        final List<CheckBox> boxes = new ArrayList<>();

        public ChoiceInputDropDown(final SingleChoiceInput field) {
            super(field);

            if (getOptions() != null) {
                final Object[] optionsList = getOptions().toArray();
                for (int i = 0; i < optionsList.length; i++) {
                    final C choice = (C) optionsList[i];

                    final Labeled item = switch (field.type) {
                        case SINGLE_DROPDOWN -> {
                            final Label label = new Label(choice.toString());
                            label.setOnMouseClicked(event
                                    -> field.setChoice(choice));
                            yield label;
                        }
                        case SINGLE_SPINNER -> new Label();
                    };
                    
                    if (!icons.isEmpty()) {
                        item.setGraphic(icons.get(i));
                    }
                    this.registerCustomMenuItem(item);
                }
            }

            final ConstellationInputListener<List<C>> cl = (final List<C> newValue) -> {
                if (newValue != null) {
                    final List<String> stringrep = newValue.stream().map(Object::toString).toList();
                    for (final CheckBox box : boxes) {
                        box.setSelected(stringrep.contains(box.getText())); 
                    }
                }
            };

            // Register the Context Menu as a listener whilst it is open incase choices are modified externaly.
            this.setOnShowing(value -> field.addListener(cl));

            // This context menu may be superseeded by a new context menu so deregister it when hidden.
            this.setOnHiding(value ->  field.removeListener(cl));
        }
    }
}
