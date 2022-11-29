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

//import com.sun.javafx.charts.ChartLayoutAnimator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.chart.Axis;
import javafx.util.StringConverter;

/**
 * An axis for representing Date objects, based on the DateAxis class available
 * in the ExtFX library.
 *
 * TODO: {@link ChartLayoutAnimator} is not longer supported, fix it.
 *
 * @author cygnus_x-1
 */
public class DateAxis extends Axis<Date> {

    /**
     * properties used for animation.
     */
    private final LongProperty currentLowerBound = new SimpleLongProperty(this, "currentLowerBound");
    private final LongProperty currentUpperBound = new SimpleLongProperty(this, "currentUpperBound");
//    private ChartLayoutAnimator animator = new ChartLayoutAnimator(this);
    private Object currentAnimationID;
    private Interval actualInterval = Interval.DECADE;

    /**
     * the min and max date of the data provided. if {@link #autoRanging} is
     * true, these are used as lower and upper bounds.
     */
    private Date minDate;
    private Date maxDate;

    private ObjectProperty<Date> lowerBound = new ObjectPropertyBase<Date>() {
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public Object getBean() {
            return DateAxis.this;
        }

        @Override
        public String getName() {
            return "lowerBound";
        }
    };

    private ObjectProperty<Date> upperBound = new ObjectPropertyBase<Date>() {
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public Object getBean() {
            return DateAxis.this;
        }

        @Override
        public String getName() {
            return "upperBound";
        }
    };

    private final ObjectProperty<StringConverter<Date>> tickLabelFormatter = new ObjectPropertyBase<StringConverter<Date>>() {
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public Object getBean() {
            return DateAxis.this;
        }

        @Override
        public String getName() {
            return "tickLabelFormatter";
        }
    };

    /**
     * Default constructor. The lower and upper bound are calculated by the
     * data.
     */
    public DateAxis() {
    }

    /**
     * Constructs a date axis with fixed lower and upper bounds.
     *
     * @param lowerBound The lower bound.
     * @param upperBound The upper bound.
     */
    public DateAxis(Date lowerBound, Date upperBound) {
        this();
        setAutoRanging(false);
        setLowerBound(lowerBound);
        setUpperBound(upperBound);
    }

    /**
     * Constructs a date axis with a label and fixed lower and upper bounds.
     *
     * @param axisLabel The label for the axis.
     * @param lowerBound The lower bound.
     * @param upperBound The upper bound.
     */
    public DateAxis(String axisLabel, Date lowerBound, Date upperBound) {
        this(lowerBound, upperBound);
        setLabel(axisLabel);
    }

    @Override
    public void invalidateRange(List<Date> list) {
        super.invalidateRange(list);

        Collections.sort(list);
        if (list.isEmpty()) {
            minDate = maxDate = new Date();
        } else if (list.size() == 1) {
            minDate = maxDate = list.get(0);
        } else if (list.size() > 1) {
            minDate = list.get(0);
            maxDate = list.get(list.size() - 1);
        } else {
            // Do nothing
        }
    }

    @Override
    protected Object autoRange(double length) {
        if (isAutoRanging()) {
            return new Object[]{minDate, maxDate};
        } else {
            if (getLowerBound() == null || getUpperBound() == null) {
                throw new IllegalArgumentException("If autoRanging is false, a lower and upper bound must be set.");
            }
            return getRange();
        }
    }

