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
package au.gov.asd.tac.constellation.graph;

/**
 * GraphConstants holds a collection of int values used to manipulate and
 * examine a Graph object.
 *
 * @author sirius
 */
public interface GraphConstants {

    /**
     * Indicates that the source vertex of a link has a lower id than the
     * destination vertex of that link.
     */
    int UPHILL = 0;

    /**
     * Indicates that the source vertex of a link has a higher id than the
     * destination vertex of that link.
     */
    int DOWNHILL = 1;

    /**
     * Indicates that the source and destination vertices of a link have the
     * same id. In other words, this link forms a loop.
     */
    int FLAT = 2;

    /**
     * Indicates that an edge or transaction has a specified vertex as its
     * source.
     */
    int OUTGOING = 0;

    /**
     * Indicates that an edge or transaction has a specified vertex as its
     * destination.
     */
    int INCOMING = 1;

    /**
     * Indicates that an edge or transaction is undirected.
     */
    int UNDIRECTED = 2;

    /**
     * A value that provided by the graph to indicate that the requested element
     * does not exist in the graph.
     */
    int NOT_FOUND = -1107;
}
