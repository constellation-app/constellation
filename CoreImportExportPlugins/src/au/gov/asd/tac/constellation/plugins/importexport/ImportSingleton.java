/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;


/**
 *
 * @author altair1673
 */
public class ImportSingleton {

    private static ImportSingleton instance = null;

    // Property that triggers the different run windows in the configuration panel to clear
    private final IntegerProperty clearDataFlag = new SimpleIntegerProperty();

    private ImportSingleton() {
        clearDataFlag.set(2);
    }

    public static synchronized ImportSingleton getDefault() {
        if (instance == null) {
            instance = new ImportSingleton();
        }
        return instance;
    }

    /**
     * Returns the clearDataFlag
     *
     * @return
     */
    public IntegerProperty getClearDataFlag() {
        return clearDataFlag;
    }

    /**
     * Sets the value of the clearDataFlag to be the either negative or positive
     * based on its current state
     */
    public void triggerClearDataFlag() {
        // Multiply by negative 1 everytime to change value from negative to postive
        // and vice-versa
        clearDataFlag.set(-1 * clearDataFlag.get());
    }
}
