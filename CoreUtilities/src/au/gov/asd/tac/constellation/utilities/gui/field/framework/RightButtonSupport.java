/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInput;
import javafx.scene.control.Label;

/**
 * An Interface for {@link ConstellationInput} extensions.
 * Provides required functionality to allow an extension to have a {@link RightButton}.
 * 
 * @author capricornunicorn123
 */
public interface RightButtonSupport {
    
    public RightButton getRightButton();
    
    public void executeRightButtonAction();
    
    public abstract class RightButton extends Button {
        public RightButton(final Label label, final ButtonType type) {
            super(label, type);
        }
    }
    
}
