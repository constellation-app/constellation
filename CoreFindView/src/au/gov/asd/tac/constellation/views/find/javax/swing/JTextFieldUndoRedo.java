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
package au.gov.asd.tac.constellation.views.find.javax.swing;

import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author Atlas139mkm
 */
public class JTextFieldUndoRedo extends JTextField {

    public final Logger LOGGER = Logger.getLogger(JTextFieldUndoRedo.class.getName());
    private final ImporovedUndoManger undoManager = new ImporovedUndoManger();

    protected Vector<UndoableEdit> edits;


    public JTextFieldUndoRedo() {
        super();
        this.getDocument().addUndoableEditListener(undoManager);

        Action controlZ = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    undoManager.undo();

                }
            }
        };
        Action controlY = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
        };
        String keyStrokeAndKeyZ = "control Z";
        KeyStroke keyStrokeZ = KeyStroke.getKeyStroke(keyStrokeAndKeyZ);
        this.getInputMap().put(keyStrokeZ, controlZ);

        String keyStrokeAndKeyY = "control Y";
        KeyStroke keyStrokeY = KeyStroke.getKeyStroke(keyStrokeAndKeyY);
        this.getInputMap().put(keyStrokeY, controlY);

    }

    public JTextFieldUndoRedo(String text) {
        super(text);
        this.getDocument().addUndoableEditListener(undoManager);

        Action controlZ = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    LOGGER.log(Level.SEVERE, undoManager.getUndoPresentationName());
                    LOGGER.log(Level.SEVERE, undoManager.getPresentationName());
                    LOGGER.log(Level.SEVERE, undoManager.getUndoOrRedoPresentationName());

                    LOGGER.log(Level.SEVERE, String.valueOf(undoManager.getLimit()));
                    undoManager.undo();

//                    LOGGER.log(Level.SEVERE, undoManager.getUndoPresentationName());
                }
            }
        };
        Action controlY = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    undoManager.redo();

                }
            }
        };
        String keyStrokeAndKey = "control Z";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);

        this.getInputMap()
                .put(keyStroke, controlZ);

        this.getInputMap()
                .put(keyStroke, controlY);

    }

    public JTextFieldUndoRedo(int columns) {
        super(columns);
        this.getDocument().addUndoableEditListener(undoManager);

    }

    public JTextFieldUndoRedo(String text, int columns) {
        super(text, columns);
        this.getDocument().addUndoableEditListener(undoManager);

    }

}
