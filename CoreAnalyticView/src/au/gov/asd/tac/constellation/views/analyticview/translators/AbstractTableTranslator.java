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
package au.gov.asd.tac.constellation.views.analyticview.translators;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.TableVisualisation;

/**
 *
 * @author cygnus_x-1
 *
 * @param <R> the AnalyticResult type being represented by this table
 * @param <C> the data type given to each cell in the table
 */
public abstract class AbstractTableTranslator<R extends AnalyticResult<?>, C> extends InternalVisualisationTranslator<R, TableVisualisation<?>> {

    protected static class UnrecognisedColumnException extends RuntimeException {

        private static final String UNRECOGNISED_COLUMN_EXCEPTION_STRING = "Column not recognised: ";

        public UnrecognisedColumnException(final String columnName) {
            super(UNRECOGNISED_COLUMN_EXCEPTION_STRING + columnName);
        }
    }

    public abstract Object getCellData(final C cellValue, final String columnName);

    public abstract String getCellText(final C cellValue, final Object cellItem, final String columnName);

    public abstract ConstellationColor getCellColor(final C cellValue, final Object cellItem, final String columnName);
}
