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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;

/**
 * A Hashmod that will allow setting of attributes from a key attribute's value
 *
 * @author CrucisGamma
 */
public class Hashmod {

    public static final String ATTRIBUTE_NAME = "hashmod";

    public Hashmod() {
    }

    /**
     * Create a new Hashmod.
     *
     */
    public Hashmod(final boolean active, final int level, final String text, final ConstellationColor fgColor, final ConstellationColor bgColor, final String template) {
    }

    @Override
    public String toString() {
        return "";
    }
}
