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

package au.gov.asd.tac.constellation.utilities.keyboardshortcut;

import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.StringUtils;

/**
 * Customised Dialog to get key press event on a Label 
 * @author spica
 */
public class KeyPressLabelDialog extends Dialog<String> {
    
    private final GridPane grid;
    private final Label label;
    private final Label infoLabel;
    private final String defaultValue;
    
    
    public KeyPressLabelDialog() {
        this(StringUtils.EMPTY);        
    }
    
    public KeyPressLabelDialog(@NamedArg("defaultValue") final String defaultValue) {
        final DialogPane dialogPane = getDialogPane();
        
        // -- label
        infoLabel = new Label("Keyboard Shortcut : ");
        
        label = createContentLabel(defaultValue);
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);        

        this.defaultValue = defaultValue;

        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        dialogPane.contentTextProperty().addListener(o -> updateGrid());
        
        
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogPane.addEventHandler(KeyEvent.KEY_PRESSED, event -> { // NOSONAR
            if (!event.getCode().isModifierKey() 
                    && !event.getCode().equals(KeyCode.ENTER)
                    && !event.getCode().equals(KeyCode.ESCAPE)
                    && !event.getCode().equals(KeyCode.SPACE)) {
                label.setText(RecordKeyboardShortcut.createCombo(event).getDisplayText());
            }
        });
        updateGrid();

        setResultConverter(dialogButton -> { // NOSONAR
            final ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            String result = data == ButtonBar.ButtonData.OK_DONE ? label.getText() : StringUtils.EMPTY;            
            label.setText(result);
            return result;
        });
    }



    /* ************************************************************************
     *
     * Public API
     *
     **************************************************************************/
    
    /**
     * Returns the default value that was specified in the constructor.
     * @return the default value that was specified in the constructor
     */
    public final String getDefaultValue() {
        return defaultValue;
    }

    public final Label getLabel() {      
        return label;
    }

    /* ************************************************************************
     *
     * Private Implementation
     *
     **************************************************************************/

    private void updateGrid() {
        grid.getChildren().clear();

        grid.add(infoLabel, 0, 0);
        grid.add(label, 1, 0);
        getDialogPane().setContent(grid);

        Platform.runLater(() -> label.requestFocus());
    }
    
    
    private static Label createContentLabel(final String text) {
        final Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }

}
