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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

/**
 *
 * @author cygnus_x-1
 */
public enum SelectionMode {

    REPLACE() {
        @Override
        public boolean calculateSelection(final boolean currentSelection, final boolean newSelection) {
            return newSelection;
        }
    },
    ADD() {
        @Override
        public boolean calculateSelection(final boolean currentSelection, final boolean newSelection) {
            return currentSelection || newSelection;
        }
    },
    INVERT() {
        @Override
        public boolean calculateSelection(final boolean currentSelection, final boolean newSelection) {
            return currentSelection ^ newSelection;
        }
    },
    REMOVE() {
        @Override
        public boolean calculateSelection(final boolean currentSelection, final boolean newSelection) {
            return currentSelection && !newSelection;
        }
    };

    public abstract boolean calculateSelection(final boolean currentSelection, final boolean newSelection);
}
