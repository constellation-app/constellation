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
package au.gov.asd.tac.constellation.views.qualitycontrol.widget;

import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlState;
import javafx.scene.control.Button;

/**
 * A placeholder for a quality button.
 *
 * @author algol
 */
public abstract class QualityControlAutoButton extends Button {

    /**
     * Update the QualityControlAutoButton using the quality control event data from the given QualityControlState.
     *
     * @param state the new QualityControlState.
     */
    protected abstract void update(final QualityControlState state);

    public abstract QualityControlAutoButton copy();
}
