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
package au.gov.asd.tac.constellation.views.histogram.access;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.histogram.AttributeType;
import au.gov.asd.tac.constellation.views.histogram.HistogramTopComponent;
import org.openide.windows.WindowManager;

/**
 * HistogramAccess provides an API allowing other modules in the application to
 * control the histogram.
 *
 * @author twilight_sparkle
 */
public class HistogramAccess {

    private final HistogramTopComponent tc;

    private static final HistogramAccess SINGLETON = new HistogramAccess((HistogramTopComponent) WindowManager.getDefault().findTopComponent(HistogramTopComponent.class.getSimpleName()));

    /**
     * Returns the default HistogramAccess object that will control the
     * histogram in the UI of Constellation.
     *
     * @return the default HistogramAccess object that will control the
     * histogram in the UI of Constellation.
     */
    public static HistogramAccess getAccess() {
        return SINGLETON;
    }

    /**
     * Creates a new HistogramAccess to control the specified
     * HistographTopComponent.
     *
     * @param tc the HistogramTopComponent holding the histogram.
     */
    private HistogramAccess(final HistogramTopComponent tc) {
        this.tc = tc;
    }

    /**
     * Sets the current attribute that the histogram is binning on.
     *
     * @param elementType the element type the attribute applies to.
     * @param attribute the attribute to bin on.
     */
    public void setHistogramAttribute(final GraphElementType elementType, final String attribute) {
        tc.setHistogramViewOptions(elementType, AttributeType.ATTRIBUTE, attribute);
    }

    /**
     * Causes the histogram to request focus in the application.
     */
    public void requestHistogramActive() {
        if (tc == null) {
            return;
        }
        if (!tc.isOpened()) {
            tc.open();
        }
        tc.requestActive();
    }
}
