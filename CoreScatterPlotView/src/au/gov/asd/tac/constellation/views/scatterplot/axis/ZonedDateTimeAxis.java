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
package au.gov.asd.tac.constellation.views.scatterplot.axis;

import au.gov.asd.tac.constellation.utilities.temporal.TemporalConstants;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import java.time.Instant;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.chart.Axis;
import javafx.util.StringConverter;

/**
 * An axis for representing ZonedDateTime objects, based on the DateAxis class
 * available in the ExtFX library.
 *
 * TODO: {@link ChartLayoutAnimator} is not longer supported, fix it.
 *
 * @author cygnus_x-1
 */
public final class ZonedDateTimeAxis extends Axis<ZonedDateTime> {

    // properties used for animation.
    private final LongProperty currentLowerBound = new SimpleLongProperty(this, "currentLowerBound");
    private final LongProperty currentUpperBound = new SimpleLongProperty(this, "currentUpperBound");
//    private final ChartLayoutAnimator animator = new ChartLayoutAnimator(this);
    private Object currentAnimationID;
    private Interval actualInterval = Interval.DECADE;

    // the min and max datetime of the data provided.
    // if {@link #autoRanging} is true, these are used as lower and upper bounds.
    private ZonedDateTime minZonedDateTime;
    private ZonedDateTime maxZonedDateTime;

    private ObjectProperty<ZonedDateTime> lowerBound = new ObjectPropertyBase<ZonedDateTime>() {
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public Object getBean() {
            return ZonedDateTimeAxis.this;
        }

        @Override
        public String getName() {
            return "lowerBound";
        }
    };

    private ObjectProperty<ZonedDateTime> upperBound = new ObjectPropertyBase<ZonedDateTime>() {
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public Object getBean() {
            return ZonedDateTimeAxis.this;
        }

        @Override
        public String getName() {
            return "upperBound";
        }
    };

    private final ObjectProperty<StringConverter<ZonedDateTime>> tickLabelFormatter = new ObjectPropertyBase<StringConverter<ZonedDateTime>>() {
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public Object getBean() {
            return ZonedDateTimeAxis.this;
        }

        @Override
        public String getName() {
            return "tickLabelFormatter";
        }
    };

    /**
     * Default constructor. The lower and upper bounds are calculated using the
     * data.
     */
    public ZonedDateTimeAxis() {
    }

    /**
     * Constructs a datetime axis with fixed lower and upper bounds.
     *
     * @param lowerBound The lower bound.
     * @param upperBound The upper bound.
     */
    public ZonedDateTimeAxis(ZonedDateTime lowerBound, ZonedDateTime upperBound) {
        this();
        setAutoRanging(false);
        setLowerBound(lowerBound);
        setUpperBound(upperBound);
    }

    /**
     * Constructs a datetime axis with a label and fixed lower and upper bounds.
     *
     * @param axisLabel The label for the axis.
     * @param lowerBound The lower bound.
     * @param upperBound The upper bound.
     */
    public ZonedDateTimeAxis(String axisLabel, ZonedDateTime lowerBound, ZonedDateTime upperBound) {
        this(lowerBound, upperBound);
        setLabel(axisLabel);
    }

    @Override
    public void invalidateRange(List<ZonedDateTime> list) {
        super.invalidateRange(list);

        Collections.sort(list);
        if (list.isEmpty()) {
            minZonedDateTime = maxZonedDateTime = ZonedDateTime.now();
        } else if (list.size() == 1) {
            minZonedDateTime = maxZonedDateTime = list.get(0);
        } else if (list.size() > 1) {
            minZonedDateTime = list.get(0);
            maxZonedDateTime = list.get(list.size() - 1);
        } else {
            // Do nothing
        }
    }

    @Override
    protected Object autoRange(double length) {
        if (isAutoRanging()) {
            return new Object[]{minZonedDateTime, maxZonedDateTime};
        } else {
            if (getLowerBound() == null || getUpperBound() == null) {
                throw new IllegalArgumentException("If autoRanging is false, a lower and upper bound must be set.");
            }
            return getRange();
        }
    }

