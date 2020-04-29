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
package au.gov.asd.tac.constellation.views.analyticview.translators;

import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.ColorVisualisation;

/**
 *
 * @author cygnus_x-1
 *
 * @param <R>
 * @param <C>
 */
public abstract class AbstractColorTranslator<R extends AnalyticResult<?>, C> extends GraphVisualisationTranslator<R, ColorVisualisation<C>> {

    protected static class InvalidElementTypeException extends RuntimeException {

        public InvalidElementTypeException(final String message) {
            super(message);
        }
    }

    public abstract void executePlugin(final boolean reset);
}
