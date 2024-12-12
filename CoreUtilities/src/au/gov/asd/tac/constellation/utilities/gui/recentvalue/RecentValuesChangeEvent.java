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
package au.gov.asd.tac.constellation.utilities.gui.recentvalue;

import java.util.Collections;
import java.util.List;

/**
 * Recent Values Change Event
 *
 * @author ruby_crucis
 */
public class RecentValuesChangeEvent {

    private final String id;
    private final List<String> newValues;

    public RecentValuesChangeEvent(final String id, final List<String> newValues) {
        this.id = id;
        this.newValues = newValues;
    }

    public String getId() {
        return id;
    }

    public List<String> getNewValues() {
        return Collections.unmodifiableList(newValues);
    }

}
