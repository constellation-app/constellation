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
package au.gov.asd.tac.constellation.views.histogram.formats;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter provides an alternative representation for the default value
 * that would be binned. If a formatter is defined, the alternative
 * representation is used to create bins and completely replaces the natural
 * value. An example is a date formatting BinFormatter that can return the
 * day-of-week as a String for a datetime. This will cause the histogram to have
 * only 7 bins representing each day of the week.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class BinFormatter implements Comparable<BinFormatter> {

    private static final Map<String, BinFormatter> FORMATTER_MAP = new HashMap<>();
    private static final List<BinFormatter> FORMATTER_LIST = new ArrayList<>();

    public static final BinFormatter DEFAULT_BIN_FORMATTER;

    static {
        init();
        DEFAULT_BIN_FORMATTER = FORMATTER_LIST.get(0);
    }

    private final String id;
    private final String label;
    private final int position;

    /**
     * Creates a new BinFormatter with a default label, position and id. The
     * label defaults to "No Format" while the position defaults to
     * {@link Integer#MIN_VALUE} meaning that it will always appear at the top
     * of any list. The id defaults to the simple name of BinFormatter class.
     *
     * @see BinFormatter#BinFormatter(java.lang.String, int, java.lang.String)
     */
    public BinFormatter() {
        this("No Format", Integer.MIN_VALUE);
    }

    /**
     * Creates a new BinFormatter with the specified label, position and id.
     *
     * @param label the label for this BinFormatter that the user will see.
     * @param position the position of this bin formatter in the list of
     * BinFormatters.
     * @param id a unique id for this BinFormatter.
     */
    public BinFormatter(final String label, final int position, final String id) {
        this.id = id;
        this.label = label;
        this.position = position;
    }

    /**
     * Creates a new BinFormatter with the specified label and position. The id
     * defaults to the simple name of the BinFormatter class.
     *
     * @param label the label for this BinFormatter that the user will see.
     * @param position the position of this bin formatter in the list of
     * BinFormatters.
     * @see BinFormatter#BinFormatter(java.lang.String, int, java.lang.String)
     */
    public BinFormatter(final String label, final int position) {
        this.id = getClass().getSimpleName();
        this.label = label;
        this.position = position;
    }

    /**
     * Returns the unique id for this BinFormatter.
     *
     * @return the unique id for this BinFormatter.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the user-visible label for this BinFormatter.
     *
     * @return the user-visible label for this BinFormatter.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the position of this BinFormatter. The position is used to order
     * a collection of BinFormatters when they are displayed in a list.
     *
     * @return the position of this BinFormatter.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns a String representation of this BinFormatter. The default
     * behaviour is simply to return the label.
     *
     * @return a String representation of this BinFormatter.
     */
    @Override
    public String toString() {
        return label;
    }

    /**
     * Compares this BinFormatter to another BinFormatter. BinFormatters are
     * ordered by their position values with lower numbers coming first in any
     * list.
     *
     * @param bf the other BinFormatter.
     *
     * @return the comparison result.
     */
    @Override
    public int compareTo(final BinFormatter bf) {
        return Integer.compare(position, bf.position);
    }

    /**
     * Returns true if this BinFormatter is capable of formatting a given bin.
     * If the BinFormatter returns false, it will be excluded from the list of
     * possible BinFormatters presented to the user for this bin type.
     *
     * @param bin the candidate bin.
     * @return true if this BinFormatter is capable of formatting a given bin.
     */
    public boolean appliesToBin(final Bin bin) {
        return true;
    }

    /**
     * Returns a collection of parameters that are needed to configure this
     * BinFormatter. If a BinFormatter requires no configuration, it should
     * return null which is the default behaviour. If a PluginParameters object
     * is returned, the resulting parameters dialog will be displayed to the
     * user when they choose this BinFormatter. This can be used in situations
     * such as where a user needs to specify a range size when binning by range.
     *
     * @return a collection of parameters that are needed to configure this
     * BinFormatter.
     *
     */
    public PluginParameters createParameters() {
        return null;
    }

    /**
     * Updates a collection of parameters that are needed to configure this
     * BinFormatter. If a BinFormatter requires no configuration, it should do
     * nothing which is the default behaviour.
     *
     * @param parameters the plugin parameter values specified by the user.
     *
     */
    public void updateParameters(PluginParameters parameters) {

    }

    /**
     * Creates a re-formatted bin to be used in place of the specified bin. This
     * method is called by the histogram framework for each bin to allow the
     * BinFormatter to specify a replacement bin.
     *
     * @param graph the graph providing the data points for the histogram.
     * @param attribute the attribute being binned.
     * @param parameters the plugin parameter values specified by the user.
     * @param bin the original bin to be replaced.
     * @return the newly created bin.
     */
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, Bin bin) {
        return bin;
    }

    public static void init() {
        if (FORMATTER_LIST.isEmpty()) {
            for (BinFormatter formatter : Lookup.getDefault().lookupAll(BinFormatter.class)) {
                FORMATTER_MAP.put(formatter.getId(), formatter);
                FORMATTER_LIST.add(formatter);
            }
            Collections.sort(FORMATTER_LIST);
        }
    }

    /**
     * Returns the BinFormatter with the specified id, or null if no such
     * BinFormatter exists.
     *
     * @param id the id of the required BinFormatter.
     * @return the BinFormatter with the specified id, or null if no such
     * BinFormatter exists.
     */
    public static BinFormatter getBinFormatter(final String id) {
        init();
        final BinFormatter formatter = FORMATTER_MAP.get(id);
        return formatter == null ? DEFAULT_BIN_FORMATTER : formatter;
    }

    /**
     * Returns all the registered BinFormatters that apply to the specified bin.
     *
     * @param bin the bin.
     * @return all the registered BinFormatters that apply to the specified bin.
     * @see
     * BinFormatter#appliesToBin(au.gov.asd.tac.constellation.views.histogram.Bin)
     */
    public static List<BinFormatter> getFormatters(final Bin bin) {
        init();
        final List<BinFormatter> formatters = new ArrayList<>();
        for (BinFormatter formatter : FORMATTER_LIST) {
            if (formatter.appliesToBin(bin)) {
                formatters.add(formatter);
            }
        }
        return formatters;
    }
}
