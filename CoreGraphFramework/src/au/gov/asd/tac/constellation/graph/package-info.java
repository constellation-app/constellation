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
/**
 * This package implements the low level data structures required to store and
 * manipulate a graph.
 * <p>
 * The Graph has no knowledge of a schema and does not rely on it. However,
 * since a schema is (nearly) always used by everything that uses a Graph, the
 * Graph module seems a reasonable place to put the schema stuff.
 */
package au.gov.asd.tac.constellation.graph;
