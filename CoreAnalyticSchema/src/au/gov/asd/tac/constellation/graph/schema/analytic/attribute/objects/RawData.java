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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * An object which represents both the identifier and type of some entity. This
 * is typically used to hold data that originated elsewhere in its 'raw' form
 * (ie. prior to processing by the CONSTELLATION schema). This is useful to have
 * as it allows data to be related more easily to the original source.
 *
 * @author cygnus_x-1
 */
public class RawData implements Comparable<RawData> {

    public static final Pattern IDENTIFIER_WITH_TYPE = Pattern.compile("^(.*)<([^>]*)>$");

    private final String rawIdentifier;
    private final String rawType;

    public RawData(final String rawIdentifier, final String rawType) {
        this.rawIdentifier = rawIdentifier;
        this.rawType = rawType;
    }

    public RawData(final String rawData) {
        if (rawData == null) {
            this.rawIdentifier = null;
            this.rawType = null;
        } else {
            final Matcher m = IDENTIFIER_WITH_TYPE.matcher(rawData.trim());
            if (m.matches()) {
                this.rawIdentifier = m.group(1);
                this.rawType = m.group(2);
            } else {
                this.rawIdentifier = rawData;
                this.rawType = null;
            }
        }
    }

    /**
     * Check if this RawData has a rawIdentifier property.
     *
     * @return true if this RawData has a rawIdentifier property.
     */
    public boolean hasRawIdentifier() {
        return StringUtils.isNotBlank(rawIdentifier);
    }

    /**
     * Get the rawIdentifier property of this RawData.
     *
     * @return the rawIdentifier property of this RawData.
     */
    public String getRawIdentifier() {
        return rawIdentifier;
    }

    /**
     * Check if this RawData has a rawType property.
     *
     * @return true if this RawData has a rawType property.
     */
    public boolean hasRawType() {
        return StringUtils.isNotBlank(rawType);
    }

    /**
     * Get the rawType property of this RawData.
     *
     * @return the rawType property of this RawData.
     */
    public String getRawType() {
        return rawType;
    }

    /**
     * Given two RawData objects, this will return a merged RawData object which
     * takes properties from the primary object over the secondary object.
     *
     * @param primaryValue the primary RawData object.
     * @param secondaryValue the secondary RawData object.
     * @return the merged RawData object.
     */
    public static RawData merge(final RawData primaryValue, final RawData secondaryValue) {
        if (secondaryValue == null) {
            return primaryValue;
        }

        if (primaryValue == null) {
            return secondaryValue;
        }

        final String mergedRawIdentifier;
        if (primaryValue.hasRawIdentifier()) {
            mergedRawIdentifier = primaryValue.getRawIdentifier();
        } else {
            mergedRawIdentifier = secondaryValue.getRawIdentifier();
        }

        final String mergedRawType;
        if (primaryValue.hasRawType()) {
            mergedRawType = primaryValue.getRawType();
        } else {
            mergedRawType = secondaryValue.getRawType();
        }

        return new RawData(mergedRawIdentifier, mergedRawType);
    }

    /**
     * Check if Raw is empty
     *
     * @return Return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return !hasRawIdentifier() && !hasRawType();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.rawIdentifier);
        hash = 67 * hash + Objects.hashCode(this.rawType);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RawData other = (RawData) obj;
        if (!Objects.equals(this.rawIdentifier, other.rawIdentifier)) {
            return false;
        }
        return Objects.equals(this.rawType, other.rawType);
    }

    @Override
    public String toString() {
        final StringBuilder repr = new StringBuilder();
        repr.append(StringUtils.defaultString(rawIdentifier));

        if (StringUtils.isNotBlank(rawType)) {
            repr.append("<");
            repr.append(rawType);
            repr.append(">");
        }

        return repr.toString();
    }

    @Override
    public int compareTo(final RawData rawValue) {
        return this.toString().toLowerCase().compareTo(rawValue.toString().toLowerCase());
    }
}
