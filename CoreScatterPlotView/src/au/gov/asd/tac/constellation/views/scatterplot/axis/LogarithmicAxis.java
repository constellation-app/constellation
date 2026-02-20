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
package au.gov.asd.tac.constellation.views.scatterplot.axis;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.chart.ValueAxis;

/**
 * An axis for representing numeric data logarithmically, based on the
 * LogarithmicAxis class available in the ExtFX library.
 *
 * TODO: {@link ChartLayoutAnimator} is not longer supported, fix it.
 * Commented out code in this class is related to this issue. Ticket has been created to address.
 * @author cygnus_x-1
 */
public class LogarithmicAxis extends ValueAxis<Number> {

    private final DoubleProperty currentUpperBound = new SimpleDoubleProperty();
//    private final ChartLayoutAnimator animator = new ChartLayoutAnimator(this);
//    private Object currentAnimationID;

    /**
     * Default constructor. Creates an auto-ranging logarithmic axis.
     */
    public LogarithmicAxis() {
        super();
    }

    /**
     * Creates a non-auto-ranging logarithmic axis with the given lower and
     * upper bound.
     *
     * @param lowerBound The lower bound.
     * @param upperBound The upper bound.
     */
    public LogarithmicAxis(final double lowerBound, final double upperBound) {
        super(lowerBound, upperBound);
    }

    @Override
    protected Object autoRange(double minValue, final double maxValue, final double length, final double labelSize) {
        if (isAutoRanging()) {
            if (minValue == 0) { // Can only be reached if graph is empty due to ChartBuilder:77,85 checks.
                minValue = 1; //When graph is empty set override default minimum of 0 (which is incomaptible with the logartihmic axis) and set it to 1. This results in behaviour similiar to that of the non-log axis for an empty chart.
            }
            return new double[]{minValue, maxValue};
        } else {
            return getRange();
        }
    }

    @Override
    protected void layoutChildren() {
        if (!isAutoRanging()) {
            currentLowerBound.set(getLowerBound());
            currentUpperBound.set(getUpperBound());
        }
        super.layoutChildren();
    }

    @Override
    public double getDisplayPosition(final Number value) {
        // get the logarithmic difference between the value and the lower bound.
        final double diffValue = Math.log10(value.doubleValue() / currentLowerBound.get());

        // get the logarithmic difference between lower and upper bound.
        final double diffTotal = Math.log10(currentUpperBound.get() / currentLowerBound.get());

        final double percent = diffValue / diffTotal;

        if (getSide().isHorizontal()) {
            return percent * getWidth();
        } else {
            // Invert for the vertical axis.
            return (1 - percent) * getHeight();
        }
    }

    @Override
    public Number getValueForDisplay(final double displayPosition) {
        // this is basically only the equivalence transformation of the getDisplayPosition method.
        if (getSide().isHorizontal()) {
            return Math.pow(10, displayPosition / getWidth() * Math.log10(currentUpperBound.get() / currentLowerBound.get())) * currentLowerBound.get();
        } else {
            return Math.pow(10, ((displayPosition / getHeight()) - 1) * -Math.log10(currentUpperBound.get() / currentLowerBound.get())) * currentLowerBound.get();
        }
    }

    @Override
    protected void setRange(final Object range, final boolean animate) {
        final double lowerBound = ((double[]) range)[0];
        final double upperBound = ((double[]) range)[1];
        final double[] r = (double[]) range;
        final double lower = r[0];
        final double upper = r[1];

        setLowerBound(lower);
        setUpperBound(upper);

        if (animate) {
//            animator.stop(currentAnimationID);
//            currentAnimationID = animator.animate(
//                    new KeyFrame(Duration.ZERO,
//                            new KeyValue(currentLowerBound, oldLowerBound),
//                            new KeyValue(currentUpperBound, oldUpperBound)
//                    ),
//                    new KeyFrame(Duration.millis(700),
//                            new KeyValue(currentLowerBound, lower),
//                            new KeyValue(currentUpperBound, upper)
//                    )
//            );
        } else {
            currentLowerBound.set(lowerBound);
            currentUpperBound.set(upperBound);
        }
    }

    @Override
    protected double[] getRange() {
        return new double[]{getLowerBound(), getUpperBound()};
    }

    @Override
    protected List<Number> calculateTickValues(final double length, final Object range) {
        final List<Number> tickValues = new ArrayList<>();

        final double[] rangeProps = (double[]) range;
        final double lowerBound = rangeProps[0];
        final double upperBound = rangeProps[1];
        final double logLowerBound = Math.log10(lowerBound);
        final double logUpperBound = Math.log10(upperBound);

        // we should always start with an "even" integer, so floor the start value
        // (otherwise the scale would contain odd values, rather then normal 1, 2, 3, 4, ... values)
        for (double major = Math.floor(logLowerBound); major < logUpperBound; major++) {
            final double p = Math.pow(10, major);
            for (double j = 1; j < 10; j++) {
                tickValues.add(j * p);
            }
        }
        return tickValues;
    }

    @Override
    protected List<Number> calculateMinorTickMarks() {
        final List<Number> minorTickMarks = new ArrayList<>();
        final double step = 1.0 / getMinorTickCount();
        final double logLowerBound = Math.log10(getLowerBound());
        final double logUpperBound = Math.log10(getUpperBound());

        for (double major = Math.floor(logLowerBound); major < logUpperBound; major++) {
            for (double j = 0; j < 10; j += step) {
                minorTickMarks.add(j * Math.pow(10, major));
            }
        }

        return minorTickMarks;
    }

    @Override
    protected String getTickMarkLabel(final Number value) {
        final NumberFormat formatter = NumberFormat.getInstance();
        return formatter.format(value);
    }
}
