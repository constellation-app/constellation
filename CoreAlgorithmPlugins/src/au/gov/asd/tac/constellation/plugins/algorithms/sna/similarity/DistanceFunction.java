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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity;

/**
 * A function that calculates the distance between two points.
 *
 * @author canis_majoris
 * @param <T>
 */
public interface DistanceFunction<T> {

    /**
     * Returns the distance between two points.
     *
     * @param firstPoint
     * @param secondPoint
     * @return
     */
    double getDistance(final T firstPoint, final T secondPoint);

}
