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

package au.gov.asd.tac.constellation.utilities.keyboardshortcut;

import java.util.ArrayList;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author spica
 */
public class RecordKeyboardShortcut {
    
    public Optional<String> start(Stage primaryStage) {
        var label = new Label();
        label.setFont(Font.font("Segoe UI", 15));
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (!event.getCode().isModifierKey()) {
                label.setText(createCombo(event).getDisplayText());
            }
        });

        primaryStage.setScene(new Scene(new StackPane(label), 500, 300));
        primaryStage.setResizable(false);
        primaryStage.showAndWait();        
        return Optional.of(label.getText().replace('+', ' ') +" ");
    }

    private KeyCombination createCombo(KeyEvent event) {
        var modifiers = new ArrayList<Modifier>();
        if (event.isControlDown()) {
            modifiers.add(KeyCombination.CONTROL_DOWN);
        }
        if (event.isMetaDown()) {
            modifiers.add(KeyCombination.META_DOWN);
        }
        if (event.isAltDown()) {
            modifiers.add(KeyCombination.ALT_DOWN);
        }
        if (event.isShiftDown()) {
            modifiers.add(KeyCombination.SHIFT_DOWN);
        }
        return new KeyCodeCombination(event.getCode(), modifiers.toArray(Modifier[]::new));
    }

}
