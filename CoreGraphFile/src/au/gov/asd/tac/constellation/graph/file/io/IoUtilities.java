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
package au.gov.asd.tac.constellation.graph.file.io;

import au.gov.asd.tac.constellation.graph.GraphElementType;

/**
 * A collection of convenient functions.
 *
 * @author algol
 */
public final class IoUtilities {
    private IoUtilities() {
    }
    
    /**
     * Convert a GraphElementType to a String in an implementation-independent
     * way.
     *
     * @param type The GraphElementType to convert.
     *
     * @return A String representation of the given GraphElementType.
     */
    public static String getGraphElementTypeString(final GraphElementType type) {
        if (null == type) {
            throw new IllegalArgumentException("Unwanted GraphElementType: " + type);
        } else switch (type) {
            case GRAPH:
                return "graph";
            case VERTEX:
                return "vertex";
            case TRANSACTION:
                return "transaction";
            case META:
                return "meta";
            default:
                throw new IllegalArgumentException("Unwanted GraphElementType: " + type);
        }
    }
}
