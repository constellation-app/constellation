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
package au.gov.asd.tac.constellation.views.timeline;

/**
 * Helper class used for passing time extents as a result of method operations.
 *
 * @author betelgeuse
 */
public class TimeExtents {

    public final long lowerTimeExtent;
    public final long upperTimeExtent;

    /**
     * Constructs a new <code>TimeExtents</code> instance.
     *
     * @param lowerTimeExtent the lower time extent.
     * @param upperTimeExtent the upper time extent.
     */
    public TimeExtents(final long lowerTimeExtent, final long upperTimeExtent) {
        this.lowerTimeExtent = lowerTimeExtent;
        this.upperTimeExtent = upperTimeExtent;
    }
}
