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
package au.gov.asd.tac.constellation.graph.versioning;

import au.gov.asd.tac.constellation.graph.StoreGraph;

/**
 * Update Provider
 *
 * @author twilight_sparkle
 */
public interface UpdateProvider {

    public static final int DEFAULT_VERSION = 0;

    public UpdateItem getVersionedItem();

    public int getFromVersionNumber();

    public int getToVersionNumber();

    public abstract void configure(final StoreGraph graph);

    public void update(final StoreGraph graph);
}