    @Override
    protected void setRange(Object range, boolean animating) {
        final Object[] r = (Object[]) range;
        final Date newLowerBound = (Date) r[0];
        final Date newUpperBound = (Date) r[1];
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
//            timeline.setOnFinished(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent actionEvent) {
//                    timer.stop();
//                    requestAxisLayout();
//                }
//            });
//            KeyValue keyValue = new KeyValue(currentLowerBound, lower.getTime());
//            KeyValue keyValue2 = new KeyValue(currentUpperBound, upper.getTime());
//            timeline.getKeyFrames().addAll(new KeyFrame(Duration.ZERO,
//                    new KeyValue(currentLowerBound, oldLowerBound.getTime()),
//                    new KeyValue(currentUpperBound, oldUpperBound.getTime())),
//                    new KeyFrame(Duration.millis(3000), keyValue, keyValue2));
//            timeline.play();

//            animator.stop(currentAnimationID);
//            currentAnimationID = animator.animate(
//                    new KeyFrame(Duration.ZERO,
//                            new KeyValue(currentLowerBound, oldLowerBound.getTime()),
//                            new KeyValue(currentUpperBound, oldUpperBound.getTime())
//                    ),
//                    new KeyFrame(Duration.millis(700),
//                            new KeyValue(currentLowerBound, newLowerBound.getTime()),
//                            new KeyValue(currentUpperBound, newUpperBound.getTime())
//                    )
//            );
        } else {
            currentLowerBound.set(getLowerBound().getTime());
            currentUpperBound.set(getUpperBound().getTime());
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
    public double getDisplayPosition(Date date) {
        final double length = getSide().isHorizontal() ? getWidth() : getHeight();

        // get the difference between the max and min date.
        double diff = currentUpperBound.get() - currentLowerBound.get();

        // get the range of the visible area (the min date should start at the zero position, so subtract it).
        double range = length - getZeroPosition();

        // get the difference from the actual date to the min date and divide it by the total difference.
        // this should be a value between 0 and 1, if the date is within the min and max date.
        double d = (date.getTime() - currentLowerBound.get()) / diff;

        // multiply this percent value with the range and add the zero offset.
        if (getSide().isVertical()) {
            return getHeight() - d * range + getZeroPosition();
        } else {
            return d * range + getZeroPosition();
        }
    }

    @Override
    public Date getValueForDisplay(double displayPosition) {
        final double length = getSide().isHorizontal() ? getWidth() : getHeight();

        // get the difference between the max and min date.
        double diff = currentUpperBound.get() - currentLowerBound.get();

        // get the range of the visible area (the min date should start at the zero position, so subtract it).
        double range = length - getZeroPosition();

        if (getSide().isVertical()) {
            final long date = (long) ((displayPosition - getZeroPosition() - getHeight()) / -range * diff + currentLowerBound.get());
            return new Date(date);
        } else {
            final long date = (long) ((displayPosition - getZeroPosition()) / range * diff + currentLowerBound.get());
            return new Date(date);
        }
    }

    @Override
    public boolean isValueOnAxis(Date date) {
        return date.getTime() > currentLowerBound.get() && date.getTime() < currentUpperBound.get();
    }

    @Override
    public double toNumericValue(Date date) {
        return date.getTime();
    }

    @Override
    public Date toRealValue(double v) {
        return new Date((long) v);
    }

    @Override
    protected List<Date> calculateTickValues(double v, Object range) {
        final Object[] r = (Object[]) range;
        final Date lowerBound = (Date) r[0];
        final Date upperBound = (Date) r[1];

        List<Date> dateList = new ArrayList<>();
        List<Date> previousDateList = new ArrayList<>();
        Interval previousInterval = Interval.values()[0];

        // the preferred gap which should be between two tick marks.
        final double averageTickGap = 100;
        final double averageTicks = v / averageTickGap;

        // starting with the greatest interval, add one of each calendar unit.
        Calendar calendar = Calendar.getInstance();
        for (Interval interval : Interval.values()) {
            calendar.setTime(lowerBound);
            dateList.clear();
            previousDateList.clear();
            actualInterval = interval;

            // loop as long we exceeded the upper bound.
            while (calendar.getTime().getTime() <= upperBound.getTime()) {
                dateList.add(calendar.getTime());
                calendar.add(interval.interval, interval.amount);
            }
            // check the size of the list, If it is greater than the amount of ticks, take that list.
            if (dateList.size() > averageTicks) {
                calendar.setTime(lowerBound);
                // recheck if the previous interval is better suited.
                while (calendar.getTime().getTime() <= upperBound.getTime()) {
                    previousDateList.add(calendar.getTime());
                    calendar.add(previousInterval.interval, previousInterval.amount);
                }
                break;
            }

            previousInterval = interval;
        }
        if (previousDateList.size() - averageTicks > averageTicks - dateList.size()) {
            dateList = previousDateList;
            actualInterval = previousInterval;
        }

        // finally, add the upper bound.
        dateList.add(upperBound);

        List<Date> evenDateList = makeDatesEven(dateList, calendar);
        // if there are at least three dates, check if the gap between the lower date and the second date
        // is at least half the gap of the second and third date, then repeat for the upper bound.
        // if gaps between dates are to small, remove one of them (this can occur, e.g. if the lower bound is 25.12.2013 and years are shown,
        // then the next year shown would be 2014 (01.01.2014) which would be too narrow to 25.12.2013).
        if (evenDateList.size() > 2) {

            final Date secondDate = evenDateList.get(1);
            final Date thirdDate = evenDateList.get(2);
            final Date lastDate = evenDateList.get(dateList.size() - 2);
            final Date previousLastDate = evenDateList.get(dateList.size() - 3);

            // if the second date is too near by the lower bound, remove it.
            if (secondDate.getTime() - lowerBound.getTime() < (thirdDate.getTime() - secondDate.getTime()) / 2) {
                evenDateList.remove(secondDate);
            }

            // if difference from the upper bound to the last date is less than the half of the difference of the previous two dates,
            // we better remove the last date, as it comes to close to the upper bound.
            if (upperBound.getTime() - lastDate.getTime() < (lastDate.getTime() - previousLastDate.getTime()) / 2) {
                evenDateList.remove(lastDate);
            }
        }

        return evenDateList;
    }

    @Override
    protected void layoutChildren() {
        if (!isAutoRanging()) {
            currentLowerBound.set(getLowerBound().getTime());
            currentUpperBound.set(getUpperBound().getTime());
        }
        super.layoutChildren();
    }

    @Override
    protected String getTickMarkLabel(Date date) {
        final StringConverter<Date> converter = getTickLabelFormatter();
        if (converter != null) {
            return converter.toString(date);
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        final DateFormat dateFormat;
        if (actualInterval.interval == Calendar.YEAR && calendar.get(Calendar.MONTH) == 0 && calendar.get(Calendar.DATE) == 1) {
            dateFormat = new SimpleDateFormat("yyyy");
        } else if (actualInterval.interval == Calendar.MONTH && calendar.get(Calendar.DATE) == 1) {
            dateFormat = new SimpleDateFormat("MMM yy");
        } else {
            switch (actualInterval.interval) {
                case Calendar.DATE:
                case Calendar.WEEK_OF_YEAR:
                case Calendar.HOUR:
                case Calendar.MINUTE:
                    dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                    break;
                case Calendar.SECOND:
                    dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
                    break;
                case Calendar.MILLISECOND:
                    dateFormat = DateFormat.getTimeInstance(DateFormat.FULL);
                    break;
                default:
                    dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                    break;
            }
        }
        return dateFormat.format(date);
    }

    /**
     * Makes dates even, in the sense of that years always begin in January,
     * months always begin on the 1st and days always at midnight.
     *
     * @param dates The list of dates.
     * @return The new list of dates.
     */
    private List<Date> makeDatesEven(List<Date> dates, Calendar calendar) {
        // if the dates contain more dates than just the lower and upper bounds, make the dates in between even.
        if (dates.size() > 2) {
            List<Date> evenDates = new ArrayList<>();

            // for each interval, modify the date slightly by a few millis, to make sure they are different days.
            // this is because Axis stores each value and won't update the tick labels, if the value is already known.
            // this happens if you display days and then add a date many years in the future the tick label will still be displayed as day.
            for (int i = 0; i < dates.size(); i++) {
                calendar.setTime(dates.get(i));
                switch (actualInterval.interval) {
                    case Calendar.YEAR:
                        // if it's not the first or last date (lower and upper bound), make the year begin with first month and let the months begin with first day.
                        if (i != 0 && i != dates.size() - 1) {
                            calendar.set(Calendar.MONTH, 0);
                            calendar.set(Calendar.DATE, 1);
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 6);
                        break;
                    case Calendar.MONTH:
                        // if it's not the first or last date (lower and upper bound), make the months begin with first day.
                        if (i != 0 && i != dates.size() - 1) {
                            calendar.set(Calendar.DATE, 1);
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 5);
                        break;
                    case Calendar.WEEK_OF_YEAR:
                        // make weeks begin with first day of week?
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 4);
                        break;
                    case Calendar.DATE:
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 3);
                        break;
                    case Calendar.HOUR:
                        if (i != 0 && i != dates.size() - 1) {
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                        }
                        calendar.set(Calendar.MILLISECOND, 2);
                        break;
                    case Calendar.MINUTE:
                        if (i != 0 && i != dates.size() - 1) {
                            calendar.set(Calendar.SECOND, 0);
                        }
                        calendar.set(Calendar.MILLISECOND, 1);
                        break;
                    case Calendar.SECOND:
                        calendar.set(Calendar.MILLISECOND, 0);
                        break;
                    default:
                        break;
                }
                evenDates.add(calendar.getTime());
            }

            return evenDates;
        } else {
            return dates;
        }
    }

