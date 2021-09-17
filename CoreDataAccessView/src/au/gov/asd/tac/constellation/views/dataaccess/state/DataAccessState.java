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
package au.gov.asd.tac.constellation.views.dataaccess.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A Data Access state object used to hold the state of the Data Access View per
 * graph (run tabs and the global parameters associated with them).
 * <p>
 * At the moment only a high-level state is stored which ignores the plugin
 * selection and any plugin-specific settings.
 *
 * @author arcturus
 */
public final class DataAccessState {
    private final List<Map<String, String>> state;

    public DataAccessState() {
        this.state = new ArrayList<>();
    }

    public void newTab() {
        state.add(new HashMap<>());
    }

    public void add(final String key, final String value) {
        state.get(state.size() - 1).put(key, value);
    }

    public List<Map<String, String>> getState() {
        return state;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DataAccessState rhs = (DataAccessState) o;

        return new EqualsBuilder()
                .append(getState(), rhs.getState())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getState())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("state", getState())
                .toString();
    }
}
