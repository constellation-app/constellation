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
package au.gov.asd.tac.constellation.graph;

/**
 * A GraphIndexType represents the different types of index that can exist on a
 * graph attribute.
 *
 * @author sirius
 */
public enum GraphIndexType {

    /**
     * No index exists on this attribute. To search this attribute, a scan of
     * all elements must be performed.
     */
    NONE,
    /**
     * An unordered index exists on this graph. Usually this is a hash-based
     * index that can find matches but cannot perform range-based queries.
     */
    UNORDERED,
    /**
     * An ordered index exists on this graph. Usually this is a tree-based index
     * that can perform both exact match and range-based queries.
     */
    ORDERED
}
