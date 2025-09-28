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

import javafx.scene.control.Label;

/**
 * An Interface for {@link ConstellationInput} extensions.
 * Provides required functionality to allow an extension to have a {@link LeftButton}.
 * 
 * @author capricornunicorn123
 */
public interface LeftButtonSupport {
    
    public LeftButton getLeftButton();
    
    public void executeLeftButtonAction();
    
    public abstract class LeftButton extends ConstellationInputButton {
        protected LeftButton(final Label label, final ConstellationInputButton.ButtonType type) {
            super(label, type);
        }
    }
    
}