    /**
     * Gets the lower bound of the axis.
     *
     * @return The property.
     * @see #getLowerBound()
     * @see #setLowerBound(java.util.Date)
     */
    public final ObjectProperty<Date> lowerBoundProperty() {
        return lowerBound;
    }

    /**
     * Gets the lower bound of the axis.
     *
     * @return The lower bound.
     * @see #lowerBoundProperty()
     */
    public final Date getLowerBound() {
        return lowerBound.get();
    }

    /**
     * Sets the lower bound of the axis.
     *
     * @param date The lower bound date.
     * @see #lowerBoundProperty()
     */
    public final void setLowerBound(Date date) {
        lowerBound.set(date);
    }

    /**
     * Gets the upper bound of the axis.
     *
     * @return The property.
     * @see #getUpperBound() ()
     * @see #setUpperBound(java.util.Date)
     */
    public final ObjectProperty<Date> upperBoundProperty() {
        return upperBound;
    }

    /**
     * Gets the upper bound of the axis.
     *
     * @return The upper bound.
     * @see #upperBoundProperty()
     */
    public final Date getUpperBound() {
        return upperBound.get();
    }

    /**
     * Sets the upper bound of the axis.
     *
     * @param date The upper bound date.
     * @see #upperBoundProperty() ()
     */
    public final void setUpperBound(Date date) {
        upperBound.set(date);
    }

