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
package au.gov.asd.tac.constellation.plugins.arrangements.grid;

import java.util.ArrayList;
import java.util.List;

/**
 * list of available grid arrangement options
 *
 * @author algol
 */
public enum GridChoice {

    SQUARE {
        @Override
        public String toString() {
            return "Square";
        }
    },
    HORIZONTAL_LINE {
        @Override
        public String toString() {
            return "Horizontal line";
        }
    },
    VERTICAL_LINE {
        @Override
        public String toString() {
            return "Vertical line";
        }
    },
    TWO_ROWS {
        @Override
        public String toString() {
            return "Two rows";
        }
    },
    THREE_ROWS {
        @Override
        public String toString() {
            return "Three rows";
        }
    },
    FOUR_ROWS {
        @Override
        public String toString() {
            return "Four rows";
        }
    },
    TWO_COLUMNS {
        @Override
        public String toString() {
            return "Two columns";
        }
    },
    THREE_COLUMNS {
        @Override
        public String toString() {
            return "Three columns";
        }
    },
    FOUR_COLUMNS {
        @Override
        public String toString() {
            return "Four columns";
        }
    };

    public static GridChoice defaultEnum() {
        return SQUARE;
    }

    public static GridChoice getValue(final String value) {
        if (value.equalsIgnoreCase(FOUR_COLUMNS.toString())) {
            return FOUR_COLUMNS;
        } else if (value.equalsIgnoreCase(FOUR_ROWS.toString())) {
            return FOUR_ROWS;
        } else if (value.equalsIgnoreCase(THREE_COLUMNS.toString())) {
            return THREE_COLUMNS;
        } else if (value.equalsIgnoreCase(THREE_ROWS.toString())) {
            return THREE_ROWS;
        } else if (value.equalsIgnoreCase(TWO_COLUMNS.toString())) {
            return TWO_COLUMNS;
        } else if (value.equalsIgnoreCase(TWO_ROWS.toString())) {
            return TWO_ROWS;
        } else if (value.equalsIgnoreCase(HORIZONTAL_LINE.toString())) {
            return HORIZONTAL_LINE;
        } else if (value.equalsIgnoreCase(VERTICAL_LINE.toString())) {
            return VERTICAL_LINE;
        } else {
            return SQUARE;
        }
    }

    public static List<String> getChoices() {
        final List<String> list = new ArrayList<>();
        list.add(SQUARE.toString());
        list.add(HORIZONTAL_LINE.toString());
        list.add(VERTICAL_LINE.toString());
        list.add(TWO_COLUMNS.toString());
        list.add(TWO_ROWS.toString());
        list.add(THREE_COLUMNS.toString());
        list.add(THREE_ROWS.toString());
        list.add(FOUR_COLUMNS.toString());
        list.add(FOUR_ROWS.toString());
        return list;
    }
}
