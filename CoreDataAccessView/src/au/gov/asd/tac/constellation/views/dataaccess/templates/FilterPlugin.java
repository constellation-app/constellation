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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Filter Plugin
 *
 * @author cygnus_x-1
 */
public abstract class FilterPlugin extends SimpleEditPlugin {

    public static final String FILTER_TYPE_PARAMETER_ID = PluginParameter.buildId(FilterPlugin.class, "filter_type");

    protected static final String FILTER_TYPE_VALUE_PROVIDED_DOES_NOT_MATCH = "Filter type value provided does not match a known trust level.";

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> filterType = SingleChoiceParameterType.build(FILTER_TYPE_PARAMETER_ID, FilterTypeParameterValue.class);
        filterType.setName("Filter Type");
        filterType.setDescription("The name of the filter");
        final List<FilterTypeParameterValue> filterTypes = new ArrayList<>();
        filterTypes.add(new FilterTypeParameterValue(FilterType.REMOVE_FILTER));
        filterTypes.add(new FilterTypeParameterValue(FilterType.DESELECT_FILTER));
        filterTypes.add(new FilterTypeParameterValue(FilterType.SELECT_FILTER));
        SingleChoiceParameterType.setOptionsData(filterType, filterTypes);
        SingleChoiceParameterType.setChoiceData(filterType, filterTypes.get(0));
        parameters.addParameter(filterType);

        return parameters;
    }

    protected enum FilterType {

        REMOVE_FILTER("Remove"),
        DESELECT_FILTER("Deselect"),
        SELECT_FILTER("Select");

        private final String filterTypeName;

        private FilterType(String filterTypeName) {
            this.filterTypeName = filterTypeName;
        }

        public String getFilterTypeName() {
            return filterTypeName;
        }
    }

    public static class FilterTypeParameterValue extends ParameterValue implements Comparable<FilterTypeParameterValue> {

        private FilterType filterType;

        public FilterTypeParameterValue() {
            this.filterType = null;
        }

        public FilterTypeParameterValue(final FilterType filterType) {
            this.filterType = filterType;
        }

        public FilterType getFilterType() {
            return filterType;
        }

        @Override
        public String validateString(String s) {
            for (FilterType trustLevelConstant : FilterType.values()) {
                if (trustLevelConstant.getFilterTypeName().equals(s)) {
                    return null;
                }
            }

            return FILTER_TYPE_VALUE_PROVIDED_DOES_NOT_MATCH;
        }

        @Override
        public boolean setStringValue(String s) {
            if (s == null) {
                return false;
            }

            for (FilterType filterTypeConstant : FilterType.values()) {
                if (s.equals(filterTypeConstant.getFilterTypeName())) {
                    this.filterType = filterTypeConstant;
                    return true;
                }
            }

            return false;
        }

        @Override
        public Object getObjectValue() {
            return this;
        }

        @Override
        public boolean setObjectValue(Object o) {
            final FilterTypeParameterValue t = (FilterTypeParameterValue) o;
            if (this.filterType != t.filterType) {
                this.filterType = t.filterType;
                return true;
            }

            return false;
        }

        @Override
        protected ParameterValue createCopy() {
            return new FilterTypeParameterValue(filterType);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + Objects.hashCode(this.filterType);
            return hash;
        }

        @Override
        public boolean equals(final Object object) {
            if (object == null) {
                return false;
            }
            if (getClass() != object.getClass()) {
                return false;
            }
            final FilterTypeParameterValue other = (FilterTypeParameterValue) object;
            return Objects.equals(this.filterType, other.filterType);
        }

        @Override
        public String toString() {
            return String.format("%s", filterType.filterTypeName);
        }

        @Override
        public int compareTo(FilterTypeParameterValue filterTypeParameter) {
            return this.filterType.compareTo(filterTypeParameter.filterType);
        }
    }
}