    @Override
    protected void setRange(Object range, boolean animating) {
        final Object[] dateTimeRange = (Object[]) range;
        final ZonedDateTime newLowerBound = (ZonedDateTime) dateTimeRange[0];
        final ZonedDateTime newUpperBound = (ZonedDateTime) dateTimeRange[1];
        setLowerBound(newLowerBound);
        setUpperBound(newUpperBound);

        if (animating) {
//            final Timeline timeline = new Timeline();
//            timeline.setAutoReverse(false);
//            timeline.setCycleCount(1);
//            final AnimationTimer timer = new AnimationTimer() {
//                @Override
//                public void handle(long l) {
//                    requestAxisLayout();
//                }
//            };
//            timer.start();
//            timeline.setOnFinished(actionEvent -> {
//                timer.stop();
//                requestAxisLayout();
//            });
//            KeyValue keyValue = new KeyValue(currentLowerBound, newLowerBound.toInstant().toEpochMilli());
//            KeyValue keyValue2 = new KeyValue(currentUpperBound, newUpperBound.toInstant().toEpochMilli());
//            timeline.getKeyFrames().addAll(new KeyFrame(Duration.ZERO,
//                    new KeyValue(currentLowerBound, oldLowerBound.toInstant().toEpochMilli()),
//                    new KeyValue(currentUpperBound, oldUpperBound.toInstant().toEpochMilli())),
//                    new KeyFrame(Duration.millis(3000), keyValue, keyValue2));
//            timeline.play();

//            animator.stop(currentAnimationID);
//            currentAnimationID = animator.animate(
//                    new KeyFrame(Duration.ZERO,
//                            new KeyValue(currentLowerBound, oldLowerBound.toInstant().toEpochMilli()),
//                            new KeyValue(currentUpperBound, oldUpperBound.toInstant().toEpochMilli())
//                    ),
//                    new KeyFrame(Duration.millis(700),
//                            new KeyValue(currentLowerBound, newLowerBound.toInstant().toEpochMilli()),
//                            new KeyValue(currentUpperBound, newUpperBound.toInstant().toEpochMilli())
//                    )
//            );
        } else {
            currentLowerBound.set(getLowerBound().toInstant().toEpochMilli());
            currentUpperBound.set(getUpperBound().toInstant().toEpochMilli());
        }
    }

    @Override
    protected Object getRange() {
        return new Object[]{getLowerBound(), getUpperBound()};
    }

    @Override
    public double getZeroPosition() {
        return 0;
    }

    @Override
    public double getDisplayPosition(ZonedDateTime datetime) {
        final double length = getSide().isHorizontal() ? getWidth() : getHeight();

        // get the difference between the max and min datetime.
        double diff = currentUpperBound.get() - currentLowerBound.get();

        // get the range of the visible area (the min datetime should start at the zero position, so subtract it).
        double range = length - getZeroPosition();

        // get the difference from the actual datetime to the min datetime and divide it by the total difference.
        // this should be a value between 0 and 1, if the datetime is within the min and max datetimes.
        double d = (datetime.toInstant().toEpochMilli() - currentLowerBound.get()) / diff;

        // multiply this percent value with the range and add the zero offset.
        if (getSide().isVertical()) {
            return getHeight() - d * range + getZeroPosition();
        } else {
            return d * range + getZeroPosition();
        }
    }

