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
 * @author spatel308
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
