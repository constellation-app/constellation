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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

/**
 * enumerations for link weights
 *
 * @author algol
 */
public enum LinkWeight {

    USE_EXTENTS {
        @Override
        public String toString() {
            return "Use extents";
        }
    },
    IGNORE {
        @Override
        public String toString() {
            return "Ignore";
        }
    },
    USE {
        @Override
        public String toString() {
            return "Use";
        }
    },
    USE_SQUARES {
        @Override
        public String toString() {
            return "Use squares";
        }
    },
    USE_INVERSE {
        @Override
        public String toString() {
            return "Use inverse";
        }
    };

    public static LinkWeight defaultEnum() {
        return USE_EXTENTS;
    }
}