    /**
     * Gets the tick label formatter for the ticks.
     *
     * @return The converter.
     */
    public final StringConverter<Date> getTickLabelFormatter() {
        return tickLabelFormatter.getValue();
    }

    /**
     * Sets the tick label formatter for the ticks.
     *
     * @param value The converter.
     */
    public final void setTickLabelFormatter(StringConverter<Date> value) {
        tickLabelFormatter.setValue(value);
    }

    /**
     * Gets the tick label formatter for the ticks.
     *
     * @return The property.
     */
    public final ObjectProperty<StringConverter<Date>> tickLabelFormatterProperty() {
        return tickLabelFormatter;
    }

    /**
     * The intervals, which are used for the tick labels. Beginning with the
     * largest interval, the axis tries to calculate the tick values for this
     * interval. If a smaller interval is better suited for, that one is taken.
     */
    private enum Interval {

        DECADE(Calendar.YEAR, 10),
        YEAR(Calendar.YEAR, 1),
        MONTH_6(Calendar.MONTH, 6),
        MONTH_3(Calendar.MONTH, 3),
        MONTH_1(Calendar.MONTH, 1),
        WEEK(Calendar.WEEK_OF_YEAR, 1),
        DAY(Calendar.DATE, 1),
        HOUR_12(Calendar.HOUR, 12),
        HOUR_6(Calendar.HOUR, 6),
        HOUR_3(Calendar.HOUR, 3),
        HOUR_1(Calendar.HOUR, 1),
        MINUTE_15(Calendar.MINUTE, 15),
        MINUTE_5(Calendar.MINUTE, 5),
        MINUTE_1(Calendar.MINUTE, 1),
        SECOND_15(Calendar.SECOND, 15),
        SECOND_5(Calendar.SECOND, 5),
        SECOND_1(Calendar.SECOND, 1),
        MILLISECOND(Calendar.MILLISECOND, 1);

        private final int amount;

        private final int interval;

        private Interval(int interval, int amount) {
            this.interval = interval;
            this.amount = amount;
        }
    }
}
