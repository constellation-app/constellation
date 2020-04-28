/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.parameters;

/**
 * Interface for objects which need to be informed when recent values are
 * updated.
 *
 * @author ruby_crucis
 */
public interface RecentValuesListener {

    /**
     * Called when any recent value is added.
     *
     * @param e the change event.
     */
    public void recentValuesChanged(final RecentValuesChangeEvent e);
}