    @Override
    public ZonedDateTime getValueForDisplay(double displayPosition) {
        final double length = getSide().isHorizontal() ? getWidth() : getHeight();

        // get the difference between the max and min datetime.
        double diff = currentUpperBound.get() - currentLowerBound.get();

        // get the range of the visible area (the min datetime should start at the zero position, so subtract it).
        double range = length - getZeroPosition();

        if (getSide().isVertical()) {
            final long datetime = (long) ((displayPosition - getZeroPosition() - getHeight()) / -range * diff + currentLowerBound.get());
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(datetime), TimeZoneUtilities.UTC);
        } else {
            final long datetime = (long) ((displayPosition - getZeroPosition()) / range * diff + currentLowerBound.get());
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(datetime), TimeZoneUtilities.UTC);
        }
    }

    @Override
    public boolean isValueOnAxis(ZonedDateTime date) {
        return date.toInstant().toEpochMilli() > currentLowerBound.get() && date.toInstant().toEpochMilli() < currentUpperBound.get();
    }

    @Override
    public double toNumericValue(ZonedDateTime date) {
        return date.toInstant().toEpochMilli();
    }

    @Override
    public ZonedDateTime toRealValue(double v) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli((long) v), TimeZoneUtilities.UTC);
    }

    @Override
    protected List<ZonedDateTime> calculateTickValues(double v, Object range) {
        final Object[] dateTimeRange = (Object[]) range;
        final ZonedDateTime lowerBound = (ZonedDateTime) dateTimeRange[0];
        final ZonedDateTime upperBound = (ZonedDateTime) dateTimeRange[1];

        List<ZonedDateTime> dateTimeList = new ArrayList<>();
        List<ZonedDateTime> previousDateTimeList = new ArrayList<>();
        Interval previousInterval = Interval.values()[0];

        // calculate the gap which should be between two tick marks.
        final double averageTickGap = 100;
        final double averageTicks = v / averageTickGap;

        // starting with the greatest interval, add one of each calendar unit.
        ZonedDateTime datetime = lowerBound;
        for (Interval interval : Interval.values()) {
            datetime = lowerBound;
            dateTimeList.clear();
            previousDateTimeList.clear();
            actualInterval = interval;

            // loop as long we exceeded the upper bound.
            while (datetime.toInstant().toEpochMilli() <= upperBound.toInstant().toEpochMilli()) {
                dateTimeList.add(datetime);
                datetime = datetime.plus(interval.amount, interval.interval);
            }
            // check the size of the list, if it is greater than the amount of ticks, take that list.
            if (dateTimeList.size() > averageTicks) {
                datetime = lowerBound;
                // recheck if the previous interval is better suited.
                while (datetime.toInstant().toEpochMilli() <= upperBound.toInstant().toEpochMilli()) {
                    previousDateTimeList.add(datetime);
                    datetime = datetime.plus(previousInterval.amount, previousInterval.interval);
                }
                break;
            }

            previousInterval = interval;
        }
        if (previousDateTimeList.size() - averageTicks > averageTicks - dateTimeList.size()) {
            dateTimeList = previousDateTimeList;
            actualInterval = previousInterval;
        }

        // finally, add the upper bound.
        dateTimeList.add(upperBound);

        List<ZonedDateTime> evenDateTimeList = makeDateTimesEven(dateTimeList);
        // if there are at least three datetimes, check if the gap between the lower datetime and the second datetime
        // is at least half the gap of the second and third datetime, then repeat for the upper bound.
        // if gaps between datetimes are too small, remove one of them (this can occur, e.g. if the lower bound is 25.12.2013 and years are shown;
        // then the next year shown would be 2014 (01.01.2014) which would be too narrow to 25.12.2013).
        if (evenDateTimeList.size() > 2) {
            final ZonedDateTime secondDateTime = evenDateTimeList.get(1);
            final ZonedDateTime thirdDateTime = evenDateTimeList.get(2);
            final ZonedDateTime lastDateTime = evenDateTimeList.get(dateTimeList.size() - 2);
            final ZonedDateTime previousLastDateTime = evenDateTimeList.get(dateTimeList.size() - 3);

            // if the second date is too near by the lower bound, remove it.
            if (secondDateTime.toInstant().toEpochMilli() - lowerBound.toInstant().toEpochMilli() < (thirdDateTime.toInstant().toEpochMilli() - secondDateTime.toInstant().toEpochMilli()) / 2) {
                evenDateTimeList.remove(secondDateTime);
            }

            // if difference from the upper bound to the last date is less than the half of the difference of the previous two dates,
            // we better remove the last date, as it comes to close to the upper bound.
            if (upperBound.toInstant().toEpochMilli() - lastDateTime.toInstant().toEpochMilli() < (lastDateTime.toInstant().toEpochMilli() - previousLastDateTime.toInstant().toEpochMilli()) / 2) {
                evenDateTimeList.remove(lastDateTime);
            }
        }

        return evenDateTimeList;
    }

    @Override
    protected void layoutChildren() {
        if (!isAutoRanging()) {
            currentLowerBound.set(getLowerBound().toInstant().toEpochMilli());
            currentUpperBound.set(getUpperBound().toInstant().toEpochMilli());
        }
        super.layoutChildren();
    }

    @Override
    protected String getTickMarkLabel(ZonedDateTime datetime) {
        final StringConverter<ZonedDateTime> converter = getTickLabelFormatter();
        if (converter != null) {
            return converter.toString(datetime);
        }

        final DateTimeFormatter formatter;
        if (actualInterval.interval == ChronoUnit.YEARS && datetime.getMonth() == Month.JANUARY && datetime.getDayOfMonth() == 1) {
            formatter = DateTimeFormatter.ofPattern("yyyy");
        } else if (actualInterval.interval == ChronoUnit.MONTHS && datetime.getDayOfMonth() == 1) {
            formatter = DateTimeFormatter.ofPattern("MMM yy");
        } else {
            switch (actualInterval.interval) {
                case DAYS:
                case WEEKS:
                case HOURS:
                case MINUTES:
                    formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
                    break;
                case SECONDS:
                    formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
                    break;
                case MILLIS:
                    formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
                    break;
                default:
                    formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
                    break;
            }
        }
        return formatter.format(datetime);
    }

    /**
     * Makes datetimes even, in the sense of that years always begin in January,
     * months always begin on the 1st and days always at midnight.
     *
     * @param dateTimes The list of dates.
     * @return The new list of dates.
     */
    private List<ZonedDateTime> makeDateTimesEven(final List<ZonedDateTime> dateTimes) {
        // if the dates contain more dates than just the lower and upper bounds, make the dates in between even.
        if (dateTimes.size() > 2) {
            List<ZonedDateTime> evenDateTimes = new ArrayList<>();

            // for each interval, modify the date slightly by a few millis, to make sure they are different days.
            // this is because Axis stores each value and won't update the tick labels, if the value is already known.
            // this happens if you display days and then add a date many years in the future the tick label will still be displayed as day.
            for (int i = 0; i < dateTimes.size(); i++) {
                ZonedDateTime evenDateTime = dateTimes.get(i);
                switch (actualInterval.interval) {
                    case YEARS:
                        // if it's not the first or last date (lower and upper bound), make the year begin with first month and let the months begin with first day.
                        if (i != 0 && i != dateTimes.size() - 1) {
                            evenDateTime = evenDateTime.withMonth(0);
                            evenDateTime = evenDateTime.withDayOfMonth(1);
                        }
                        evenDateTime = evenDateTime.withHour(0);
                        evenDateTime = evenDateTime.withMinute(0);
                        evenDateTime = evenDateTime.withSecond(0);
                        evenDateTime = evenDateTime.withNano(6 * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
                        break;
                    case MONTHS:
                        // if it's not the first or last date (lower and upper bound), make the months begin with first day.
                        if (i != 0 && i != dateTimes.size() - 1) {
                            evenDateTime = evenDateTime.withDayOfMonth(1);
                        }
                        evenDateTime = evenDateTime.withHour(0);
                        evenDateTime = evenDateTime.withMinute(0);
                        evenDateTime = evenDateTime.withSecond(0);
                        evenDateTime = evenDateTime.withNano(5 * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
                        break;
                    case WEEKS:
                        // make weeks begin with first day of week?
                        evenDateTime = evenDateTime.withHour(0);
                        evenDateTime = evenDateTime.withMinute(0);
                        evenDateTime = evenDateTime.withSecond(0);
                        evenDateTime = evenDateTime.withNano(4 * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
                        break;
                    case DAYS:
                        evenDateTime = evenDateTime.withHour(0);
                        evenDateTime = evenDateTime.withMinute(0);
                        evenDateTime = evenDateTime.withSecond(0);
                        evenDateTime = evenDateTime.withNano(3 * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
                        break;
                    case HOURS:
                        if (i != 0 && i != dateTimes.size() - 1) {
                            evenDateTime = evenDateTime.withMinute(0);
                            evenDateTime = evenDateTime.withSecond(0);
                        }
                        evenDateTime = evenDateTime.withNano(2 * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
                        break;
                    case MINUTES:
                        if (i != 0 && i != dateTimes.size() - 1) {
                            evenDateTime = evenDateTime.withSecond(0);
                        }
                        evenDateTime = evenDateTime.withNano(1 * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
                        break;
                    case SECONDS:
                        evenDateTime = evenDateTime.withNano(0 * TemporalConstants.NANOSECONDS_IN_MILLISECOND);
                        break;
                    default:
                        break;
                }
                evenDateTimes.add(evenDateTime);
            }

            return evenDateTimes;
        } else {
            return dateTimes;
        }
    }

    /**
     * Gets the lower bound of the axis.
     *
     * @return The property.
     * @see #getLowerBound()
     * @see #setLowerBound(java.util.Date)
     */
    public final ObjectProperty<ZonedDateTime> lowerBoundProperty() {
        return lowerBound;
    }

    /**
     * Gets the lower bound of the axis.
     *
     * @return The lower bound.
     * @see #lowerBoundProperty()
     */
    public final ZonedDateTime getLowerBound() {
        return lowerBound.get();
    }

    /**
     * Sets the lower bound of the axis.
     *
     * @param date The lower bound date.
     * @see #lowerBoundProperty()
     */
    public final void setLowerBound(ZonedDateTime date) {
        lowerBound.set(date);
    }

    /**
     * Gets the upper bound of the axis.
     *
     * @return The property.
     * @see #getUpperBound() ()
     * @see #setUpperBound(java.util.Date)
     */
    public final ObjectProperty<ZonedDateTime> upperBoundProperty() {
        return upperBound;
    }

    /**
     * Gets the upper bound of the axis.
     *
     * @return The upper bound.
     * @see #upperBoundProperty()
     */
    public final ZonedDateTime getUpperBound() {
        return upperBound.get();
    }

    /**
     * Sets the upper bound of the axis.
     *
     * @param date The upper bound date.
     * @see #upperBoundProperty() ()
     */
    public final void setUpperBound(ZonedDateTime date) {
        upperBound.set(date);
    }

    /**
     * Gets the tick label formatter for the ticks.
     *
     * @return The property.
     */
    public final ObjectProperty<StringConverter<ZonedDateTime>> tickLabelFormatterProperty() {
        return tickLabelFormatter;
    }

    /**
     * Gets the tick label formatter for the ticks.
     *
     * @return The converter.
     */
    public final StringConverter<ZonedDateTime> getTickLabelFormatter() {
        return tickLabelFormatter.getValue();
    }

    /**
     * Sets the tick label formatter for the ticks.
     *
     * @param value The converter.
     */
    public final void setTickLabelFormatter(StringConverter<ZonedDateTime> value) {
        tickLabelFormatter.setValue(value);
    }

    /**
     * The intervals, which are used for the tick labels. Beginning with the
     * largest interval, the axis tries to calculate the tick values for this
     * interval. If a smaller interval is better suited for, that one is taken.
     */
    private enum Interval {

        DECADE(ChronoUnit.DECADES, 1),
        YEAR(ChronoUnit.YEARS, 1),
        MONTH_6(ChronoUnit.MONTHS, 6),
        MONTH_3(ChronoUnit.MONTHS, 3),
        MONTH_1(ChronoUnit.MONTHS, 1),
        WEEK(ChronoUnit.WEEKS, 1),
        DAY(ChronoUnit.DAYS, 1),
        HOUR_12(ChronoUnit.HOURS, 12),
        HOUR_6(ChronoUnit.HOURS, 6),
        HOUR_3(ChronoUnit.HOURS, 3),
        HOUR_1(ChronoUnit.HOURS, 1),
        MINUTE_15(ChronoUnit.MINUTES, 15),
        MINUTE_5(ChronoUnit.MINUTES, 5),
        MINUTE_1(ChronoUnit.MINUTES, 1),
        SECOND_15(ChronoUnit.SECONDS, 15),
        SECOND_5(ChronoUnit.SECONDS, 5),
        SECOND_1(ChronoUnit.SECONDS, 1),
        MILLISECOND(ChronoUnit.MILLIS, 1);

        private final ChronoUnit interval;
        private final int amount;

        private Interval(ChronoUnit interval, int amount) {
            this.interval = interval;
            this.amount = amount;
        }
    }
}
